package nl.utwente.group10.ui.components.blocks;

import com.google.common.collect.ImmutableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Cond;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;

import java.util.List;

public class CondBlock extends Block implements InputBlock, OutputBlock {
    @FXML private Pane anchorSpace;
    @FXML private Pane outputSpace;

    private InputAnchor cond, t, f;
    private OutputAnchor out;

    public CondBlock(CustomUIPane parent) {
        super(parent);
        loadFXML("CondBlock");

        cond = new InputAnchor(this, parent);
        t = new InputAnchor(this, parent);
        f = new InputAnchor(this, parent);

        out = new OutputAnchor(this, parent);

        anchorSpace.getChildren().setAll(cond, t, f);
        outputSpace.getChildren().setAll(out);
    }

    @Override
    public Expr asExpr() {
        return new Cond(cond.asExpr(), t.asExpr(), f.asExpr());
    }

    /* CondBlock-specific code ends here. */

    @Override
    public Type getInputSignature(InputAnchor input) {
        if (input.equals(cond)) return new ConstT("Bool");
        else return HindleyMilner.makeVariable();
    }

    @Override
    public Type getInputSignature(int index) {
        if (index == 0) return new ConstT("Bool");
        else return HindleyMilner.makeVariable();
    }

    @Override
    public Type getInputType(InputAnchor input) {
        // TODO not sure what to return here
        return HindleyMilner.makeVariable();
    }

    @Override
    public Type getInputType(int index) {
        // TODO not sure what to return here
        return HindleyMilner.makeVariable();
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(cond, t, f);
    }

    @Override
    public List<InputAnchor> getActiveInputs() {
        // TODO Not that useful, can't partially apply an if
        return getAllInputs();
    }

    @Override
    public OutputAnchor getOutputAnchor() {
        return out;
    }

    @Override
    public Type getOutputType() {
        // TODO do we need to support this?
        return getOutputType(new Env());
    }

    @Override
    public Type getOutputType(Env env) {
        // TODO not sure if this is the right place to call analyze()

        try {
            return asExpr().analyze(env).prune();
        } catch (HaskellException e) {
            return getOutputSignature();
        }
    }


    @Override
    public Type getOutputSignature() {
        // TODO not sure what to return here
        return HindleyMilner.makeVariable();
    }

    @Override
    public Type getOutputSignature(Env env) {
        // TODO not sure what to return here
        return HindleyMilner.makeVariable();
    }
}
