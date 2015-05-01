package nl.utwente.group10.ui.components.blocks;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.InputAnchor;

public interface InputBlock {
	
	public Type getInputSignature(InputAnchor input);
	public Type getInputSignature(int index);

	public Type getInputType(InputAnchor input);
	public Type getInputType(int index);
	
	public InputAnchor[] getInputs();
	public int getInputIndex(InputAnchor anchor);
	public boolean inputsAreConnected();
	public boolean inputIsConnected(int index);
}
