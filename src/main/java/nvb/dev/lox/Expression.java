package nvb.dev.lox;

public abstract class Expression {

    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);

        R visitLiteralExpr(Literal expr);

        R visitGroupingExpr(Grouping expr);

        R visitUnaryExpr(Unary expr);

        R visitCommaExpr(Comma expr);

        R visitConditionalExpr(Conditional expr);

        R visitVariableExpr(Variable expr);

        R visitAssignExpr(Assign expr);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Binary extends Expression {
        final Expression left;
        final Token operator;
        final Expression right;

        Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Literal extends Expression {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class Grouping extends Expression {
        final Expression expression;

        Grouping(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class Unary extends Expression {
        final Token operator;
        final Expression operand;

        Unary(Token operator, Expression operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class Comma extends Expression {
        final Expression left;
        final Expression right;

        Comma(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCommaExpr(this);
        }
    }

    static class Conditional extends Expression {
        final Expression condition;
        final Expression trueBranch;
        final Expression elseBranch;

        Conditional(Expression condition, Expression trueBranch, Expression elseBranch) {
            this.condition = condition;
            this.trueBranch = trueBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitConditionalExpr(this);
        }
    }

    static class Variable extends Expression {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    static class Assign extends Expression {
        final Token name;
        final Expression value;

        Assign(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

}
