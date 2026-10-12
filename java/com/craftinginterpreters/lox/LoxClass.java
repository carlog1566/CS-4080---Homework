//> Classes lox-class
package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/* Classes lox-class < Classes lox-class-callable
class LoxClass {
*/
//> lox-class-callable
class LoxClass extends LoxInstance implements LoxCallable {
//< lox-class-callable
  final String name;
//> Inheritance lox-class-superclass-field
  final LoxClass superclass;
//< Inheritance lox-class-superclass-field
/* Classes lox-class < Classes lox-class-methods

  LoxClass(String name) {
    this.name = name;
  }
*/
//> lox-class-methods
  private final Map<String, LoxFunction> methods;

/* Classes lox-class-methods < Inheritance lox-class-constructor
  LoxClass(String name, Map<String, LoxFunction> methods) {
*/
//> metaclass-constructor
  LoxClass(LoxClass metaclass, String name,
          LoxClass superclass, Map<String, LoxFunction> methods) {
    super(metaclass);

    this.name = name;
    this.superclass = superclass;
    this.methods = methods;
  }
//< metaclass-constructor
//< lox-class-methods
//> lox-class-find-method
  LoxFunction findMethod(String name) {
    if (superclass != null) {
      LoxFunction method =
          superclass.findMethod(name);

      if (method != null) {
        return method;
      }
    }

    return methods.get(name);
  }
//< lox-class-find-method
//> beta-method-chain
  private void collectMethods(
      String name,
      List<LoxFunction> chain) {

    if (superclass != null) {
      superclass.collectMethods(name, chain);
    }

    if (methods.containsKey(name)) {
      chain.add(methods.get(name));
    }
  }

  LoxFunction findMethod(
      LoxInstance instance,
      String name) {

    List<LoxFunction> chain =
        new ArrayList<>();

    collectMethods(name, chain);

    if (chain.isEmpty()) {
      return null;
    }

    LoxFunction inner = null;

    for (int i = chain.size() - 1;
        i >= 0;
        i--) {

      inner = chain.get(i).bind(
          instance, inner);
    }

    return inner;
  }
//< beta-method-chain

  @Override
  public String toString() {
    return name;
  }
//> lox-class-call-arity
  @Override
  public Object call(Interpreter interpreter,
                     List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this);
//> lox-class-call-initializer
    LoxFunction initializer = findMethod(instance,"init");
    if (initializer != null) {
      initializer.call(interpreter, arguments);
    }

//< lox-class-call-initializer
    return instance;
  }

  @Override
  public int arity() {
/* Classes lox-class-call-arity < Classes lox-initializer-arity
    return 0;
*/
//> lox-initializer-arity
    LoxFunction initializer = findMethod("init");
    if (initializer == null) return 0;
    return initializer.arity();
//< lox-initializer-arity
  }
//< lox-class-call-arity
}
