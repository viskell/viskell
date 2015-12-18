package nl.utwente.viskell.haskell.type;

public class TypeCon extends ConcreteType {
    /**
     * The name of type constructor.
     */
    protected final String name;

    TypeCon(String name) {
        this.name = name;
    }

    /**
     * @return the name of the type constructor
     */
    public String getName() {
        return name;
    }

    @Override
    public String prettyPrint(int fixity) {
        return this.name;
    }

    @Override
    public TypeCon getFresh(TypeScope scope) {
        return this;
    }

    @Override
	public Type getConcrete() {
    	return Type.con(this.name);
	}

	@Override
    public boolean containsOccurenceOf(TypeVar tvar) {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TypeCon && this.name.equals(((TypeCon)other).getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }

}
