package grails.plugin.spock.build

import org.codehaus.groovy.grails.test.FormattedOutput
import org.codehaus.groovy.grails.test.XMLFormatter
import org.codehaus.groovy.grails.test.PlainFormatter

class ReportFactory {

    final protected formatters = [
        xml: [
            clazz: XMLFormatter,
            filenameGenerator: { "TEST-${it}.xml" }
        ], 
        plain: [
            clazz: PlainFormatter,
            filenameGenerator: { "plain/TEST-${it}.txt" }
        ]
    ]
    
    final protected reportsDir
    final protected formats
    
    ReportFactory(File reportsDir, List<String> formats) {
        this.reportsDir = reportsDir
        this.formats = formats
    }
    
    List<FormattedOutput> createReports(String name) {
        formats.collect { createReport(it, name) }
    }
    
    protected createReport(format, name) {
        def formatter = formatters[format]
        if (formatter) {
            new FormattedOutput(
                new File(reportsDir, formatter.filenameGenerator(name)),
                formatter.clazz.newInstance()
            )
        } else {
            throw new RuntimeException("Unknown formatter type: $format")
        }
    }
}