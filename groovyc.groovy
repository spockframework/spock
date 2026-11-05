import groovy.transform.CompileStatic
import groovy.lang.GroovySystem

withConfig(configuration) {
  if ((GroovySystem.getVersion().split(/\./, 2).first() as int) >= 4) {
    ast(CompileStatic)
    //ast(groovy.transform.TypeChecked)
  }
}
