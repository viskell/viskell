package nl.utwente.viskell.haskell.type;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

public final class ConstraintSet {

    /**
     * A set of type class constraints belonging to a single type object.
     */
    TreeSet<TypeClass> constraints;

    public ConstraintSet() {
        this.constraints = new TreeSet<>();
    }

    public ConstraintSet(TreeSet<TypeClass> constraints) {
        this.constraints = constraints;
    }

    /**
     * @return Whether this constraint set is not empty.
     */
    public boolean hasConstraints() {
        return ! this.constraints.isEmpty();
    }
    
    /**
     * @param tc the type class to extend this constraint set with
     */
    protected void addExtraConstraint(TypeClass tc) {
        this.constraints.add(tc);
        this.simplifyConstraints();
    }

    /**
     * @param extras additional constraint set to extend this constraint set with
     */
    protected void addExtraConstraint(ConstraintSet extras) {
        this.constraints.addAll(extras.constraints);
        this.simplifyConstraints();
    }
    
    /**
     * Checks whether the given type is within the constraints. 
     * If the set of constraints is empty, every type is within the constraints.
     * 
     * @param type The type to check.
     * @return Whether the given type is within the constraints of this type.
     */
    protected boolean allConstraintsMatch(TypeCon con) {
        return this.constraints.stream().allMatch(c -> c.hasType(con));
    }

    /**
     * @param con the type constructor to check.
     * @param arity number of arguments to get the constraints for.
     * @return A arity long list of constraints set that are required by instances that match the type constructor.
     */
    protected List<ConstraintSet> getImpliedArgConstraints(TypeCon con, int arity) {
        List<ConstraintSet> results = new ArrayList<>(arity);
        for (int i = 0; i < arity; i++) {
            results.add(new ConstraintSet());
        }
        
        for (TypeClass typeClass : this.constraints) {
            int n = typeClass.lookupConstrainedArgs(con);
            for (int i = 0; i < n; i++) {
                results.get(i).addExtraConstraint(typeClass);
            }
        }
        
        return results;
    }
    
    /**
     * simplify the constraint set by removing super class implications
     */
    private void simplifyConstraints() {
        if (this.constraints.size() <= 1) {
            return;
        }
        
        Set<TypeClass> allSupers = new TreeSet<>();
        for (TypeClass tc : this.constraints) {
            allSupers.addAll(tc.getSupers());
        }
    
        this.constraints = new TreeSet<>(Sets.difference(this.constraints, allSupers));
        return;
    }
    
    /**
     * Merge this constrain set with another, while also simplifying and checking satisfiability.
     * @param other constraint set to merge with
     * @throws HaskellTypeError if the combined constraint set is not satisfiable.
     */
    protected void mergeConstraintsWith(ConstraintSet other) throws HaskellTypeError {
        this.constraints = new TreeSet<>(Sets.union(this.constraints, other.constraints));
        this.simplifyConstraints();
        this.checkSatisfiable();
    }

    /**
     * Check if type constructors exist that can satisfy all the constraint in this set.  
     * @throws HaskellTypeError if this constraint set is not satisfiable.
     * 
     */
    private void checkSatisfiable() throws HaskellTypeError {
        if (this.constraints.size() <= 1) {
            return;
        }
        
        if (this.constraints.stream().map(TypeClass::allInstanceTypeCons).reduce(Sets::intersection).get().isEmpty()) {
            throw new HaskellTypeError("no known type constructor satisfies all of " + this.toString());
        }
    }

    protected Optional<ConcreteType> tryGetDefaulted() {
        Set<TypeClass> classes = this.constraints;
        // search through the type classes for a suitable default
        while (!classes.isEmpty()) {
            for (TypeClass tc : classes) {
                if (tc.getDefaultType().isPresent()) {
                    TypeCon def = tc.getDefaultType().get();
                    if (this.allConstraintsMatch(def)) {
                        return Optional.of(def);
                    }
                }
            }
            // fall back on super classes
            classes = classes.stream().flatMap(tc -> tc.getSupers().stream()).collect(Collectors.toSet());
        }

        return Optional.empty();
    }
    
    /**
     * @param typeText the String representation of type being constrained 
     * @param The fixity of the context the type is shown in.
     * @return The readable representation of this type for in the UI.
     */
    public String prettyPrintWith(String typeText, final int fixity) {
        if (this.constraints.isEmpty()) {
            return typeText;
        } else if (fixity < 9 && this.constraints.size() == 1) {
            return this.constraints.iterator().next().getName() + " " + typeText;
        } else {
            final StringBuilder out = new StringBuilder();
            out.append("(");

            int i = 0;
            for (TypeClass tc : this.constraints) {
                out.append(tc.getName());
                if (i + 1 < this.constraints.size()) {
                    out.append("+");
                }

                i++;
            }

            out.append(" ");
            out.append(typeText);
            out.append(")");
            return out.toString();
        }
    }
    
    @Override
    public ConstraintSet clone() {
        return new ConstraintSet(new TreeSet<>(this.constraints));
    }

    @Override
    public String toString() {
        return Arrays.toString(this.constraints.stream().map(c -> c.getName()).toArray());
    }

    @Override
    public boolean equals(Object other) {
        if (! (other instanceof ConstraintSet)) {
            return false;
        }
        
        return this.constraints.equals(((ConstraintSet)other).constraints);
    }

}
