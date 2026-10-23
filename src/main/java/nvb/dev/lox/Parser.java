package nvb.dev.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokenList;
    private int current = 0;
    private int loopDepth = 0;

    Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    Statement parseRepl() {
        try {
            if (match(TokenType.VAR)) return varDeclaration();
            if (match(TokenType.PRINT)) return printStatement();

            Expression expr = expression();
            if (match(TokenType.SEMICOLON))
                return new Statement.Expression(expr);

            if (!isAtEnd())
                throw error(peek(), "Expect end of input.");

            return new Statement.ReplExpression(expr);
        } catch (ParseError error) {
            return null;
        }
    }

    private Expression expression() {
        if (match(TokenType.COMMA)) {
            Token operator = previous();
            Lox.error(operator.line, "Missing left operand.");
            assignment();
            return null;
        }

        Expression expr = assignment();

        while (match(TokenType.COMMA)) {
            Expression right = assignment();
            expr = new Expression.Comma(expr, right);
        }

        return expr;
    }

    private Expression assignment() {
        Expression expr = or();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expr instanceof Expression.Variable variable) {
                return new Expression.Assign(variable.name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expression or() {
        Expression expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = and();
            expr = new Expression.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expression and() {
        Expression expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            expr = new Expression.Logical(expr, operator, right);
        }

        return expr;
    }

    private Statement declaration() {
        try {
            if (match(TokenType.VAR))
                return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement statement() {
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.LEFT_BRACE)) return new Statement.Block(block());
        if (match(TokenType.BREAK)) return breakStatement();
        return expressionStatement();
    }

    private Statement forStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'for'.");

        Statement initializer;
        if (match(TokenType.SEMICOLON))
            initializer = null;
        else if (match(TokenType.VAR))
            initializer = varDeclaration();
        else
            initializer = expressionStatement();

        Expression condition = null;
        if (!match(TokenType.SEMICOLON))
            condition = expression();

        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        Expression increment = null;
        if (!check(TokenType.RIGHT_PARENTHESIS))
            increment = expression();

        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after for clauses.");

        loopDepth++;
        Statement body;
        try {
            body = statement();
            if (increment != null)
                body = new Statement.Block(Arrays.asList(body, new Statement.Expression(increment)));

            if (condition == null) condition = new Expression.Literal(true);
            body = new Statement.While(condition, body);

            if (initializer != null)
                body = new Statement.Block(Arrays.asList(initializer, body));
        } finally {
            loopDepth--;
        }

        return body;
    }

    private Statement ifStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after if.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(TokenType.ELSE))
            elseBranch = statement();

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private Statement varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expression initializer = null;
        if (match(TokenType.EQUAL))
            initializer = expression();

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }

    private Statement whileStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after condition.");

        loopDepth++;
        Statement body;
        try {
            body = statement();
        } finally {
            loopDepth--;
        }

        return new Statement.While(condition, body);
    }

    private Statement breakStatement() {
        Token breakToken = previous();
        consume(TokenType.SEMICOLON, "Expect ';' after 'break'.");

        if (loopDepth == 0)
            throw error(breakToken, "Can't use 'break' outside of a loop.");

        return new Statement.Break();
    }

    private List<Statement> block() {
        List<Statement> statementList = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statementList.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statementList;
    }

    private Statement expressionStatement() {
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Statement.Expression(value);
    }

    private Expression ternary() {
        Expression expr = equality();

        if (match(TokenType.QUESTION)) {
            Expression trueBranch = expression();
            consume(TokenType.COLON, ": is expected");
            Expression elseBranch = ternary();
            expr = new Expression.Conditional(expr, trueBranch, elseBranch);
        }

        return expr;
    }

    private Expression equality() {
        if (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Lox.error(operator.line, "Missing left operand.");
            comparison();
            return null;
        }

        Expression expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression comparison() {
        if (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Lox.error(operator.line, "Missing left operand.");
            term();
            return null;
        }

        Expression expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression term() {
        if (match(TokenType.PLUS)) {
            Token operator = previous();
            Lox.error(operator.line, "Missing left operand.");
            factor();
            return null;
        }

        Expression expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression factor() {
        if (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Lox.error(operator.line, "Missing left operand.");
            unary();
            return null;
        }

        Expression expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expression right = unary();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }

        return primary();
    }

    private Expression primary() {
        if (match(TokenType.FALSE)) return new Expression.Literal(false);
        if (match(TokenType.TRUE)) return new Expression.Literal(true);
        if (match(TokenType.NIL)) return new Expression.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING))
            return new Expression.Literal(previous().literal);

        if (match(TokenType.IDENTIFIER))
            return new Expression.Variable(previous());

        if (match(TokenType.LEFT_PARENTHESIS)) {
            Expression expr = expression();
            consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after expression.");
            return new Expression.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType tokenType, String message) {
        if (check(tokenType)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokenList.get(current);
    }

    private Token previous() {
        return tokenList.get(current - 1);
    }
}
