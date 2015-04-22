package nl.utwente.group10.haskell.typeparser;

import java.util.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nl.utwente.group10.haskell.type.*;

/**
 * ANTLR listener that builds Type instances.
 */
class TypeBuilderListener extends TypeBaseListener {
    /** Temporary storage area for compound types. */
    private final Stack<List<Type>> stack = new Stack<>();

    /** Temporary reference store for variable types. */
    private final HashMap<String, VarT> vars = new HashMap<>();

    /** Temporary reference store for type classes. */
    private final Multimap<String, TypeClass> constraints = HashMultimap.create();

    /** Temporary store for the last type class. */
    private Optional<TypeClass> typeClass = Optional.empty();

    /** The available type classes. */
    private final Map<String, TypeClass> typeClasses;

    /**
     * Build a TypeBuilderListener.
     * @param typeClasses The available type classes.
     */
    protected TypeBuilderListener(Map<String, TypeClass> typeClasses) {
        this.typeClasses = typeClasses;
        this.enter();
    }

    /** Build a TypeBuilderListener without type class support. */
    protected TypeBuilderListener() {
        this(new HashMap<>());
    }

    @Override
    public void exitTypeClasses(TypeParser.TypeClassesContext ctx) {
        for (String varName : this.constraints.keySet()) {
            vars.put(varName, new VarT(varName, (Set<TypeClass>) this.constraints.get(varName), null));
        }
    }

    @Override
    public void exitClassedType(TypeParser.ClassedTypeContext ctx) {
        if (this.typeClass.isPresent()) {
            this.constraints.put(ctx.getText(), this.typeClass.get());
        }
    }

    @Override
    public void exitTypeClass(TypeParser.TypeClassContext ctx) {
        this.typeClass = Optional.ofNullable(this.typeClasses.getOrDefault(ctx.getText(), null));
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
