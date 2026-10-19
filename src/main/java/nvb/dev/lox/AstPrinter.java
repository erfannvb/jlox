package nvb.dev.lox;

public class AstPrinter implements Expression.Visitor<String> {

    @Override
    public String visitBinaryExpr(Expression.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitLiteralExpr(Expression.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitGroupingExpr(Expression.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitUnaryExpr(Expression.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.operand);
    }

    @Override
    public String visitCommaExpr(Expression.Comma expr) {
        return parenthesize(",", expr.left, expr.right);
    }

    @Override
    public String visitConditionalExpr(Expression.Conditional expr) {
        return parenthesize("conditional", expr.condition, expr.trueBranch, expr.elseBranch);
    }

    @Override
    public String visitVariableExpr(Expression.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitAssignExpr(Expression.Assign expr) {
        return parenthesize("=", new Expression.Variable(expr.name), expr.value);
    }

    private String parenthesize(String name, Expression... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expr : expressions) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    String print(Expression expression) {
        return expression.accept(this);
    }
}
