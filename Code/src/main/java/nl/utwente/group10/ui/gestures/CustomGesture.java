package nl.utwente.group10.ui.gestures;

import java.sql.Time;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class CustomGesture implements EventHandler<MouseEvent> {

	private GestureCallBack callBack;

	
	public CustomGesture(GestureCallBack callBack, Node latchTo) {
		this.callBack = callBack;
		latchTo.addEventHandler(MouseEvent.ANY, this);
	}

	@Override
	public void handle(MouseEvent event) {
		Thread tt = new Thread(); 
		InterruptedException ett = null;
		if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)){
			tt.start();
			try {
				tt.join(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				ett = e;
			}
			if (ett.equals(null)){
				callBack.handleCustomEvent(new CustomGestureEvent(CustomGestureEvent.TAP_HOLD));
			} else {
				ett = null;
			}
		}
		if(event.getEventType().equals(MouseEvent.MOUSE_RELEASED)){
			if(tt.isAlive()){
				tt.interrupt();
			} else if(!ett.equals(null)){
				ett = null;
			} else {
				callBack.handleCustomEvent(new CustomGestureEvent(CustomGestureEvent.TAP));
			}
		}
		//if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
		//	callBack.handleCustomEvent(new CustomGestureEvent(
		//			CustomGestureEvent.ANY));
		//}
		else{
			System.out.println(event.getEventType());
		}
	}

}
