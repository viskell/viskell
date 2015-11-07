package nl.utwente.viskell.ghcj;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Evaluator for ghci that pushes code to the ghci environment and can evaluate expressions and return a result.
 */
public class GhciEvaluator extends Evaluator {
    public GhciEvaluator() throws HaskellException {
        super();
    }

    @Override
    protected List<String> getCommand() {
        return ImmutableList.of("ghci", "-ignore-dot-ghci");
    }

    @Override
    protected List<String> getModules() {
        return ImmutableList.of("Data.List", "Data.Maybe", "Data.Either");
    }
}
