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

//> no-inner-function
  private static final LoxCallable NO_INNER =
      new LoxCallable() {
        @Override
        public int arity() {
          return 0;
        }

        @Override
        public Object call(
            Interpreter interpreter,
            List<Object> arguments) {
          return null;
        }

        @Override
        public String toString() {
          return "<inner>";
        }
      };
//< no-inner-function

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
//> beta-bind
  LoxFunction bind(LoxInstance instance) {
    return bind(instance, null);
  }

  LoxFunction bind(
      LoxInstance instance,
      LoxFunction inner) {

    Environment environment =
        new Environment(closure);

    // "this"
    environment.defineAt(0, instance);

    // "inner"
    if (inner != null) {
      environment.defineAt(1, inner);
    } else {
      environment.defineAt(1, NO_INNER);
    }

    // Normal named method.
    if (declaration != null) {
      return new LoxFunction(
          declaration,
          environment,
          isInitializer);
    }

    // Keeps anonymous functions compatible too.
    return new LoxFunction(
        expressionDeclaration,
        environment);
  }
//< beta-bind
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
    if (declaration.params == null) return 0;

    return declaration.params.size();
  }
//< function-arity
//> is-getter
  boolean isGetter() {
    return declaration.params == null;
  }
//< is-getter
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
    if (params != null) {
      for (int i = 0; i < params.size(); i++) {
        Token parameter = params.get(i);

        Integer index =
            interpreter.getDeclarationSlot(parameter);

        environment.defineAt(index, arguments.get(i));
      }
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
