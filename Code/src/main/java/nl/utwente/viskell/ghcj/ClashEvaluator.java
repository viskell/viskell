package nl.utwente.viskell.ghcj;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Evaluator implementation that talks with CLaSH interactive mode instead of GHCi.
 * http://www.clash-lang.org/
 */
public class ClashEvaluator extends GhciEvaluator {
    public ClashEvaluator() throws HaskellException {
        super();
    }

    @Override
    protected List<String> getCommand() {
        return ImmutableList.of("clash", "--interactive", "-ignore-dot-ghci", "-fno-warn-overlapping-patterns");
    }

    @Override
    protected List<String> getModules() {
        return ImmutableList.of("CLaSH.Prelude", "Data.List", "Data.Maybe", "Data.Either");
    }

}
