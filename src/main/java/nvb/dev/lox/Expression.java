package nvb.dev.lox;

public abstract class Expression {

    static class Binary extends Expression {
        private final Expression left;
        private final Token operator;
        private final Expression right;

        Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    static class Literal extends Expression {
        private final Object value;

        Literal(Object value) {
            this.value = value;
        }
    }

    static class Grouping extends Expression {
        private final Expression expression;

        Grouping(Expression expression) {
            this.expression = expression;
        }
    }

    static class Unary extends Expression {
        private final Expression operand;
        private final Token operator;

        Unary(Expression operand, Token operator) {
            this.operand = operand;
            this.operator = operator;
        }
    }

}
