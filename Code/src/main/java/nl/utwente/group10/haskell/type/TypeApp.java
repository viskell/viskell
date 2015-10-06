package nl.utwente.group10.haskell.type;

import java.util.ArrayList;
import java.util.List;


public class TypeApp extends ConcreteType {

    /*
     * The Type of function part of of type application, usually some type constructor
     */
    private final Type typeFun;
    
    /*
     * The Type of argument part of a type application 
     */
    private final Type typeArg;
    
    TypeApp(Type typeFun, Type typearg) {
        this.typeFun = typeFun;
        this.typeArg = typearg;
    }

    public Type getTypeFun() {
        return this.typeFun;
    }

    public Type getTypeArg() {
        return this.typeArg;
    }

    @Override
    public String toHaskellType(int fixity) {
        final ArrayList<Type> targs = new ArrayList<Type>();
        targs.add(this.typeArg);
        return (this.typeFun.asTypeAppChain(fixity, targs));
    }

    @Override
    protected String asTypeAppChain(final int fixity, final List<Type> args) {
        final ArrayList<Type> targs = new ArrayList<Type>(args);
        targs.add(this.typeArg);
        return this.typeFun.asTypeAppChain(fixity, targs);
    }
    
    @Override
    public TypeApp getFresh(TypeScope scope) {
        return new TypeApp(this.typeFun.getFresh(scope), this.typeArg.getFresh(scope));
    }

    @Override
    public boolean containsOccurenceOf(TypeVar tvar) {
        return this.typeFun.containsOccurenceOf(tvar) || this.typeArg.containsOccurenceOf(tvar);
    }

    @Override
    public String toString() {
        return String.format("(%s @ %s)", this.typeFun, this.typeArg);
    }

}
