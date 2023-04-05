public void loop() {
    do {
        this.println('once')
    } while (false)
}

public void methodRef() {
    [].forEach(java.lang.System::'print')
}

public void lambdas() {
    java.lang.Object lambda = ( java.lang.Object x) -> {
        x * x }
    java.lang.Object lambdaMultiArg = ( int a, int b) -> {
        a <=> b }
    java.lang.Object lambdaNoArg = ( ) -> {
        throw new java.lang.RuntimeException('bam')
    }
}

public void blockStatements() {
    with_label:
    {
        this.println(foo)
    }
}