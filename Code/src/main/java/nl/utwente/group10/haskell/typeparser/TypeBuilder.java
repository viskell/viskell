package nl.utwente.group10.haskell.typeparser;

import nl.utwente.group10.haskell.type.Type;

import nl.utwente.group10.haskell.type.TypeClass;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Map;
import java.util.Optional;

/**
 * Turns a String into a Type.
 */
public final class TypeBuilder {
    /** Map of available type classes. */
    private Optional<Map<String, TypeClass>> typeClasses;

    /**
     * @param typeClasses The available type classes.
     */
    public TypeBuilder(Map<String, TypeClass> typeClasses) {
        this.typeClasses = Optional.ofNullable(typeClasses);
    }

    public TypeBuilder() {
        this(null);
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
        TypeBuilderListener extractor;

        if (this.typeClasses.isPresent()) {
            extractor = new TypeBuilderListener(this.typeClasses.get());
        } else {
            extractor = new TypeBuilderListener();
        }

        walker.walk(extractor, tree);

        return extractor.result();
    }
}
