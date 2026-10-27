package nvb.dev.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Statement.Function declaration;

    public LoxFunction(Statement.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return e) {
            return e.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
