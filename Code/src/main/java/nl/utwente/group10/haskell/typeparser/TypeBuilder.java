package nl.utwente.group10.haskell.typeparser;

import nl.utwente.group10.haskell.type.Type;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Turns a String into a Type.
 */
public final class TypeBuilder {
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
        TypeBuilderListener extractor = new TypeBuilderListener();
        walker.walk(extractor, tree);

        return extractor.result();
    }
}
