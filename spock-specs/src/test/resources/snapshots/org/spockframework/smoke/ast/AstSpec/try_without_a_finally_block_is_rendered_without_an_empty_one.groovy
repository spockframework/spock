public java.lang.Object m() {
    try {
        this.println('x')
    }
    catch (java.lang.RuntimeException e) {
        this.println(e)
    }
    try {
        this.println('y')
    }
    finally {
        this.println('done')
    }
}