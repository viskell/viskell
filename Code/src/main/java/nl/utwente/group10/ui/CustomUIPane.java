package nl.utwente.group10.ui;

import nl.utwente.cs.caes.tactile.control.TactilePane;
import nl.utwente.group10.ui.gestures.CustomGesture;
import nl.utwente.group10.ui.gestures.CustomGestureEvent;
import nl.utwente.group10.ui.gestures.GestureCallBack;

public class CustomUIPane extends TactilePane implements GestureCallBack{

	public CustomUIPane(){
		new CustomGesture(this,this);
		new CustomGesture(this,this);
		new CustomGesture(this,this);
		new CustomGesture(this,this);
	}
	
	
	@Override
	public void handleCustomEvent(CustomGestureEvent event) {
		System.out.println("EVENT TRIGGER!");
	}
	
}
