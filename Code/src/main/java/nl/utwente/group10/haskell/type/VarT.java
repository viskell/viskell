package nl.utwente.group10.haskell.type;

public class VarT extends Type {
    private String name;

    public VarT(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
