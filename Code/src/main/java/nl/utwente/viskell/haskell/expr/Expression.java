package nl.utwente.viskell.haskell.expr;

import com.google.common.collect.ImmutableList;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;

import java.util.List;

/**
 * An expression in Haskell.
 */
public abstract class Expression {
    /**
     * Analyzes the type tree and infers the type for this usage of this expression
     *
     * @return The type for this usage of this expression.
     * @throws HaskellException The type tree contains an application of an incompatible type.
     */
    public abstract Type inferType() throws HaskellTypeError; 

    /**
     * Returns the Haskell code for this expression.
     * @return The Haskell code for this expression.
     */
    public abstract String toHaskell();

    /**
     * @return A string representation of this Haskell expression.
     */
    @Override
    public abstract String toString();

    /**
     * @return a list of subexpressions, if any, or else an empty list.
     */
    public List<Expression> getChildren() {
        return ImmutableList.of();
    }
}
