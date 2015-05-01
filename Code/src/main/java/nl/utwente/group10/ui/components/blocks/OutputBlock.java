package nl.utwente.group10.ui.components.blocks;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.OutputAnchor;

public interface OutputBlock {

	/**
     * @return the output Anchor for this Block
     */
    public OutputAnchor getOutputAnchor();
    
    public Type getOutputType();
	public Type getOutputType(Env env, GenSet genSet);
	
	public Type getOutputSignature();
	public Type getOutputSignature(Env env, GenSet genSet);
}
