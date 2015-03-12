package nl.utwente.group10.ui;

import nl.utwente.cs.caes.tactile.control.TactilePane;

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
