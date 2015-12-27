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
    
    /** The set of constraints attached to this type application that can not yet be propagated further. */
    private ConstraintSet constraints;
    
    TypeApp(Type typeFun, Type typearg) {
        this.typeFun = typeFun;
        this.typeArg = typearg;
        this.constraints = new ConstraintSet();
    }

    public Type getTypeFun() {
        return this.typeFun;
    }

    public Type getTypeArg() {
        return this.typeArg;
    }

    protected ConstraintSet getConstraint() {
    	return this.constraints;
    }
    
    /**
     * Remove all constraints from this type application, to be used only after all are satisfied.
     */
    protected void clearConstraints() {
    	this.constraints = new ConstraintSet();
    }
    
    /**
     * Extends the constraint set with extra set of constraints.   
     * @param constraints set to be added to the type application
     */
    protected void extendConstraints(ConstraintSet constraints) {
        this.constraints.addExtraConstraint(constraints);
    }

    @Override
    public String prettyPrint(int fixity) {
        List<Type> chain = this.asFlattenedAppChain();
        Type ftype = chain.remove(0);
        String apptype = ftype.prettyPrintAppChain(fixity, chain);
        return this.constraints.prettyPrintWith(apptype, fixity);
    }

    public final List<Type> asFlattenedAppChain(){
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
	public Type getConcrete() {
		return new TypeApp(this.typeFun.getConcrete(), this.typeArg.getConcrete());
	}

	@Override
    public boolean containsOccurenceOf(TypeVar tvar) {
        return this.typeFun.containsOccurenceOf(tvar) || this.typeArg.containsOccurenceOf(tvar);
    }

    @Override
    public String toString() {
    	if (this.constraints.hasConstraints()) {
    		return String.format("(%s:%s @ %s)", this.constraints, this.typeFun, this.typeArg);
    	} else {
    		return String.format("(%s @ %s)", this.typeFun, this.typeArg);
    	}
    }

}
