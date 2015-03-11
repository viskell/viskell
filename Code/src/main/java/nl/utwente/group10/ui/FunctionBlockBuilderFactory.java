package nl.utwente.group10.ui;

import nl.utwente.cs.caes.tactile.control.Anchor;
import nl.utwente.cs.caes.tactile.control.Bond;
import nl.utwente.cs.caes.tactile.fxml.TactilePaneAnchorBuilder;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Builder;
import javafx.util.BuilderFactory;

public class FunctionBlockBuilderFactory implements BuilderFactory{
	private final JavaFXBuilderFactory defaultBuilderFactory = new JavaFXBuilderFactory();
	
    @Override
    public Builder<?> getBuilder(Class<?> type) {
    	return defaultBuilderFactory.getBuilder(type);
    }
}
