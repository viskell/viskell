package nl.utwente.viskell.haskell.type;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.ImmutableList;


/**
 * Variable type.
 */
public class TypeVar extends Type {

    /**
     * An optional mutable reference to a concrete type.
     */
    protected final static class TypeInstance {
        /**
         * The textual representation of the type variable.
         */
        private final String name;

        /**
         * Whether this type variable was created internally in typechecking process, and preferably should not be shown to the user.
         */
        private final boolean internal;

        /**
         * Whether this type variable is rigid (meaning that it can't be unified with a more specific or concrete type)
         */
        private boolean isRigid;
        
        /**
         * A concrete instantiated type or null.
         */
        private ConcreteType type;

        /**
         * The type constraints for this instance.
         */
        private ConstraintSet constraints;

        /**
         * List of type applications that needs their constraints checked once this variable is instantiated.
         */
        private List<TypeApp> associatedTypeApps;
        
        /**
         * Internal List of all type variables that refer to this instance.
         * this list contains the creating type variable as well as the variables have unified with this.
         * It is using WeakReference to avoid holding on to too much old type variables. 
         */
        private LinkedList<WeakReference<TypeVar>> unifiedVars;

        /**
         * @param name The textual representation of the type variable.
         * @param internal whether this an internally generated type variable
         * @param type The concrete instance of this type, might be null.
         * @param constraints The set of constraints for this type.
         */
        private TypeInstance(TypeVar creator, String name, boolean internal, boolean isRigid, ConcreteType type, final ConstraintSet constraints) {
            this.name = name;
            this.internal = internal;
            this.isRigid = isRigid;
            this.type = type;
            this.constraints = constraints;
            this.unifiedVars = new LinkedList<>();
            this.unifiedVars.add(new WeakReference<>(creator));
            this.associatedTypeApps = new ArrayList<>();
        }

        
        /**
         *  Make this type variable rigid
         * @throws IllegalStateException when is isPresent() is true
         */
        protected void makeRigid() {
            if (this.type != null) {
                throw new IllegalStateException("Type instance already set");
            }

            this.isRigid = true;
        }
        
        /**
         * @return Whether there is a concrete type.
         */
        private boolean isPresent() {
            return this.type != null;
        }

        /**
         * @throws NullPointerException when isPresent() is false
         * @return The concrete type instance
         */
        private ConcreteType get() {
            if (this.type == null) {
                throw new NullPointerException("Getting invalid type instance");
            }

            return this.type;
        }

        /**
         * @throws IllegalStateException when is isPresent() is true
         * @throws HaskellTypeError if this type variable is rigid
         * @param The new instance of this type.
         */
        private void set(ConcreteType ctype) throws HaskellTypeError {
            if (this.type != null) {
                throw new IllegalStateException("Type instance already set");
            }
            
            if (this.isRigid) {
                throw new HaskellTypeError("Can not unify a rigid type variable " + this.name + " with concrete type " + ctype.prettyPrint());
            }

            this.type = ctype;
            
            // now the type variable is instantiated we need check all deferred typeapp constraints
            for (TypeApp tapp : this.associatedTypeApps) {
            	TypeChecker.satisfyConstraints(tapp, this.constraints, "typeapp constraint with " + this.getName());
            	// once satisfied the constraints are not needed anymore
            	tapp.clearConstraints();
            }
            this.associatedTypeApps = ImmutableList.of();
        }

        /**
         * Deeply unifying all aspects of type instances
         * @param the other type instance.
         * @throws HaskellTypeError if the combined constraint set of the typevar instances is not satisfiable.
         * @throws HaskellTypeError if a rigid type variable gets unified with more constraints or another rigid type variable
         */
        private void unifyWith(TypeInstance other) throws HaskellTypeError {
            if (this == other) {
                return;
            }
            
            if (this.isRigid && other.isRigid) {
                throw new HaskellTypeError("Can not unify a rigid type variable " + this.name + " with another rigid type variable " + other.name);
            }
            
            // if one of the type variables is rigid, first check that the constraints are identical
            if ((this.isRigid || other.isRigid) && ! this.constraints.equals(other.constraints)) {
                throw new HaskellTypeError("Can not add extra constraints to a rigid type variable " + this.name);
            }
            
            other.constraints.mergeConstraintsWith(this.constraints);
            other.associatedTypeApps.addAll(this.associatedTypeApps);
            
            other.unifiedVars.addAll(this.unifiedVars);
            // Go through all type variable associated with both type instances, to make sure all of them are unified to the same instance.
            for (ListIterator<WeakReference<TypeVar>> iter = this.unifiedVars.listIterator(); iter.hasNext();) {
                TypeVar referer = iter.next().get();
                if (referer == null) {
                    iter.remove();
                } else {
                    referer.instance = other;
                }
            }
        }

        /**
         * @return The textual representation of the type variable.
         */
        private String getName() {
            return this.name;
        }
        
        /**
         * @param The fixity of the context the type is shown in.
         * @return The readable representation of this type for in the UI.
         */
        private final String prettyPrint(final int fixity) {
            if (this.type != null) {
                return this.type.prettyPrint(fixity);
            }

            return this.constraints.prettyPrintWith(this.getName(), fixity);
        }
    }

    /**
     * The reference to the potential concrete instance for this type.
     */
    private TypeInstance instance;

    /**
     * @param name The textual representation of the type variable.
     * Identifiers are not used in the type checking progress, 
     * different {@code TypeVar} instances with the same name are not equal.
     * @param internal whether this an internally generated type variable
     */
    public TypeVar(final String name, final boolean internal) {
        this(name, internal, false, new ConstraintSet(), null);
    }

    /**
     * @param name The textual representation of the type variable.
     * Identifiers are not used in the type checking progress, 
     * different {@code TypeVar} instances with the same name are not equal.
     * @param internal whether this an internally generated type variable
     * @param constraints The set of constraints for this type.
     * @param instance The concrete instance of this type, might be null.
     */
    private TypeVar(final String name, final boolean internal, final boolean isRigid, final ConstraintSet constraints, final ConcreteType type) {
        this.instance = new TypeInstance(this, name.toLowerCase(), internal, isRigid, type, constraints);
    }

    /**
     * @return The name of this variable type.
     */
    public final String getName() {
        return this.instance.getName();
    }

    /**
     * @return Whether this type variable has been instantiated with a concrete type.
     */
    public final boolean hasConcreteInstance() {
        return this.instance.isPresent();
    }

    /**
     * @throws NullPointerException when hasInstance() is false
     * @return The concrete type this type variable has been instantiated with.
     */
    public final ConcreteType getInstantiatedType() {
        return this.instance.get();
    }

    /*
    * @throws IllegalStateException when hasInstance() is true
   * @throws HaskellTypeError if this type variable is rigid
    * @param The concrete type this type variable is unified with
    */
    public final void setConcreteInstance(ConcreteType type) throws HaskellTypeError {
        this.instance.set(type);
    }

    /**
     * Use the same type instance for both type variable, effectively unifying them.
     * @param the other type variable.
     * @throws HaskellTypeError if the combined constraint set of the type variables is not satisfiable. 
     */
    public final void unifyWith(TypeVar other) throws HaskellTypeError {
        if (this.instance.isRigid) {
            other.instance.unifyWith(this.instance);
        } else if (other.instance.isRigid || this.instance.internal) {
            this.instance.unifyWith(other.instance);
        } else {
            other.instance.unifyWith(this.instance);
        }
    }

    /**
     * @return The set of type class constraints associated with this type variable
     */
    ConstraintSet getConstraints() {
        return this.instance.constraints;
    }

    /**
     * Extends the constraint set with extra type class.   
     * @param typeClass to be added to this type variable
     */
    protected void introduceConstraint(TypeClass typeClass) {
        this.instance.constraints.addExtraConstraint(typeClass);
    }

    /**
     * Extends the constraint set with extra type class.   
     * @param constraints set to be added to this type variable
     */
    protected void introduceConstrainst(ConstraintSet constraints) {
        this.instance.constraints.addExtraConstraint(constraints);
    }

    /**
     * Adds a type application that requires constraint checking once this type variable gets instantiated
     * @param typeapp to add
     */
	protected void addConstrainedTypeApp(TypeApp typeapp) {
		if (! this.instance.associatedTypeApps.contains(typeapp)) {
			this.instance.associatedTypeApps.add(typeapp);
		}
	}

    @Override
    public final String prettyPrint(final int fixity) {
        return this.instance.prettyPrint(fixity);
    }

    @Override
    protected String prettyPrintAppChain(int fixity, List<Type> args) {
        if (this.instance.isPresent()) {
            return this.instance.get().prettyPrintAppChain(fixity, args);
        }
        
        return super.prettyPrintAppChain(fixity, args);
    }

    @Override
    public Type getFresh(TypeScope scope) {
        if (this.instance.isPresent()) {
            return this.instance.get().getFresh();
        }

        return scope.pickFreshTypeVar(this);
    }
    
    /**
     * This internal method should only be called from TypeScope
     * @param staleToFresh The mapping between known type instances and their related fresh type variables.
     * @return A refreshed type variable.
     */
    protected TypeVar pickFreshTypeVarInstance(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) {
        if (staleToFresh.containsKey(this.instance)) {
            return staleToFresh.get(this.instance);
        }
        
        if (this.instance.isRigid) {
            //FIXME this is a ugly workaround to make to rigid typevars unify with fresh copies of themselves
            //TODO remove this special case once type scoping is dealt with properly for whole lambdas
            this.instance.unifiedVars = new LinkedList<>();
            this.instance.unifiedVars.add(new WeakReference<>(this));
            return this;
        }

        TypeVar fresh = new TypeVar(this.instance.name, this.instance.internal, this.instance.isRigid, this.instance.constraints.clone(), null);
        staleToFresh.put(this.instance, fresh);
        return fresh;
       
    }

    @Override
    public boolean containsOccurenceOf(TypeVar tvar) {
        // If type variable share the same instance then they have been unified to a single one.
        if (this.instance == tvar.instance) {
            return true;
        }

        if (!this.instance.isPresent()) {
            return false;
        }

        return this.instance.get().containsOccurenceOf(tvar);
    }

    @Override
    public final String toString() {
        String constr = this.instance.constraints.toString();
        String fmt = this.instance.isRigid ? "forall %s.(%s)%s" : "%s(%s)%s";
        String tmp = String.format(fmt, this.getName(), Integer.toHexString(this.instance.hashCode()), constr);
        return this.instance.isPresent() ? tmp + ":" + this.instance.get().toString() : tmp;
    }

    /**
     * This method simply checks whether the reference of instance in the type variables are the same.
     * The uniqueness of type variable depends on this because the meaning of the name is context sensitive.
     * This is why we leave the task of using the right type variable to the programmer that implements this type system
     * - there would be no clear and easy way of doing this automagically.
     *
     * @param obj The object to compare with.
     * @return Whether the given object is equal to this object.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeVar)) {
            return false;
        }

        TypeVar other = (TypeVar) obj;
        return this.instance == other.instance;
    }

}
