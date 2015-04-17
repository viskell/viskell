package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;
import java.util.ArrayList;

import edu.emory.mathcs.backport.java.util.Arrays;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
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
	
	private boolean initialized = false;

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
		
		
		//TODO meh, maybe do different
		initialized = true;
		
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

	@Override
	public final Expr asExpr() {
		Expr expr = new Ident(getName());
		int undefined = 0;
		for (InputAnchor in : getInputs()) {
			if(in!=null){
				Expr inputExpr = in.asExpr();
				//TODO check for something else than undefined (thus pass something else than an undefined Ident)
				if(!inputExpr.toHaskell().equals("undefined")){
					for(int i=0;i<undefined;i++){
						expr = new Apply(expr,new Ident("undefined"));
					}
					undefined = -1;
					expr = new Apply(expr, inputExpr);
				}else{
					if(undefined>=0){
						undefined++;
					}
				}
			}
		}
		return expr;
	}
	
	public final boolean isInitialized(){
		return true;
	}
	
	public final Expr getNakedExpr(){
		return new Ident(getName());
	}


	@Override
	public void invalidate(){
		invalidate(getPane().getEnvInstance(),new GenSet());
	}
	
	public void invalidate(Env env, GenSet genSet) {
		if(isInitialized()){
			Type nakedType = null;
			Type currentType = null;
			try{
				nakedType = this.getNakedExpr().analyze(env, genSet);
			}catch(HaskellException e){
				//Function is wrongly specified in catalogus
				e.printStackTrace();
			}
			try {
				currentType = this.asExpr().analyze(env,genSet).prune();

				invalidateInput(env,genSet,currentType,nakedType);
				invalidateOutput(currentType);
			}catch (HaskellTypeError e1){
				// One of the inputs arguments is of the wrong type.
				System.out.println("Type mismatch!");
				//TODO display type mismatch.
			} catch (HaskellException e2) {
				// TODO are there other exceptions?
				e2.printStackTrace();
			}
		}
	}
	
	private void invalidateInput(Env env, GenSet genSet, Type currentType, Type nakedType){
		//TODO not clear and re-add all labels every invalidate()
		inputTypes.getChildren().clear();
		InputAnchor[] inputs = getInputs();
		for(int i=0;i<inputs.length;i++){
			InputAnchor in = inputs[i];
			Expr expr = in.asExpr();
			
			//TODO pass something other than an undefined Ident
			Type type = null;
			if(!expr.toHaskell().equals("undefined")){
				try {
					type = expr.analyze(env,genSet).prune();
				} catch (HaskellException e) {
					e.printStackTrace();
					//TODO is this the right thing to do?
					// type = ((ConstT) nakedType).getArgs()[i];
				}
			}else{
				//TODO does this cast always work?
				Type[] types = ((ConstT) nakedType).getArgs();
				for(int depth = 1; depth<=i;depth++){
					types = ((ConstT) types[1]).getArgs();
				}
				type = types[0];				
			}
			inputTypes.getChildren().add(new Label(type.toHaskellType()));
		}
	}
	
	private void invalidateOutput(Type currentType){
		outputTypes.getChildren().clear();
		outputTypes.getChildren().add(new Label(currentType.toHaskellType()));
	}
}
