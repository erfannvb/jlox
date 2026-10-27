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
        R visitForStatement(For stmt);
        R visitBreakStatement(Break stmt);
        R visitContinueStatement(Continue stmt);
        R visitFunctionStatement(Function stmt);
        R visitReturnStatement(Return stmt);
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

    static class For extends Statement {
        final Statement initializer;
        final nvb.dev.lox.Expression condition;
        final nvb.dev.lox.Expression increment;
        final Statement body;

        For(Statement initializer, nvb.dev.lox.Expression condition, nvb.dev.lox.Expression increment, Statement body) {
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStatement(this);
        }
    }

    static class Break extends Statement {

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStatement(this);
        }
    }

    static class Continue extends Statement {

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitContinueStatement(this);
        }
    }

    static class Function extends Statement {
        final Token name;
        final List<Token> params;
        final List<Statement> body;

        Function(Token name, List<Token> params, List<Statement> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStatement(this);
        }
    }

    static class Return extends Statement {
        final Token keyword;
        final nvb.dev.lox.Expression value;

        Return(Token keyword, nvb.dev.lox.Expression value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return null;
        }
    }

}
