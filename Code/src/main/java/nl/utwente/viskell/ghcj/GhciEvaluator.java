package nl.utwente.viskell.ghcj;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Evaluator implementation that uses GHCi, the Glasgow Haskell Compiler's
 * interactive mode, for evaluating Haskell expressions.
 */
public class GhciEvaluator extends Evaluator {
    public GhciEvaluator() throws HaskellException {
        super();
    }

    @Override
    protected List<String> getCommand() {
        return ImmutableList.of("ghci", "-ignore-dot-ghci", "-fno-warn-overlapping-patterns");
    }

    @Override
    protected List<String> getModules() {
        return ImmutableList.of("Data.List", "Data.Maybe", "Data.Either", "Data.Monoid", "Control.Applicative", "Test.QuickCheck");
    }

    @Override
    protected String getCatalogPath() {
        return "/catalog/haskell.xml";
    }
}
