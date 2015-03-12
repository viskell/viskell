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
		
		if(event.equals(CustomGestureEvent.TAP)){
			//TODO:selecteer het aangetikte element, als deze selecteerbaar is.
		} else if(event.equals(CustomGestureEvent.TAP_HOLD)){
			//TODO:open het quick-menu van een element, als deze een quick-menu heeft.
		} else if(event.equals(CustomGestureEvent.ANY)){
			// niets
		}
		
		System.out.println("EVENT TRIGGER!");
	}
	
}
