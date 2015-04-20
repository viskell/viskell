package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.BackendUtils;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.InputAnchor;

/**
 * Main building block for the visual interface, this class represents a Haskell
 * function together with it's arguments and visual representation.
 */
public class FunctionBlock extends Block {
	/** The inputs for this FunctionBlock. **/
	private InputAnchor[] inputs;

	/** The function name. **/
	private StringProperty name;

	/** The type of this Function. **/
	private StringProperty type;

	@FXML
	private Pane anchorSpace;

	@FXML
	private Pane outputSpace;

	@FXML
	private Pane inputTypes;

	@FXML
	private Pane outputTypes;

	@FXML
	private Pane argumentSpace;

	/**
	 * Method that creates a newInstance of this class along with it's visual
	 * representation
	 *
	 * @param name
	 *            The name of the function.
	 * @param type
	 *            The function's type (usually a FuncT).
	 * @param pane
	 *            The parent pane in which this FunctionBlock exists.
	 * @throws IOException
	 *             when the FXML defenition for this Block cannot be loaded.
	 */
	public FunctionBlock(String name, Type type, CustomUIPane pane)
			throws IOException {
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

		invalidate();
	}

	/**
	 * Nest another Node object within this FunctionBlock
	 * 
	 * @param node
	 *            The node to nest.
	 */
	public final void nest(Node node) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the name property of this FunctionBlock.
	 * 
	 * @return name The name of this Block.
	 */
	public final String getName() {
		return name.get();
	}

	/**
	 * @param name
	 *            The name of this FunctionBlock
	 */
	public final void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return type The Haskell type of this FunctionBlock.
	 */
	public final String getType() {
		return type.get();
	}

	/**
	 * @param type
	 *            The new Haskell type for this FunctionBlock.
	 */
	public final void setType(String type) {
		this.type.set(type);
	}

	/**
	 * @return name The StringProperty for the name of the function.
	 */
	public final StringProperty nameProperty() {
		return name;
	}

	/**
	 * @return type The StringProperty for the type of the function.
	 */
	public final StringProperty typeProperty() {
		return type;
	}

	/**
	 * @return The array of input anchors for this function block.
	 */
	public final InputAnchor[] getInputs() {
		return inputs;
	}

	/**
	 * Returns the index of the argument matched to the Anchor.
	 * 
	 * @param anchor
	 *            The anchor to look up.
	 * @return The index of the given Anchor in the input anchor array.
	 */
	public final int getArgumentIndex(ConnectionAnchor anchor) {
		int index = 0;
		/**
		 * @invariant index < inputs.length
		 */
		while ((inputs[index] != anchor) && (index < inputs.length)) {
			index++;
		}
		return index;
	}

	/**
	 * @return The current (output) expression belonging to this block. If input
	 *         number n is filled in, this output assumes all inputs till number n are also
	 *         filled in.
	 */
	@Override
	public final Expr asExpr() {
		Expr expr = new Ident(getName());
		// Find last input that is filled in
		for (int a = getInputs().length - 1; a >= 0; a--) {
			InputAnchor in = getInputs()[a];
			Expr inputExpr = in.asExpr();
			// Fill in all inputs till the filled in input.
			if (in != null && BackendUtils.isValidExpression(inputExpr)) {
				for (int b = 0; b <= a; b++) {
					in = getInputs()[b];
					inputExpr = in.asExpr();
					expr = new Apply(expr, inputExpr);
				}
				a = -1; // Break loop
			}
		}
		return expr;
	}

	public Type getFunctionSignature(){
		return getFunctionSignature(getPane().getEnvInstance(),new GenSet());
	}
	
	public Type getFunctionSignature(Env env, GenSet genSet) {
		try {
			return new Ident(getName()).analyze(env, genSet);
		} catch (HaskellException e) {
			// Only happens when function is incorrectly declared in catalog.
			e.printStackTrace();
			//TODO return something more meaningful?
			return null;
		}
	}
	
	public Type getOutputType(){
		return getOutputType(getPane().getEnvInstance(),new GenSet());
	}
	
	public Type getOutputType(Env env, GenSet genSet){
		try {
			return this.asExpr().analyze(env, genSet);
		} catch (HaskellException e) {
			// TODO: Current connections are wrong, now what?
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void invalidate() {
		invalidate(getPane().getEnvInstance(), new GenSet());
	}

	public void invalidate(Env env, GenSet genSet) {
		// TODO not clear and re-add all labels every invalidate()
		Type nakedType = getFunctionSignature();
		try {
			Type currentType = this.asExpr().analyze(env, genSet).prune();

			invalidateInput(env, genSet, currentType, nakedType);
			invalidateOutput(currentType);
		} catch (HaskellTypeError e1) {
			// One of the inputs arguments is of the wrong type.
			System.out.println("Type mismatch!");
			// TODO display type mismatch.
		} catch (HaskellException e2) {
			// TODO are there other exceptions?
			e2.printStackTrace();
		}
	}

	/**
	 * Updates the input types to the Block's new state.
	 * 
	 * @param env
	 * @param genSet
	 * @param outputType
	 *            The current output type of the function (with inputs applied)
	 * @param fullType
	 *            The full type of the fuction (no inputs applied).
	 */
	private void invalidateInput(Env env, GenSet genSet, Type outputType,
			Type fullType) {
		inputTypes.getChildren().clear();
		InputAnchor[] inputs = getInputs();
		for (int i = 0; i < inputs.length; i++) {
			InputAnchor in = inputs[i];
			Expr expr = in.asExpr();

			Type type = null;
			if (BackendUtils.isValidExpression(expr)) {
				try {
					type = expr.analyze(env, genSet).prune();
				} catch (HaskellException e) {
					e.printStackTrace();
					// TODO now what?
				}
			} else {
				// If this cast fails, the function signature is wrongly specified.
				if (fullType instanceof ConstT) {
					type = getInputType(i);
				} else {
					type = fullType;
					System.err.println("Fulltype is wrongly specified!");
					// TODO do something useful
				}
			}
			inputTypes.getChildren().add(new Label(type.toHaskellType()));
		}
	}
	
	public Type getInputType(int index){
		if(index>=0 && index <inputs.length){
			//TODO what if fullType != ConstT
			return BackendUtils.dive((ConstT) getFunctionSignature(),index + 1);
		}else{
			return null;
		}
	}
	
	public Type getInputType(InputAnchor input){
		for(int i=0;i<getInputs().length;i++){
			if(getInputs()[i].equals(input)){
				return getInputType(i);
			}
		}
		//TODO return invalid type?
		return null;
	}

	private void invalidateOutput(Type outputType) {
		outputTypes.getChildren().clear();
		outputTypes.getChildren().add(new Label(outputType.toHaskellType()));
	}
}
