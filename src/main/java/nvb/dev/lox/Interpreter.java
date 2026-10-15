package nvb.dev.lox;

public class Interpreter implements Expression.Visitor<Object> {

    void interpret(Expression expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
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

                if (left instanceof String leftStr && right instanceof String rightStr)
                    yield leftStr + rightStr;

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
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
}
