package nl.utwente.group10.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.gestures.AbstractGesture;
import nl.utwente.group10.ui.gestures.CustomGesture;
import nl.utwente.group10.ui.gestures.CreateConnectionHandler;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Main building block for the visual interface, this class
 * represents a Haskell function together with it's arguments and
 * visual representation.
 */
public class FunctionBlock extends Block {
	/** The inputs for this FunctionBlock.**/
	private InputAnchor[] inputs;

	/** The name of this Function. **/
	private StringProperty name;

	/** The type of this Function. **/
	private StringProperty type;

	/** intstance to create Events for this FunctionBlock. **/
	private static AbstractGesture cg;

	@FXML private Pane anchorSpace;
	
	@FXML private Pane outputSpace;
	
	@FXML private Pane argumentSpace;

	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 *
	 * @param name The name of the function.
	 * @param type The function's type (usually a FuncT).
	 * @param pane The CustomUIPane in which this FunctionBlock exists. Via this this FunctionBlock knows which other FunctionBlocks exist.  @return a new instance of this class
	 * @throws IOException
	 */
	public FunctionBlock(String name, Type type, CustomUIPane pane) throws IOException {
		super("FunctionBlock", pane);
		
		this.name = new SimpleStringProperty(name);
		this.type = new SimpleStringProperty(type.toHaskellType());

		this.getLoader().load();
		
		// Collect argument types
		ArrayList<String> args = new ArrayList<>();
		Type t = type;
		while (t instanceof FuncT) {
			FuncT ft = (FuncT) t;
			args.add(ft.getArgs()[0].toHaskellType());
			t = ft.getArgs()[1];
		}

		this.inputs = new InputAnchor[args.size()];

		// Create anchors and labels for each argument
		for (int i = 0; i < args.size(); i++) {
			inputs[i] = new InputAnchor(this, pane);
			anchorSpace.getChildren().add(inputs[i]);

			argumentSpace.getChildren().add(new Label(args.get(i)));
		}

		// Create an anchor and label for the result
		Label lbl = new Label(t.toHaskellType());
		lbl.getStyleClass().add("result");
		argumentSpace.getChildren().add(lbl);
		outputSpace.getChildren().add(this.getOutputAnchor());
	}
	
	/**
	 * Executes this FunctionBlock and returns the output as a String
	 * @return Output of the Function
	 */
	public String executeMethod() {		
		return "DEBUG-OUTPUT";
	}
	
	/**
	 * Nest another Node object within this FunctionBlock
	 * @param node to nest
	 */
	public void nest(Node node) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the name property of this FunctionBlock.
	 * @return name
	 */
	public String getName() {
		return name.get();
	}
	
	/**
	 * @param name for this FunctionBlock
	 */
	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * Get the type property of this FunctionBlock.
	 * @return type
	 */
	public String getType() {
		return type.get();
	}

	/**
	 * @param type the type property of this FunctionBlock.
	 */
	public void setType(String type) {
		this.type.set(type);
	}
	
	/**
	 * the StringProperty for the name of this FunctionBlock.
	 * @return name
	 */
	public StringProperty nameProperty() {
		return name;
	}
	
	/**
	 * the StringProperty for the type of this FunctionBlock.
	 * @return type
	 */
	public StringProperty typeProperty() {
		return type;
	}
	
	/**
	 * Method to fetch an array containing all of the input anchors for this
	 * FunctionBlock
	 * @return inputAnchors
	 */
	public InputAnchor[] getInputs(){
		return inputs;
	}
	
	/**
	 * Returns the index of the argument matched to the Anchor.
	 * @param anchor The anchor to look up.
	 * @return argumentIndex
	 */
	public int getArgumentIndex(ConnectionAnchor anchor) {
		int index=0;
		/**
		 * @invariant index < inputs.length
		 */
		while((inputs[index]!=anchor)&&(index<inputs.length)) {
			index++;
		}
		return index;
	}

	@Override
	public Expr asExpr() {
		Expr expr = new Ident(getName());

		for (InputAnchor in : getInputs()) expr = new Apply(expr, in.asExpr());

		return expr;
	}
}
