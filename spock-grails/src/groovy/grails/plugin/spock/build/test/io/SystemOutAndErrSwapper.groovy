package grails.plugin.spock.build.test.io

// TODO this class needs a better name and method name
class SystemOutAndErrSwapper {

    protected swappedOutOut
    protected swappedOutErr
    
    protected swappedInOut
    protected swappedInErr
    
    def swap(Closure swappedFor) {
        this.swappedOutOut = System.out
        this.swappedOutErr = System.err

        this.swappedInOut = new ByteArrayOutputStream()
        this.swappedInErr = new ByteArrayOutputStream()

        System.setOut(new PrintStream(this.swappedInOut))
        System.setErr(new PrintStream(this.swappedInErr))
        
        def streams = [this.swappedInOut, this.swappedInErr]
        
        try {
            switch(swappedFor.maximumNumberOfParameters) {
                case 0:
                    swappedFor()
                break
                case 1:
                    swappedFor(streams)
                break
                default:
                    swappedFor(*streams)
                break
            }
        } finally {
            System.out = this.swappedOutOut
            System.err = this.swappedOutErr

            this.swappedOutOut = null
            this.swappedOutErr = null

            this.swappedInOut = null
            this.swappedInErr = null
        }

        streams
    }
}