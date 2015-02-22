package nl.utwente.group10.haskell.type;

// TODO
public class VarT extends Type {
    public VarT(String name) {
        super(name.toLowerCase());
    }

    @Override
    public boolean compatibleWith(Type other) {
        return true;
    }

    @Override
    public String toHaskellType() {
        return null;
    }

    public String toString() {
        return getName();
    }

    public VarT clone() throws CloneNotSupportedException {
        return (VarT) super.clone();
    }
}
