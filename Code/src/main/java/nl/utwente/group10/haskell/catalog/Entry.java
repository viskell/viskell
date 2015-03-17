package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.expr.EnvFunc;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

/** A function entry in the Haskell catalog. */
public class Entry {
    private final String name;
    private final String category;
    private final String signature;
    private final String documentation;

    Entry(String name, String category, String signature, String documentation) {
        this.name = name;
        this.category = category;
        this.signature = signature;
        this.documentation = documentation;
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getDocumentation() {
        return this.documentation;
    }

    public EnvFunc asEnvFunc() {
        TypeBuilder tb = new TypeBuilder();
        return new EnvFunc(this.name, tb.buildFuncT(this.signature));
    }
}
