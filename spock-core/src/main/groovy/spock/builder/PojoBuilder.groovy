/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.builder

import org.spockframework.util.Nullable

import java.lang.reflect.Type
import java.lang.reflect.Method
import org.codehaus.groovy.runtime.MetaClassHelper
import org.spockframework.gentyref.GenericTypeReflector
import java.util.regex.Pattern

import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import org.spockframework.util.UnreachableCodeError
import org.codehaus.groovy.runtime.InvokerHelper
import org.spockframework.util.MopUtil
import java.lang.reflect.Field

// TODO: test building pojo enriched with expandometaclass/category property
// TODO: test call to void varargs method in expect: block
// (related: find all calls to MetaMethod.invoke and replace with doMethodInvoke)
// IDEA: allow syntax foo = { ... } to make it clear built object should be assigned to a property (can't handle all cases with method syntax)
// IDEA: helpful coercions (esp. for configuration): List -> Set (enables the use of list literals for sets), potentially String -> Enum
interface ISlotFactory {
  @Nullable
  ISlot create(owner, Type ownerType, String name)
}

class SetterSlotFactory implements ISlotFactory {
  ISlot create(owner, Type ownerType, String name) {
    MetaProperty property = owner.getClass().metaClass.getMetaProperty(name)
    property && MopUtil.isWriteable(property) ? new PropertySlot(owner, ownerType, property) : null
  }
}

class PropertySlot implements ISlot {
  private final owner
  private final Type ownerType
  private final MetaProperty property

  PropertySlot(owner, Type ownerType, MetaProperty property) {
    this.owner = owner;
    this.ownerType = ownerType;
    this.property = property;
  }

  Type getType() {
    // could possibly add fast path here, but be careful (inner classes etc.)
    
    Method setter = MopUtil.setterFor(property)
    if (setter) return GenericTypeReflector.getExactParameterTypes(setter, ownerType)[0]

    Field field = MopUtil.fieldFor(property)
    if (field) return GenericTypeReflector.getExactFieldType(field, ownerType)
    
    throw new UnreachableCodeError()
  }

  void write(Object value) {
    property.setProperty(owner, value)
  }

}

class SetterLikeSlot implements ISlot {
  private final owner
  private final Type ownerType
  private final MetaMethod setterLikeMethod

  SetterLikeSlot(owner, Type ownerType, MetaMethod setterLikeMethod) {
    this.owner = owner
    this.ownerType = ownerType
    this.setterLikeMethod = setterLikeMethod
  }

  Type getType() {
    Method m = MopUtil.methodFor(setterLikeMethod)
    m ? GenericTypeReflector.getExactParameterTypes(m, ownerType)[0] : setterLikeMethod.nativeParameterTypes[0]
  }

  void write(value) {
    setterLikeMethod.doMethodInvoke(owner, value)
  }
}

class AddSlotFactory implements ISlotFactory {
  ISlot create(owner, Type ownerType, String name) {
    def addName = "add" + MetaClassHelper.capitalize(name)
    def addMethod = owner.metaClass.pickMethod(addName, Object)
    addMethod ? new SetterLikeSlot(owner, ownerType, addMethod) : null
  }
}

class CollectionSlotFactory implements ISlotFactory {
  private static final Pattern pluralIESPattern = Pattern.compile(".*[^aeiouy]y", Pattern.CASE_INSENSITIVE)

  ISlot create(owner, Type ownerType, String name) {
    def plural = toPluralForm(name)
    MetaProperty property = owner.getClass().metaClass.getMetaProperty(plural)
    property && Collection.isAssignableFrom(property.type) && MopUtil.isReadable(property) ?
      new CollectionSlot(plural, owner, ownerType, property) : null
  }

  private String toPluralForm(String word) {
    if (word[-1].equalsIgnoreCase("s")) return word + "es"
    def matchesIESRule = pluralIESPattern.matcher(word).matches()
    matchesIESRule ? word[0..<word.size() - 1] + "ies" : word + "s"
  }
}

class CollectionSlot implements ISlot {
  private final String name
  private final owner
  private final Type ownerType
  private final MetaProperty property

  CollectionSlot(String name, owner, Type ownerType, MetaProperty property) {
    this.name = name
    this.owner = owner
    this.ownerType = ownerType
    this.property = property
  }

  Type getType() {
    def type = collectionType
    if (type instanceof ParameterizedType) type.actualTypeArguments[0]
    else if (type instanceof Class) Object
    else throw new UnreachableCodeError()
  }

  private getCollectionType() {
     Method getter = MopUtil.getterFor(property)
    if (getter) return GenericTypeReflector.getExactReturnType(getter, ownerType)

    Field field = MopUtil.fieldFor(property)
    if (field) return GenericTypeReflector.getExactFieldType(field, ownerType)

    throw new UnreachableCodeError()
  }

  void write(value) {
    def collection = property.getProperty(owner)
    if (collection == null) {
      if (MopUtil.isWriteable(property)) {
        collection = createCollection(property.type)
        property.setProperty(owner, collection)
      } else {
        throw new RuntimeException(
"Cannot add element to collection property '$name' because it is neither initialized nor does it have a setter")
      }
    }
    collection.add(value)
  }

  private Collection createCollection(Class clazz) {
    if ((clazz.modifiers & Modifier.ABSTRACT) == 0) {
      return BuilderHelper.createInstance(clazz)
    }

    if (List.isAssignableFrom(clazz)) return new ArrayList()
    if (Set.isAssignableFrom(clazz)) return new HashSet()

    throw new RuntimeException("Don't know how to create a collection of type '${clazz.name}'")
  }
}

interface ISlot {
  Type getType()
  void write(value)
}

class GestaltBuilder {
  void build(IGestalt root) {
    new Sculpturer().$form(root) 
  }
}

/**
 * Forms a gestalt from its blueprint.
 */
class Sculpturer {
  private final IGestalt $gestalt

  void $form(IGestalt gestalt) {
    Closure blueprint = gestalt.blueprint
    if (!blueprint) return

    $gestalt = gestalt
    blueprint.delegate = this
    blueprint.resolveStrategy = Closure.DELEGATE_FIRST
    blueprint(gestalt.subject)
  }

  def getProperty(String name) {
    $gestalt.getValue(name)
  }

  void setProperty(String name, value) {
    $gestalt.setValue(name, value)
  }

  def invokeMethod(String name, args) {
    def subGestalt = $gestalt.subGestalt(name, args)
    new Sculpturer().$form(subGestalt)
  }
}

interface IGestalt {
  def getSubject()
  @Nullable Closure getBlueprint()
  
  def getValue(String name)
  void setValue(String name, value)
  IGestalt subGestalt(String name, Object[] args)
}

class PojoGestalt implements IGestalt {
  private final pojo
  private final Type pojoType
  private final Closure blueprint
  private final List<ISlotFactory> slotFactories

  PojoGestalt(pojo, Type pojoType, Closure blueprint, List<ISlotFactory> slotFactories) {
    this.pojo = pojo
    this.pojoType = pojoType
    this.blueprint = blueprint
    this.slotFactories = slotFactories
  }

  def getSubject() {
    return pojo
  }

  Closure getBlueprint() {
    blueprint
  }

  def getValue(String name) {
    pojo."$name"
  }

  void setValue(String name, value) {
    pojo."$name" = value
  }

  // foo([a:1],b) and foo(a:1,b) are currently not distinguishable in Groovy
  // neither are foo([a:1],b) and foo(b,a:1)
  // so we should probably add some heuristics to tell them apart (look at subject's method signatures)
  // same for telling apart last arg (could be blueprint or last constructor arg)
  // current impl is dead stupid:
  // - named args not treated specially
  // - last arg is closure => treat as blueprint
  IGestalt subGestalt(String name, Object[] args) {
    def slot = findSlot(name, args)
    def gestalt = createGestalt(slot.type, args)
    slot.write(gestalt.subject)
    new Sculpturer().$form(gestalt)
    gestalt
  }

  private ISlot findSlot(String name, Object[] args) {
    for (factory in slotFactories) {
      def slot = factory.create(pojo, pojoType, name)
      if (slot != null) return slot
    }
    throw new RuntimeException("Cannot find a slot named '$name'")
  }

  private IGestalt createGestalt(Type newType, Object[] args) {
    def newBlueprint = null
    if (args && args[-1] instanceof Closure) {
      newBlueprint = args[-1]
      args = args[0..<args.size() - 1]
    }

    def newClazz = GenericTypeReflector.erase(newType) // TODO: check that this succeeds (Type could be a TypeVariable etc.)
    def newPojo = BuilderHelper.createInstance(newClazz, args)
    return new PojoGestalt(newPojo, newType, newBlueprint, slotFactories)
  }
}

// TODO: provide a way to set visibility of methods/properties to look for
class PojoBuilder {
  List<ISlotFactory> slotFactories = [new SetterSlotFactory(), new AddSlotFactory(), new CollectionSlotFactory()]

  private final gestaltBuilder = new GestaltBuilder()

  def build(pojo, Closure blueprint) {
    def gestalt = new PojoGestalt(pojo, pojo.getClass(), blueprint, slotFactories)
    gestaltBuilder.build(gestalt)
    pojo
  }
}

class BuilderHelper {
  static createInstance(Class clazz, Object[] args) {
    if (args.size() == 1) {
      def arg = args[0]
      if (MetaClassHelper.isAssignableFrom(clazz, arg.getClass())) return arg
      // IDEA: could do additional coercions here, like Groovy does when setting a property
      // (note that we don't know if we are setting a property or invoking a method):
      // int -> byte (at least if it fits), etc.
    }

    if (args && args[0] instanceof Class) {

    }   

    if (clazz.modifiers & Modifier.ABSTRACT)
      throw new RuntimeException(
"Cannot instantiate ${clazz.primitive ? "primitive" : clazz.interface ? "interface" : "abstract"} type '$clazz.name'")

    // TODO: need exception handling for better error messages?
    return InvokerHelper.invokeConstructorOf(clazz, args)
  }
}