package org.grails.gradle.plugin

import grails.util.GrailsNameUtils

import org.codehaus.groovy.grails.cli.support.GrailsRootLoader
import org.codehaus.groovy.grails.cli.support.GrailsBuildHelper
import org.gradle.api.Plugin
import org.gradle.api.Project

class GrailsPlugin implements Plugin<Project> {
    /**
     * These Grails commands require the project's runtime dependencies
     * in the Grails root loader because they are not using the runtime
     * classpath (as they are supposed to).
     */
    static final RUNTIME_CLASSPATH_COMMANDS = [ "RunApp", "TestApp" ] as Set

    static public final GRAILS_TASK_PREFIX = "grails-"

    void apply(Project project) {
        if (!project.hasProperty("grailsVersion")) {
            throw new RuntimeException("[GrailsPlugin] the 'grailsVersion' project property is not set - you need to set this before applying the plugin")
        }
        
        project.configurations {
            logging
            
            compile.extendsFrom logging
            runtime.extendsFrom compile
            test.extendsFrom compile
            
            bootstrap.extendsFrom logging
            bootstrapRuntime.extendsFrom bootstrap, runtime
        }

        // Set up the 'bootstrap' configuration so that it contains
        // all the dependencies required by the Grails build system. This
        // pretty much means everything used by the scripts too.
        project.dependencies {
            bootstrap("org.grails:grails-bootstrap:${project.grailsVersion}") {
              exclude group: "org.jboss.netty"
            }
            bootstrap "org.grails:grails-scripts:${project.grailsVersion}"
            bootstrap "org.apache.ivy:ivy:2.1.0"
        }
        
        // Provide a task that allows the user to create a fresh Grails
        // project from a basic Gradle build file.
        project.task("init") << {
            // First make sure that a project version has been configured.
            if (project.version == "unspecified") {
                throw new RuntimeException("[GrailsPlugin] Build file must specify a 'version' property.")
            }

            // Don't create a new project if one already exists.
            if (project.file("application.properties").exists() && project.file("grails-app").exists()) {
                logger.warn "Grails project already exists - SKIPPING"
                return
            }

            // The project name comes from the name of the project
            // directory, but this can be overridden by an argument.
            def projName = project.hasProperty("args") ? project.args : project.projectDir.name
            runGrails("CreateApp", project, "--inplace --appVersion=" + project.version + " " + projName)
        }

        // Make the Grails 'clean' command available as a 'clean' task.
        project.task("clean", overwrite: true) << {
            runGrailsWithProps("Clean", project)
        }
        addDependencyToProjectLibTasks(project.clean)

        // Most people are used to a "test" target or task, but Grails
        // has "test-app". So we hard-code a "test" task.
        project.task(overwrite: true, "test") << {
            runGrailsWithProps("TestApp", project)
        }
        addDependencyToProjectLibTasks(project.test)

        // Gradle's Java plugin provides an "assemble" task. We map that
        // to the War command here.
        project.task(overwrite: true, "assemble") << {
            runGrailsWithProps("War", project)
        }
        addDependencyToProjectLibTasks(project.assemble)

        // Attach a generic “grailsTask” method so the build can run arbitrary tasks
        project.grailsTask = { String cmd, String args = null, String env = null ->
            this.runGrails(GrailsNameUtils.getNameFromScript(cmd), project, args, env)
        }

        // Convert any task executed from the command line 
        // with the special prefix into the Grails equivalent command.
        project.gradle.afterProject { p, ex ->
            if (p == project) { // Only add the task to the project that applied the plugin
                project.tasks.addRule("Grails command") { String name ->
                    if (name.startsWith(GRAILS_TASK_PREFIX)) {
                        // Add a task for the given Grails command.
                        project.task(name) << {
                            runGrailsWithProps(GrailsNameUtils.getNameFromScript(name - GRAILS_TASK_PREFIX), project)
                        }
                        addDependencyToProjectLibTasks(project."$name")
                    }
                }
            }
        }
    }

    /**
     * Evaluate any project lib dependencies in any of the configurations
     * and add the jar task of that project as a dependency of the task we created
     */
    private void addDependencyToProjectLibTasks(task) {
        task.project.configurations.each {
            task.dependsOn(it.getTaskDependencyFromProjectDependency(true, "jar"))
        }
    }
    
    /**
     * Launches Grails and executes the given command. Any command
     * arguments or environment are picked up from the "args" and "env"
     * project properties.
     * @param cmd The Grails command to execute. Note that this should
     * actually be the name of the script, not the command name. So
     * "RunApp" rather than "run-app".
     * @param project The Gradle project to run Grails in.
     */
    private void runGrailsWithProps(String cmd, Project project) {
        def cmdArgs = project.hasProperty("args") ? project.args : null
        def cmdEnv = project.hasProperty("env") ? project.env : null
        runGrails(cmd, project, cmdArgs, cmdEnv)
    }

    /**
     * Launches Grails and executes the given command.
     * @param cmd The Grails command to execute. Note that this should
     * actually be the name of the script, not the command name. So
     * "RunApp" rather than "run-app".
     * @param project The Gradle project to run Grails in.
     * @param args (Optional) Any arguments (as a single, space-separated
     * string) that you want to pass to the Grails command. Defaults to
     * <code>null</code> (no args).
     * @param env (Optional) The environment to run the Grails command
     * in. Defaults to <code>null</code>, which means that the command
     * uses whatever its default environment is.
     */
    private void runGrails(String cmd, Project project, String args = null, String env = null) {
        // Start by checking that the project has both Grails and a
        // logging implementation as dependencies. Otherwise we fail
        // the build.
        def runtimeDeps = project.configurations.runtime.allDependencies
        def grailsDep = runtimeDeps.find { it.group == 'org.grails' && it.name.startsWith('grails-') }
        if (!grailsDep) {
            throw new RuntimeException("[GrailsPlugin] Your project does not contain any 'grails-*' dependencies in 'compile' or 'runtime'.")
        }

        def loggingDep = runtimeDeps.find { it.group == 'org.slf4j' && it.name.startsWith('slf4j-') }
        if (!loggingDep) {
            throw new RuntimeException("[GrailsPlugin] Your project does not contain an SLF4J logging implementation dependency.")
        }


        // Add the "tools.jar" to the classpath so that the Grails
        // scripts can run native2ascii. First assume that "java.home"
        // points to a JRE within a JDK.
        def javaHome = System.getProperty("java.home");
        def toolsJar = new File(javaHome, "../lib/tools.jar");
        if (!toolsJar.exists()) {
            // The "tools.jar" cannot be found with that path, so
            // now try with the assumption that "java.home" points
            // to a JDK.
            toolsJar = new File(javaHome, "tools.jar");
        }

        // There is no tools.jar, so native2ascii may not work. Note
        // that on Mac OS X, native2ascii is already on the classpath.
        if (!toolsJar.exists() && !System.getProperty('os.name') == 'Mac OS X') {
            project.logger.warn "[GrailsPlugin] Cannot find tools.jar in JAVA_HOME, so native2ascii may not work."
        }
        
        def bootsrapConfiguration = cmd in RUNTIME_CLASSPATH_COMMANDS ? project.configurations.bootstrapRuntime : project.configurations.bootstrap
        
        // Get the bootstrap configuration as a list of URLs
        // and add tools.jar to it.
        def classpath = bootsrapConfiguration.files.collect { it.toURI().toURL() }
        classpath << toolsJar.toURI().toURL()

        // So we know what files are on what classpaths.
        project.logger.info "Classpath for Grails root loader:\n  ${classpath.join('\n  ')}"
        project.logger.info "Compile classpath:\n  ${project.configurations.compile.files.join('\n  ')}"
        project.logger.info "Test classpath:\n  ${project.configurations.test.files.join('\n  ')}"
        project.logger.info "Runtime classpath:\n  ${project.configurations.runtime.files.join('\n  ')}"

        // Finally, kick off Grails with the given command. GrailsBuildHelper
        // allows us to easily configure the Grails build settings and the
        // various lists of dependencies. It also ensures that the Grails
        // build system runs in its own class loader so that Gradle's
        // dependencies don't conflict with it.
        def rootLoader = new GrailsRootLoader(classpath as URL[], ClassLoader.systemClassLoader)
        def grailsHelper = new GrailsBuildHelper(rootLoader, null, project.projectDir.absolutePath)
        grailsHelper.compileDependencies = project.configurations.compile.files as List
        grailsHelper.testDependencies = project.configurations.test.files as List
        grailsHelper.runtimeDependencies = project.configurations.runtime.files as List
        grailsHelper.projectWorkDir = project.buildDir
        grailsHelper.classesDir = new File(project.buildDir, "classes")
        grailsHelper.testClassesDir = new File(project.buildDir, "test-classes")
        grailsHelper.resourcesDir = new File(project.buildDir, "resources")
        grailsHelper.projectPluginsDir = new File(project.buildDir, "plugins")
        grailsHelper.testReportsDir = new File(project.buildDir, "test-results")

        // Grails 1.2+ only. Previous versions of Grails don't have the
        // 'dependenciesExternallyConfigured' property. Note that this
        // is a HACK because the 'settings' field is private.
        //
        // We can't simply check whether the property exists on the
        // helper because it's the 1.2 version, whereas the project may
        // be using Grails version 1.1. That's why we have to get hold
        // of the actual BuildSettings instance.
        def buildSettings = grailsHelper.settings
        if (buildSettings.metaClass.hasProperty(buildSettings, "dependenciesExternallyConfigured")) {
            grailsHelper.dependenciesExternallyConfigured = true
        }
        
        // Using a Groovy trick here, because we either want to call
        // the execute() method that takes two arguments, or the one
        // that takes three. So rather than calling those methods
        // explicitly within a condition, we create an argument list
        // and then use the spread operator.
        def methodArgs = [ cmd, args ]
        if (env) methodArgs << env

        def retval = grailsHelper.execute(*methodArgs)
        if (retval != 0) {
            throw new RuntimeException("[GrailsPlugin] Grails returned non-zero value: " + retval);
        }
    }
}
