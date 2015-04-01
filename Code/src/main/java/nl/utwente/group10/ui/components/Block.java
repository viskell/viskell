package nl.utwente.group10.ui.components;

import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.gestures.GestureCallBack;
import nl.utwente.group10.ui.gestures.UIEvent;

import java.io.IOException;

/**
 * Base UI Component that other visual elements will extend from. If common functionality is found it should be
 * refactored to here.
 */
public abstract class Block extends StackPane implements GestureCallBack {
	/** The output of this Block. */
	private OutputAnchor output;

	/** The fxmlLoader responsible for loading the fxml of this Block. */
	private FXMLLoader fxmlLoader;

	/** The pane that is used to hold state and place all components on. */
	private CustomUIPane parentPane;

	/**
	 * @param blockName Name of this block. The name is used to load the FXML definition for this block.
	 * @param pane The pane this block belongs to.
	 * @throws IOException when the blo
	 */
	public Block(String blockName, CustomUIPane pane) throws IOException {
		fxmlLoader = new FXMLLoader(getClass().getResource(String.format("/ui/%s.fxml", blockName)));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		output = new OutputAnchor(this, pane);
		parentPane = pane;

		parentPane.selectedBlockProperty().addListener(event -> {
			if (parentPane.getSelectedBlock().isPresent() && this.equals(parentPane.getSelectedBlock().get())) {
				this.getStyleClass().add("selected");
			} else {
				this.getStyleClass().removeAll("selected");
			}
		});
	}

	/**
	 * @return The FXMLLoader used by this Block.
	 */
	public final FXMLLoader getLoader(){
		return fxmlLoader;
	}

	/**
	 * @return the output Anchor for this Block
	 */
	public final OutputAnchor getOutputAnchor() {
		return output;
	}

	@Override
	public void handleCustomEvent(UIEvent event) {
		EventType eventType = event .getEventType();

		if (eventType.equals(UIEvent.TAP)) {
			parentPane.setSelectedBlock(this);
		} else if (eventType.equals(UIEvent.TAP_HOLD)) {
			//TODO: open the quick-menu
		}
	}

	/**
	 * @return an expression that evaluates to what this block is.
	 */
	public abstract Expr asExpr();
}
