class SpockGrailsPlugin {

    def version = "0.2"
    def grailsVersion = "1.2-M3 > *"
    def dependsOn = [:]
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Luke Daley"
    def authorEmail = "ld@ldaley.com"
    def title = "Spock Integration - spockframework.org"
    def description = 'Brings BDD to Grails via the Spock Framework'
}
