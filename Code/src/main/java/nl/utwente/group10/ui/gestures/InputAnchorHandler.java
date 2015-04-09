package nl.utwente.group10.ui.gestures;

import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.InputAnchor;
import nl.utwente.group10.ui.components.OutputAnchor;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class InputAnchorHandler implements EventHandler<InputEvent>{

	private CustomUIPane cpane;
	private InputAnchor anchor;
	
	public InputAnchorHandler(InputAnchor anchor, CustomUIPane cpane) {
		this.cpane = cpane;
		this.anchor = anchor;
		anchor.addEventFilter(MouseEvent.ANY, this);
		anchor.addEventFilter(MouseDragEvent.ANY, this);
		anchor.addEventFilter(TouchEvent.ANY, this);
	}
	
	@Override
	public void handle(InputEvent event) {
		if(event instanceof MouseDragEvent){
			MouseDragEvent mdEvent = ((MouseDragEvent) event);
			if(mdEvent.getEventType().equals(MouseDragEvent.MOUSE_DRAG_RELEASED)){
				if(mdEvent.getGestureSource() instanceof OutputAnchor){
					anchor.createConnectionFrom((OutputAnchor)mdEvent.getGestureSource());
					cpane.invalidate();
				}
			}
		}
		event.consume();	
	}

}
