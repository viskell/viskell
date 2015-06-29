package nl.utwente.group10.ui.components.blocks.input;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Block that accepts a (Float -> Float) function to be displayed on a linechart
 * inside the Block.
 */
public class GraphBlock extends Block implements InputBlock {
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
    public GraphBlock(CustomUIPane pane) {
        super(pane);

        loadFXML("GraphBlock");

        input = new InputAnchor(this, new FuncT(new ConstT("Float"), new ConstT("Float")));
        input.layoutXProperty().bind(inputSpace.widthProperty().divide(2));
        inputSpace.getChildren().setAll(input);
        
        //Make sure inputSpace is drawn on top.
        BorderPane borderPane = (BorderPane) inputSpace.getParent();
        borderPane.getChildren().remove(inputSpace);
        borderPane.setTop(inputSpace);
    }

    @Override
    public Expr updateExpr() {
        return input.asExpr();
    }

    @Override
    public Type getInputSignature(int index) {
        assert index == 0;
        return input.getSignature();
    }
    @Override
    public Type getInputType(int index) {
        return input.getType();
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(input);
    }

    @Override
    public List<InputAnchor> getActiveInputs() {
        return ImmutableList.of(input);
    }

    @Override
    public void invalidateConnectionState() {
        ObservableList<XYChart.Series<Double, Double>> lineChartData = FXCollections.observableArrayList();

        double step = 0.01;
        double min = x.getLowerBound();
        double max = x.getUpperBound();
        // Haskell equivalent:
        // putStrLn $ unwords $ map show $ map (id) [1.0,1.1..5.0]
        Expr expr = new Apply(
            new Ident("putStrLn"),
            new Apply(
                new Ident("unwords"),
                new Apply(
                    new Apply(
                        new Ident("map"),
                        new Ident("show")
                    ),
                    new Apply(
                        new Apply(
                            new Ident("map"),
                            updateExpr()
                        ),
                        new Ident(String.format(Locale.US,"[%f,%f..%f]", min, min+step, max))
                    )
                )
            )
        );
        
        try {
            String results = getPane().getGhciSession().get().pull(expr);

            LineChart.Series<Double, Double> series = new LineChart.Series<>();
            ObservableList<XYChart.Data<Double, Double>> data = series.getData();
            Iterator<String> v = Splitter.on(' ').split(results).iterator();

            for (double i = min; i < max; i += step) {
                data.add(new XYChart.Data<>(i, Double.valueOf(v.next())));
            }

            lineChartData.add(series);
        } catch (HaskellException | NoSuchElementException | NumberFormatException ignored) {
            // Pretend we didn't hear anything.
        }

        chart.setData(lineChartData);
    }
}
