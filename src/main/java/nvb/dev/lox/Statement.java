package nvb.dev.lox;

public abstract class Statement {

    interface Visitor<R> {
        R visitExpressionStatement(Expression stmt);
        R visitPrintStatement(Print stmt);
        R visitVarStatement(Var stmt);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Expression extends Statement {
        final nvb.dev.lox.Expression expression;

        Expression(nvb.dev.lox.Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    static class Print extends Statement {
        final nvb.dev.lox.Expression expression;

        Print(nvb.dev.lox.Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }

    static class Var extends Statement {
        final Token name;
        final nvb.dev.lox.Expression initializer;

        Var(Token name, nvb.dev.lox.Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStatement(this);
        }
    }

}
