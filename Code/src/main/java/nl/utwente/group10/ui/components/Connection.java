package nl.utwente.group10.ui.components;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;

/**
 * This is a ConnectionLine that also stores a startAnchor and an endAnchor to
 * keep track of origin points of the Line.
 * 
 * For Lines that connect inputs and outputs of Blocks see Connection.
 */
public class Connection extends ConnectionLine implements
		ChangeListener<Number> {
	/** Starting point of this Line that can be Anchored onto other objects */
	private OutputAnchor startAnchor;
	/** Ending point of this Line that can be Anchored onto other objects */
	private InputAnchor endAnchor;

	public Connection(){
	}
	public Connection(OutputAnchor from){
		this.setStartAnchor(from);
		
		double x = from.getCenterX();
		double y = from.getCenterY();
		Point2D point = startAnchor.localToScene(x, y);
		setEndPosition(point.getX(), point.getY());
	}
	public Connection(InputAnchor to){
		this.setEndAnchor(to);
		
		double x = to.getCenterX();
		double y = to.getCenterY();
		Point2D point = endAnchor.localToScene(x, y);
		setStartPosition(point.getX(), point.getY());
		
	}
	
	public Connection(OutputAnchor from, InputAnchor to){
		this.setStartAnchor(from);
		this.setEndAnchor(to);
	}
	public Connection(InputAnchor to,OutputAnchor from){
		this(from,to);
	}
	
	public void setFreeEnds(double x, double y){
		if(startAnchor==null){
			setStartPosition(x,y);
		}
		if(endAnchor==null){
			setEndPosition(x,y);
		}
	}
	
	public boolean addAnchor(ConnectionAnchor anchor){
		if(startAnchor==null && anchor instanceof OutputAnchor){
			setStartAnchor((OutputAnchor) anchor);
		}else if(endAnchor==null && anchor instanceof InputAnchor){
			setEndAnchor((InputAnchor) anchor);
		}
		return isConnected();
	}
	
	public boolean isConnected(){
		return startAnchor!=null && endAnchor!=null;
	}
	
	/**
	 * Set the startAnchor for this line. After setting the StartPosition will
	 * be updated.
	 * 
	 * @param Anchor
	 *            to start at.
	 */
	public void setStartAnchor(OutputAnchor start) {
		if (startAnchor != null) {
			startAnchor.getBlock().layoutXProperty().removeListener(this);
			startAnchor.getBlock().layoutYProperty().removeListener(this);
		}
		startAnchor = start;
		startAnchor.setConnection(this);
		
		startAnchor.getBlock().layoutXProperty().addListener(this);
		startAnchor.getBlock().layoutYProperty().addListener(this);
		updateStartPosition();
	}

	/**
	 * Set the endAnchor for this line. After setting the EndPosition will be
	 * updated.
	 * 
	 * @param Anchor
	 *            to end at.
	 */
	public void setEndAnchor(InputAnchor end) {
		if (endAnchor != null) {
			endAnchor.getBlock().layoutXProperty().removeListener(this);
			endAnchor.getBlock().layoutYProperty().removeListener(this);
		}
		endAnchor = end;
		endAnchor.setConnection(this);
		
		endAnchor.getBlock().layoutXProperty().addListener(this);
		endAnchor.getBlock().layoutYProperty().addListener(this);
		updateEndPosition();
	}

	/**
	 * Runs both the update Start end End position functions. Use when
	 * refreshing UI representation of the Line.
	 */
	protected void updateStartEndPositions() {
		updateStartPosition();
		updateEndPosition();
	}

	/**
	 * Refresh the Start position of this Line using startAnchor as a reference
	 * point.
	 */
	private void updateStartPosition() {
		if (startAnchor != null) {
			double x = startAnchor.getCenterX();
			double y = startAnchor.getCenterY();
			Point2D point = startAnchor.localToScene(x, y);

			setStartPosition(point.getX(), point.getY());
		}
	}

	/**
	 * Refresh the End position of this Line using endAnchor as a reference
	 * point.
	 */
	private void updateEndPosition() {
		if (endAnchor != null) {
			double x = endAnchor.getCenterX();
			double y = endAnchor.getCenterY();
			Point2D point = endAnchor.localToScene(x, y);

			setEndPosition(point.getX(), point.getY());
		}
	}
	
	public Optional<OutputAnchor> getOutputAnchor(){
		return Optional.ofNullable(startAnchor);
	}
	public Optional<InputAnchor> getInputAnchor(){
		return Optional.ofNullable(endAnchor);
	}

	@Override
	public void changed(ObservableValue<? extends Number> observable,
			Number oldValue, Number newValue) {
		updateStartEndPositions();
	}
	
	public void disconnect(ConnectionAnchor anchor){
		if(startAnchor!=null && startAnchor.equals(anchor)){
			startAnchor.disconnect(this);
			startAnchor = null;
		}
		if(endAnchor!=null && endAnchor.equals(anchor)){
			endAnchor.disconnect(this);
			endAnchor = null;
		}
	}
	
	public void disconnect(){
		disconnect(startAnchor);
		disconnect(endAnchor);
	}
	
	@Override
	public String toString(){
		return "Connection connecting \n(out) "+startAnchor+"   to\n(in)  "+endAnchor;
	}
}
