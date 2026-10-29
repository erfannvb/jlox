package nvb.dev.lox;

public class VariableInfo {
    public final Token name;
    public boolean isDefined;
    public boolean isUsed;

    public VariableInfo(Token name) {
        this.name = name;
    }
}
