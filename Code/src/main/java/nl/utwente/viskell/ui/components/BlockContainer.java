package nl.utwente.viskell.ui.components;

public interface BlockContainer {

    /** Set fresh types in all anchors of this lambda for the next typechecking cycle. */
    public void refreshAnchorTypes();

    /**
     * Handle the expression and types changes caused by modified connections or values.
     * Also propagate the changes through internal connected blocks, and then outwards.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    public void handleConnectionChanges(boolean finalPhase);

}