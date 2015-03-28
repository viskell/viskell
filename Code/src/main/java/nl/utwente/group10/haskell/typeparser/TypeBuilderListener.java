package nl.utwente.group10.haskell.typeparser;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.TupleT;
import nl.utwente.group10.haskell.type.ListT;
import nl.utwente.group10.haskell.type.ConstT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * ANTLR listener that builds Type instances.
 */
class TypeBuilderListener extends TypeBaseListener {
    /** Temporary storage area for compound types. */
    private final Stack<List<Type>> stack = new Stack<>();

    /** Temporary reference store for variable types. */
    private final HashMap<String, VarT> vars = new HashMap<>();

    /** Build a TypeBuilderListener. */
    protected TypeBuilderListener() {
        this.enter();
    }

    @Override
    public final void exitVariableType(TypeParser.VariableTypeContext ctx) {
        String varName = ctx.getText();

        if (this.vars.containsKey(varName)) {
            this.addParam(this.vars.get(varName));
        } else {
            VarT var = new VarT(varName);
            this.vars.put(varName, var);
            this.addParam(var);
        }
    }

    @Override
    public final void enterFunctionType(TypeParser.FunctionTypeContext ctx) {
        this.enter();
    }

    @Override
    public final void exitFunctionType(TypeParser.FunctionTypeContext ctx) {
        Type[] params = this.popParams();
        this.addParam(new FuncT(params[0], params[1])); // We can do this because the grammer makes sure that a function
                                                        // always has two arguments.
    }

    @Override
    public final void enterTupleType(TypeParser.TupleTypeContext ctx) {
        this.enter();
    }

    @Override
    public final void exitTupleType(TypeParser.TupleTypeContext ctx) {
        this.addParam(new TupleT(this.popParams()));
    }

    @Override
    public final void enterListType(TypeParser.ListTypeContext ctx) {
        this.enter();
    }

    @Override
    public final void exitListType(TypeParser.ListTypeContext ctx) {
        this.addParam(new ListT(this.popParams()[0]));
    }

    @Override
    public final void exitTypeConstructor(TypeParser.TypeConstructorContext ctx) {
        this.addParam(new ConstT(ctx.getText()));
    }

    @Override
    public final void enterConstantType(TypeParser.ConstantTypeContext ctx) {
        this.enter();
    }

    @Override
    public final void exitConstantType(TypeParser.ConstantTypeContext ctx) {
        Type[] types = this.popParams();
        Type[] args = Arrays.copyOfRange(types, 1, types.length);
        this.addParam(new ConstT(types[0].toHaskellType(), args));
    }

    /** Call this when entering a compound (function, list, tuple) type. */
    private void enter() {
        this.stack.push(new ArrayList<>());
    }

    /**
     * Call this when adding a part to a compound type.
     *
     * @param t The type to addParam.
     */
    private void addParam(Type t) {
        this.stack.peek().add(t);
    }

    /**
     * Utility method that pops and converts to an array.
     *
     * @return The topmost stack element as an array of Types.
     */
    private Type[] popParams() {
        List<Type> p = this.stack.pop();
        return p.toArray(new Type[p.size()]);
    }

    /**
     * Checks and returns the parse result.
     *
     * @return The result of the parse.
     */
    public Type result() {
        this.assertTrue(this.stack.size() == 1);
        return this.stack.pop().get(0);
    }

    /**
     * Version of assert that also works when Java assertions are off.
     * @param condition This should be true.
     */
    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new RuntimeException("assertTrue failed while treebuilding");
        }
    }
}
