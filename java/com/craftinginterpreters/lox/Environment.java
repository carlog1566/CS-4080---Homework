//> Statements and State environment-class
package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

class Environment {
//> enclosing-field
  final Environment enclosing;
//< enclosing-field
  private final Map<String, Object> values = new HashMap<>();
//> local-slots
  private final List<Object> slots = new ArrayList<>();
//< local-slots
//> environment-constructors
  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }
//< environment-constructors
//> environment-get

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }
//> environment-get-enclosing

    if (enclosing != null) return enclosing.get(name);
//< environment-get-enclosing

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

//< environment-get
//> environment-assign
  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

//> environment-assign-enclosing
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

//< environment-assign-enclosing
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
//< environment-assign
//> environment-define
  void define(String name, Object value) {
    values.put(name, value);
  }
//< environment-define
//> Resolving and Binding ancestor
  Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing; // [coupled]
    }

    return environment;
  }
//< Resolving and Binding ancestor
//> Resolving and Binding get-at
  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }
//< Resolving and Binding get-at
//> Resolving and Binding assign-at
  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }
//< Resolving and Binding assign-at
//> indexed-environment-access
  void defineAt(int index, Object value) {
    while (slots.size() <= index) {
      slots.add(null);
    }

    slots.set(index, value);
  }

  Object getAt(int distance, int index) {
    return ancestor(distance).slots.get(index);
  }

  void assignAt(int distance, int index, Object value) {
    Environment target = ancestor(distance);

    while (target.slots.size() <= index) {
      target.slots.add(null);
    }

    target.slots.set(index, value);
  }
//< indexed-environment-access
//> omit
  @Override
  public String toString() {
    String result = values.toString();
    if (enclosing != null) {
      result += " -> " + enclosing.toString();
    }

    return result;
  }
//< omit
}
