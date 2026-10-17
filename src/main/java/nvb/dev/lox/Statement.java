package nvb.dev.lox;

public abstract class Statement {

    interface Visitor<R> {
        R visitExpressionStatement(Expression stmt);
        R visitPrintStatement(Print stmt);
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

}
