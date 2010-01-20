import org.codehaus.groovy.grails.resolve.IvyDependencyManager
import grails.util.GrailsUtil

// Workaround for GRAILS-5755
if (GrailsUtil.grailsVersion == '1.2.0') {
    // Update the cached dependencies in grailsSettings, and add new jars to the root loader
    ['compile', 'build', 'test', 'runtime'].each { type ->
        def existing = grailsSettings."${type}Dependencies"
        def all = grailsSettings.dependencyManager.resolveDependencies(IvyDependencyManager."${type.toUpperCase()}_CONFIGURATION").allArtifactsReports.localFile
        def toAdd = all - existing
        if (toAdd) {
            existing.addAll(toAdd)
            if (type in ['build', 'test']) {
                toAdd.each {
                    grailsSettings.rootLoader.addURL(it.toURL())
                }
            }
        }
    }
}

