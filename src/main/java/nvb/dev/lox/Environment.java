package nvb.dev.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> globalVariables = new HashMap<>();
    private final List<Object> localVariables = new ArrayList<>();

    Environment() {
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (globalVariables.containsKey(name.lexeme))
            return globalVariables.get(name.lexeme);

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object getAt(int distance, int index) {
        Environment ancestor = ancestor(distance);
        return ancestor.localVariables.get(index);
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }

    void define(String name, Object value) {
        if (isGlobal())
            globalVariables.put(name, value);
        else
            localVariables.add(value);
    }

    void assign(Token name, Object value) {
        if (globalVariables.containsKey(name.lexeme)) {
            globalVariables.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assignAt(int distance, int index, Object value) {
        Environment ancestor = ancestor(distance);
        ancestor.localVariables.set(index, value);
    }

    private boolean isGlobal() {
        return this.enclosing == null;
    }
}
