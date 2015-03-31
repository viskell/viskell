package nl.utwente.group10.ui.gestures;

import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ConnectionLine;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class DragFromGesture extends AbstractGesture implements EventHandler<InputEvent>{

	private ConnectionLine line;
	private CustomUIPane cpane;
	
	public DragFromGesture(Node latchTo, CustomUIPane cpane) {
		super(latchTo);
		this.cpane = cpane;
	}
	
	
	@Override
	public void handle(InputEvent event) {
		if(event instanceof MouseEvent){
			MouseEvent mEvent = ((MouseEvent) event);
			if(event.getEventType().equals(MouseEvent.DRAG_DETECTED)){
				createLine(mEvent.getSceneX(),mEvent.getSceneY());
			}else if(event.getEventType().equals(MouseEvent.MOUSE_DRAGGED) && line!=null){
				updateLine(mEvent.getSceneX(),mEvent.getSceneY());
			}else if(event.getEventType().equals(MouseEvent.MOUSE_RELEASED) && line!=null){
				finalizeLine();
			}
		}
		event.consume();
	}


	@Override
	protected void latch() {
		latchTo.addEventFilter(MouseEvent.ANY, this);
		latchTo.addEventFilter(TouchEvent.ANY, this);
	}
	
	
	private void createLine(double x, double y){
		line = cpane.createLine();
		line.setStartPosition(x, y);
	}
	
	private void updateLine(double x, double y){
		line.setEndPosition(x, y);
	}
	
	private void finalizeLine(){
		line = null;
	}

}
