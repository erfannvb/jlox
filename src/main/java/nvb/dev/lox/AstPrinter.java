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
}
