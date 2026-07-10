public java.lang.Object m(Foo other, java.util.List<Foo> foos) {
    java.lang.Object a = other.@order
    java.lang.Object b = other?.@order
    java.lang.Object c = foos*.@order
    this.@order = a
}