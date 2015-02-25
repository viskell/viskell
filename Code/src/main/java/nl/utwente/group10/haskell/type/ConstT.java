package nl.utwente.group10.haskell.type;

public class ConstT extends Type {
    private String name;

    public ConstT(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
