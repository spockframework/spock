package grails.plugin.spock.build.test

import grails.util.BuildSettings
import org.spockframework.buildsupport.SpeckClassFileFinder

class GrailsSpeckHelper {

    protected final baseDir
    protected final testClassesDir
    protected final parentLoader
    protected final currentClassLoader
    protected final resourceResolver
    protected final speckFinder = new SpeckClassFileFinder()
            
    GrailsSpeckHelper(BuildSettings settings, ClassLoader classLoader, Closure resourceResolver) {
        this.baseDir = settings.baseDir
        this.testClassesDir = settings.testClassesDir
        this.parentLoader = classLoader
        this.resourceResolver = resourceResolver
    }

    def createTests(List<String> testNames, String type) {
        def testTypeClassesDir = "${testClassesDir.absolutePath}/$type"
        def testTypeClassesDirFile = new File(testTypeClassesDir)
        def suite = new GrailsSpeckSuite()
        
        currentClassLoader = createClassLoader(type)
        
        def testResources = testNames.inject([]) { resources, filePattern ->
            
            // Filter out the method name or file extension
            int pos = filePattern.lastIndexOf('.')
            if (pos != -1) {
                filePattern = filePattern.substring(0, pos)
            }
            resources += resourceResolver("file:${testTypeClassesDir}/${filePattern}.class") as List
        }
        
        def speckFiles = testResources*.file.findAll { speckFinder.isSpeck(it) }
        speckFiles.each {
            def className = fileToClassName(it, testTypeClassesDirFile)
            suite << currentClassLoader.loadClass(className)
        }
        
        suite
    }
    
    protected fileToClassName(File file, File baseDir) {
        def filePath = file.canonicalFile.absolutePath
        def basePath = baseDir.canonicalFile.absolutePath
        if (!filePath.startsWith(basePath)) {
            throw new IllegalArgumentException("File path (${filePath}) is not descendent of base path (${basePath}).")
        }

        filePath = filePath.substring(basePath.size() + 1)
        def suffixPos = filePath.lastIndexOf(".")
        return filePath[0..(suffixPos - 1)].replace(File.separatorChar, '.' as char)
    }
        
    protected createClassLoader(String type) {
        def classPathAdditions = []
        classPathAdditions << new File("test/$type").toURI().toURL()
        classPathAdditions << new File(testClassesDir, type).toURI().toURL()
        
        new URLClassLoader(classPathAdditions as URL[], parentLoader)
    }
}