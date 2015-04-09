package nl.utwente.group10.ui.gestures;

import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.Connection;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.ConnectionLine;
import nl.utwente.group10.ui.components.InputAnchor;
import nl.utwente.group10.ui.components.OutputAnchor;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class OutputAnchorHandler implements EventHandler<InputEvent>{

	private ConnectionLine line;
	private CustomUIPane cpane;
	private ConnectionAnchor anchor;
	
	public OutputAnchorHandler(ConnectionAnchor anchor, CustomUIPane cpane) {
		this.cpane = cpane;
		this.anchor = anchor;
		anchor.addEventFilter(MouseEvent.ANY, this);
		anchor.addEventFilter(MouseDragEvent.ANY, this);
		anchor.addEventFilter(TouchEvent.ANY, this);
	}	
	
	@Override
	public void handle(InputEvent event) {
		if(event instanceof MouseEvent){
			MouseEvent mEvent = ((MouseEvent) event);
			if(event.getEventType().equals(MouseEvent.DRAG_DETECTED)){
				anchor.startFullDrag();
				Point2D point = anchor.localToScene(anchor.getCenterX(),anchor.getCenterY());
				createLine(point.getX(),point.getY());
			}else if(event.getEventType().equals(MouseEvent.MOUSE_DRAGGED) && line!=null){
				updateLine(mEvent.getSceneX(),mEvent.getSceneY());
			}else if(event.getEventType().equals(MouseEvent.MOUSE_RELEASED) && line!=null){
				finalizeLine();
			}
			if(event instanceof MouseDragEvent){
				System.out.println(event.getEventType());
			}
		}
		event.consume();
	}
	
	
	private void createLine(double x, double y){
		line = cpane.createLine();
		line.setMouseTransparent(true);
		line.setStartPosition(x, y);
		line.setEndPosition(x, y);
	}
	
	private void updateLine(double x, double y){
		line.setEndPosition(x, y);
	}
	
	private void finalizeLine(){
		line.setMouseTransparent(false);
		cpane.getChildren().remove(line);
		line = null;
	}

}
