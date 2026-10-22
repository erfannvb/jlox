package nvb.dev.lox;

import java.util.List;

public abstract class Statement {

    interface Visitor<R> {
        R visitExpressionStatement(Expression stmt);
        R visitReplExpressionStatement(ReplExpression stmt);
        R visitPrintStatement(Print stmt);
        R visitVarStatement(Var stmt);
        R visitBlockStatement(Block stmt);
        R visitIfStatement(If stmt);
        R visitWhileStatement(While stmt);
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

    static class Block extends Statement {
        final List<Statement> statements;

        Block(List<Statement> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    static class ReplExpression extends Statement {
        final nvb.dev.lox.Expression expression;

        ReplExpression(nvb.dev.lox.Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReplExpressionStatement(this);
        }
    }

    static class If extends Statement {
        final nvb.dev.lox.Expression condition;
        final Statement thenBranch;
        final Statement elseBranch;

        If(nvb.dev.lox.Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }
    }

    static class While extends Statement {
        final nvb.dev.lox.Expression condition;
        final Statement body;

        While(nvb.dev.lox.Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

}
