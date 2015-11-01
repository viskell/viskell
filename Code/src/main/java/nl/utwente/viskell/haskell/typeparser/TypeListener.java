// Generated from nl/utwente/viskell/haskell/typeparser/Type.g4 by ANTLR 4.5
package nl.utwente.viskell.haskell.typeparser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TypeParser}.
 */
public interface TypeListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TypeParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(TypeParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(TypeParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#innerType}.
	 * @param ctx the parse tree
	 */
	void enterInnerType(TypeParser.InnerTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#innerType}.
	 * @param ctx the parse tree
	 */
	void exitInnerType(TypeParser.InnerTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#functionType}.
	 * @param ctx the parse tree
	 */
	void enterFunctionType(TypeParser.FunctionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#functionType}.
	 * @param ctx the parse tree
	 */
	void exitFunctionType(TypeParser.FunctionTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#compoundType}.
	 * @param ctx the parse tree
	 */
	void enterCompoundType(TypeParser.CompoundTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#compoundType}.
	 * @param ctx the parse tree
	 */
	void exitCompoundType(TypeParser.CompoundTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#appliedType}.
	 * @param ctx the parse tree
	 */
	void enterAppliedType(TypeParser.AppliedTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#appliedType}.
	 * @param ctx the parse tree
	 */
	void exitAppliedType(TypeParser.AppliedTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#tupleType}.
	 * @param ctx the parse tree
	 */
	void enterTupleType(TypeParser.TupleTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#tupleType}.
	 * @param ctx the parse tree
	 */
	void exitTupleType(TypeParser.TupleTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#listType}.
	 * @param ctx the parse tree
	 */
	void enterListType(TypeParser.ListTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#listType}.
	 * @param ctx the parse tree
	 */
	void exitListType(TypeParser.ListTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#parenType}.
	 * @param ctx the parse tree
	 */
	void enterParenType(TypeParser.ParenTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#parenType}.
	 * @param ctx the parse tree
	 */
	void exitParenType(TypeParser.ParenTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#constantType}.
	 * @param ctx the parse tree
	 */
	void enterConstantType(TypeParser.ConstantTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#constantType}.
	 * @param ctx the parse tree
	 */
	void exitConstantType(TypeParser.ConstantTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#typeConstructor}.
	 * @param ctx the parse tree
	 */
	void enterTypeConstructor(TypeParser.TypeConstructorContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#typeConstructor}.
	 * @param ctx the parse tree
	 */
	void exitTypeConstructor(TypeParser.TypeConstructorContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#variableType}.
	 * @param ctx the parse tree
	 */
	void enterVariableType(TypeParser.VariableTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#variableType}.
	 * @param ctx the parse tree
	 */
	void exitVariableType(TypeParser.VariableTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#typeClasses}.
	 * @param ctx the parse tree
	 */
	void enterTypeClasses(TypeParser.TypeClassesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#typeClasses}.
	 * @param ctx the parse tree
	 */
	void exitTypeClasses(TypeParser.TypeClassesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#typeWithClass}.
	 * @param ctx the parse tree
	 */
	void enterTypeWithClass(TypeParser.TypeWithClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#typeWithClass}.
	 * @param ctx the parse tree
	 */
	void exitTypeWithClass(TypeParser.TypeWithClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#classedType}.
	 * @param ctx the parse tree
	 */
	void enterClassedType(TypeParser.ClassedTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#classedType}.
	 * @param ctx the parse tree
	 */
	void exitClassedType(TypeParser.ClassedTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#typeClass}.
	 * @param ctx the parse tree
	 */
	void enterTypeClass(TypeParser.TypeClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#typeClass}.
	 * @param ctx the parse tree
	 */
	void exitTypeClass(TypeParser.TypeClassContext ctx);
}