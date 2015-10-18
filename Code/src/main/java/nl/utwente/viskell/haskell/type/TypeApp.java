package nl.utwente.viskell.haskell.type;

import java.util.LinkedList;
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
    public String prettyPrint(int fixity) {
        List<Type> chain = this.asFlattenedAppChain();
        Type ftype = chain.remove(0);
        return ftype.prettyPrintAppChain(fixity, chain);
    }

    protected final List<Type> asFlattenedAppChain(){
        final LinkedList<Type> chain = new LinkedList<>();
        Type type = this;
        while (type instanceof TypeApp) {
            TypeApp ta = (TypeApp) type;
            chain.addFirst(ta.typeArg);
            type = ta.typeFun;
        }

        chain.addFirst(type);
        return chain;
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
