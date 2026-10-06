package nvb.dev.lox;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens;

    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanTokens();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
