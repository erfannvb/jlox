package nvb.dev.lox;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    final Environment globals = new Environment();

    private static class BreakSignal extends RuntimeException {
    }

    private static class ContinueSignal extends RuntimeException {
    }

    private static final Object UNINITIALIZED = new Object();

    private Environment environment = globals;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);

            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitBinaryExpr(Expression.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case BANG_EQUAL -> !isEqual(left, right);
            case EQUAL_EQUAL -> isEqual(left, right);
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left <= (double) right;
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double leftDouble && right instanceof Double rightDouble)
                    yield leftDouble + rightDouble;

                if (left instanceof String || right instanceof String)
                    yield String.valueOf(left) + right;

                throw new RuntimeError(expr.operator, "Operands must be two numbers or at least one string.");
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0.0) {
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                yield (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left * (double) right;
            }
            default -> null;
        };
    }

    @Override
    public Object visitLiteralExpr(Expression.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expression.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expression.Unary expr) {
        Object right = evaluate(expr.operand);

        return switch (expr.operator.type) {
            case BANG -> !isTruthy(right);
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }
            default -> null;
        };
    }

    @Override
    public Object visitCommaExpr(Expression.Comma expr) {
        evaluate(expr.left);
        return evaluate(expr.right);
    }

    @Override
    public Object visitConditionalExpr(Expression.Conditional expr) {
        Object condition = evaluate(expr.condition);
        if (isTruthy(condition))
            return evaluate(expr.trueBranch);

        return evaluate(expr.elseBranch);
    }

    @Override
    public Object visitVariableExpr(Expression.Variable expr) {
        Object obj = environment.get(expr.name);
        if (obj == UNINITIALIZED)
            throw new RuntimeError(expr.name, "Not initialized!");

        return obj;
    }

    @Override
    public Object visitAssignExpr(Expression.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expression.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitCallExpr(Expression.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expression expression : expr.arguments) {
            arguments.add(evaluate(expression));
        }

        if (!(callee instanceof LoxCallable function))
            throw new RuntimeError(expr.parenthesis, "Can only functions and classes.");

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.parenthesis, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitReplExpressionStatement(Statement.ReplExpression stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var stmt) {
        Object value = null;
        if (stmt.initializer != null)
            value = evaluate(stmt.initializer);
        else
            value = UNINITIALIZED;

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If stmt) {
        if (isTruthy(evaluate(stmt.condition)))
            execute(stmt.thenBranch);
        else if (stmt.elseBranch != null)
            execute(stmt.elseBranch);

        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakSignal ignored) {
                break;
            } catch (ContinueSignal ignored) {
                continue;
            }
        }
        return null;
    }

    @Override
    public Void visitForStatement(Statement.For stmt) {
        Environment previous = this.environment;

        try {
            this.environment = new Environment(this.environment);

            if (stmt.initializer != null)
                execute(stmt.initializer);

            while (stmt.condition == null || isTruthy(evaluate(stmt.condition))) {
                try {
                    execute(stmt.body);
                } catch (BreakSignal ignored) {
                    break;
                } catch (ContinueSignal ignored) {
                }

                if (stmt.increment != null)
                    evaluate(stmt.increment);
            }
        } finally {
            this.environment = previous;
        }

        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break stmt) {
        throw new BreakSignal();
    }

    @Override
    public Void visitContinueStatement(Statement.Continue stmt) {
        throw new ContinueSignal();
    }

    @Override
    public Void visitFunctionStatement(Statement.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new Return(value);
    }

    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean b) return b;
        return true;
    }

    private boolean isEqual(Object firstObj, Object secondObj) {
        if (firstObj == null && secondObj == null) return true;
        if (firstObj == null) return false;

        return firstObj.equals(secondObj);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;

        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
}
