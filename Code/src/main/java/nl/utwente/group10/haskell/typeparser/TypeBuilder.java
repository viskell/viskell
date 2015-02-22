package nl.utwente.group10.haskell.typeparser;

import nl.utwente.group10.haskell.type.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Turns a String into a Type.
 */
public final class TypeBuilder {
    protected class TypeBuilderListener extends TypeBaseListener {
        /** Temporary storage area for compound types. */
        private final Stack<List<Type>> stack = new Stack<>();

        /** Build a TypeBuilderListener. */
        protected TypeBuilderListener() {
            this.enter();
        }

        @Override
        public final void exitVariableType(@NotNull TypeParser.VariableTypeContext ctx) {
            this.push(new VarT(ctx.getText()));
        }

        @Override
        public final void enterFunctionType(@NotNull TypeParser.FunctionTypeContext ctx) {
            this.enter();
        }

        @Override
        public final void exitFunctionType(@NotNull TypeParser.FunctionTypeContext ctx) {
            this.push(new FuncT(this.params()));
        }

        @Override
        public final void enterTupleType(@NotNull TypeParser.TupleTypeContext ctx) {
            this.enter();
        }

        @Override
        public final void exitTupleType(@NotNull TypeParser.TupleTypeContext ctx) {
            this.push(new TupleT(this.params()));
        }

        @Override
        public final void enterListType(@NotNull TypeParser.ListTypeContext ctx) {
            this.enter();
        }

        @Override
        public final void exitListType(@NotNull TypeParser.ListTypeContext ctx) {
            this.push(new ListT(this.params()[0]));
        }

        @Override
        public final void exitConstantType(@NotNull TypeParser.ConstantTypeContext ctx) {
            this.push(new ConstT(ctx.getText()));
        }

        private void enter() {
            this.stack.push(new ArrayList<Type>());
        }

        private void push(Type t) {
            this.stack.peek().add(t);
        }

        private Type[] params() {
            List<Type> p = this.stack.pop();
            return p.toArray(new Type[p.size()]);
        }

        public Type result() {
            this.check(this.stack.size() == 1);
            return this.stack.pop().get(0);
        }

        private void check(boolean condition) {
            if (!condition) {
                throw new RuntimeException("check() failed while building tree");
            }
        }
    }

    public TypeBuilder() {
    }

    /**
     * Parse a Haskell type declaration into a TypeT instance.
     *
     * @param hs The Haskell type declaration
     * @return Type
     */
    public Type build(final String hs) {
        TypeLexer lexer = new TypeLexer(new ANTLRInputStream(hs));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TypeParser parser = new TypeParser(tokens);

        ParserRuleContext tree = parser.type();
        ParseTreeWalker walker = new ParseTreeWalker();
        TypeBuilder.TypeBuilderListener extractor = new TypeBuilderListener();
        walker.walk(extractor, tree);

        return extractor.result();
    }
}
