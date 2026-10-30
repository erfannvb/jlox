package nvb.dev.lox;

public class VariableInfo {
    public final Token name;
    public boolean isDefined;
    public boolean isUsed;
    public final int index;

    public VariableInfo(Token name, int index) {
        this.name = name;
        this.index = index;
    }
}
