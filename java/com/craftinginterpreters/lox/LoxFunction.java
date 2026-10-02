//> Functions lox-function
package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Expr.Function expressionDeclaration;
//> closure-field
  private final Environment closure;
  
//< closure-field
/* Functions lox-function < Functions closure-constructor
  LoxFunction(Stmt.Function declaration) {
*/
/* Functions closure-constructor < Classes is-initializer-field
  LoxFunction(Stmt.Function declaration, Environment closure) {
*/
//> Classes is-initializer-field
  private final boolean isInitializer;

  LoxFunction(Stmt.Function declaration, Environment closure,
              boolean isInitializer) {
    this.isInitializer = isInitializer;
//< Classes is-initializer-field
//> closure-constructor
    this.closure = closure;
//< closure-constructor
    this.declaration = declaration;
    this.expressionDeclaration = null;
  }
//> anonymous-function-constructor
  LoxFunction(Expr.Function declaration, Environment closure) {
    this.declaration = null;
    this.expressionDeclaration = declaration;
    this.closure = closure;
    this.isInitializer = false;
  }
//< anonymous-function-constructor
//> Classes bind-instance
  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    //> indexed-this
    environment.defineAt(0, instance);
    //< indexed-this
/* Classes bind-instance < Classes lox-function-bind-with-initializer
    return new LoxFunction(declaration, environment);
*/
//> lox-function-bind-with-initializer
    return new LoxFunction(declaration, environment,
                           isInitializer);
//< lox-function-bind-with-initializer
  }
//< Classes bind-instance
//> function-to-string
  @Override
  public String toString() {
    if (expressionDeclaration != null) {
      return "<fn>";
    }

    return "<fn " + declaration.name.lexeme + ">";
  }
//< function-to-string
//> function-arity
  @Override
  public int arity() {
    if (expressionDeclaration != null) {
      return expressionDeclaration.params.size();
    }

    return declaration.params.size();
  }
//< function-arity
//> function-call
  @Override
  public Object call(Interpreter interpreter,
                     List<Object> arguments) {
/* Functions function-call < Functions call-closure
    Environment environment = new Environment(interpreter.globals);
*/
    List<Token> params;
    List<Stmt> body;

    if (expressionDeclaration != null) {
      params = expressionDeclaration.params;
      body = expressionDeclaration.body;
    } else {
      params = declaration.params;
      body = declaration.body;
    }
//> call-closure
    Environment environment = new Environment(closure);
//< call-closure

//> indexed-parameters
    for (int i = 0; i < params.size(); i++) {
      Token parameter = params.get(i);

      Integer index =
          interpreter.getDeclarationSlot(parameter);

      environment.defineAt(index, arguments.get(i));
    }
//< indexed-parameters

/* Functions function-call < Functions catch-return
    interpreter.executeBlock(declaration.body, environment);
*/
//> catch-return
    try {
      interpreter.executeBlock(body, environment);
    } catch (Return returnValue) {
//> Classes early-return-this
      if (isInitializer) return closure.getAt(0, 0);

//< Classes early-return-this
      return returnValue.value;
    }
//< catch-return
//> Classes return-this

    if (isInitializer) return closure.getAt(0, 0);
//< Classes return-this
    return null;
  }
//< function-call
}
