package nl.utwente.viskell.ghcj;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Evaluator for ghci that pushes code to the ghci environment and can evaluate expressions and return a result.
 */
public class ClashEvaluator extends PipeEvaluator {
    public ClashEvaluator() throws HaskellException {
        super();
    }

    @Override
    protected List<String> getCommand() {
        return ImmutableList.of("clash", "--interactive", "-ignore-dot-ghci");
    }

    @Override
    protected List<String> getModules() {
        return ImmutableList.of("CLaSH.Prelude", "Data.List", "Data.Maybe", "Data.Either");
    }
}
