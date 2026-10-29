package nvb.dev.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {

    private final Interpreter interpreter;
    private final Stack<Map<String, VariableInfo>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBinaryExpr(Expression.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expression.Literal expr) {
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expression.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expression.Unary expr) {
        resolve(expr.operand);
        return null;
    }

    @Override
    public Void visitCommaExpr(Expression.Comma expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitConditionalExpr(Expression.Conditional expr) {
        resolve(expr.condition);
        resolve(expr.trueBranch);
        resolve(expr.elseBranch);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expression.Variable expr) {
        if (!scopes.isEmpty()) {
            VariableInfo variableInfo = scopes.peek().get(expr.name.lexeme);
            if (variableInfo != null && !variableInfo.isDefined)
                Lox.error(expr.name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name, true);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expression.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name, false);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expression.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expression.Call expr) {
        resolve(expr.callee);
        for (Expression argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReplExpressionStatement(Statement.ReplExpression stmt) {
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var stmt) {
        declare(stmt.name);

        if (stmt.initializer != null)
            resolve(stmt.initializer);

        define(stmt.name);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);

        if (stmt.elseBranch != null)
            resolve(stmt.elseBranch);

        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitForStatement(Statement.For stmt) {
        beginScope();

        try {
            if (stmt.initializer != null)
                resolve(stmt.initializer);

            if (stmt.condition != null)
                resolve(stmt.condition);

            if (stmt.increment != null)
                resolve(stmt.increment);

            resolve(stmt.body);
        } finally {
            endScope();
        }
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break stmt) {
        return null;
    }

    @Override
    public Void visitContinueStatement(Statement.Continue stmt) {
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return stmt) {
        if (currentFunction == FunctionType.NONE)
            Lox.error(stmt.keyword, "Can't return from top-level code.");

        if (stmt.value != null)
            resolve(stmt.value);
        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        Map<String, VariableInfo> variableInfoMap = scopes.peek();
        for (Map.Entry<String, VariableInfo> entry : variableInfoMap.entrySet()) {
            if (!entry.getValue().isUsed)
                Lox.error(entry.getValue().name, "Local variable '" + entry.getKey() + "' is never used.");
        }
        scopes.pop();
    }

    void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }

    void resolve(Statement statement) {
        statement.accept(this);
    }

    void resolve(Expression expression) {
        expression.accept(this);
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, VariableInfo> scope = scopes.peek();
        if (scope.containsKey(name.lexeme))
            Lox.error(name, "Already variable with this name in this scope.");

        scope.put(name.lexeme, new VariableInfo(name));
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        VariableInfo variableInfo = scopes.peek().get(name.lexeme);
        if (variableInfo != null)
            variableInfo.isDefined = true;
    }

    private void resolveLocal(Expression expression, Token name, boolean readOperation) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                Map<String, VariableInfo> variableInfoMap = scopes.get(i);
                VariableInfo variableInfo = variableInfoMap.get(name.lexeme);
                if (readOperation)
                    variableInfo.isUsed = true;
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Statement.Function function, FunctionType functionType) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = functionType;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }
}
