package nl.utwente.group10.ui;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;

public class BackendUtils {
    
    /**
     * The Env name for an invalid expression.
     */
	//TODO make this prettier
	public static final String EXPR_INVALID = "invalid";
	
	
	/**
	 * @param steps
	 *            Amount of steps to dive
	 * @return The First argument in this function with steps more depth than this
	 *         function.
	 *         
	 *         example:
	 *         (a -> (b -> (c -> d))).dive(2) = b
	 *         
	 *         If it is impossible to dive the amount of steps provided, it
	 *         returns the closest result to that.
	 */
	public static Type dive(ConstT function, int steps) {
		if(steps==1){
			return function.getArgs()[0];
		}else if(steps > 1){		
			Type next = function.getArgs()[1];
			if (steps > 1) {
				if (next instanceof ConstT) {
					return dive(((ConstT) next),steps - 1);
				}
			}
		}
		return function;
	}
	
	/**
	 * An invalid Expression is equal to an Expression that does nothing.
	 * @return
	 */
	public static Expr getInvalidExpression(){
		return new Ident(EXPR_INVALID);
	}
	
	/**
	 * @param expr
	 * @return True if the given expression is not invalid.
	 */
    public static final boolean isValidExpression(Expr expr){
    	return !expr.toHaskell().equals(EXPR_INVALID);
    }
	
}
