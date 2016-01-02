package nl.utwente.viskell.ui.components;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ToplevelPane;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Block that accepts a (Float -> Float) function to be displayed on a linechart
 * inside the Block.
 */
public class GraphBlock extends Block {
    /** The InputAnchor of this Block. */
    private InputAnchor input;

    /** The Pane that contains the inputs. */
    @FXML private Pane inputSpace;

    /** The LineChart that is displayed inside this Block. */
    @FXML private LineChart<Double, Double> chart;

    /** NumberAxis for x. */
    @FXML private NumberAxis x;

    /** NuberAxis for y. */
    @FXML private NumberAxis y;

    /**
     * Constructs a new GraphBlock.
     * @param pane The CustomUIPane on which this Block resides.
     */
    public GraphBlock(ToplevelPane pane) {
        super(pane);
        loadFXML("GraphBlock");

        input = new InputAnchor(this);
        input.layoutXProperty().bind(inputSpace.widthProperty().divide(2));
        inputSpace.getChildren().setAll(input);
        
        //Make sure inputSpace is drawn on top.
        BorderPane borderPane = (BorderPane) inputSpace.getParent();
        borderPane.getChildren().remove(inputSpace);
        borderPane.setTop(inputSpace);
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(input);
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of();
    }
    
    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new GraphBlock(this.getToplevel()));
    }
    
    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return input.getLocalExpr(outsideAnchors);
    }

    @Override
    public void refreshAnchorTypes() {
        this.input.setFreshRequiredType(new FunType(Type.con("Double"), Type.con("Double")), new TypeScope()); 
    }

    @Override
    public void invalidateVisualState() {
    	this.input.invalidateVisualState();
    	
        if (! (this.inValidContext && this.input.hasValidConnection())) {
            return;
        }
        
        ObservableList<XYChart.Series<Double, Double>> lineChartData = FXCollections.observableArrayList();

        double step = 0.01;
        double min = x.getLowerBound();
        double max = x.getUpperBound();

        try {
            GhciSession ghciSession = getToplevel().getGhciSession();
            String funName = "graph_fun_" + Integer.toHexString(this.hashCode());
            ghciSession.push(funName, this.getFullExpr());
            String range = String.format(Locale.US, " [%f,%f..%f]", min, min+step, max);
            String results = ghciSession.pullRaw("putStrLn $ unwords $ map show $ map " + funName + range).get();

            LineChart.Series<Double, Double> series = new LineChart.Series<>();
            ObservableList<XYChart.Data<Double, Double>> data = series.getData();
            Iterator<String> v = Splitter.on(' ').split(results).iterator();

            for (double i = min; i < max; i += step) {
                data.add(new XYChart.Data<>(i, Double.valueOf(v.next())));
            }

            lineChartData.add(series);
        } catch (NoSuchElementException | NumberFormatException | InterruptedException | ExecutionException ignored) {
            // Pretend we didn't hear anything.
        }

        chart.setData(lineChartData);
    }

    @Override
    public boolean checkValidInCurrentContainer() {
        if (this.container instanceof LambdaContainer || this.container instanceof Lane) {
            return false;
        } else {
            return true;
        }
    }
    
}
