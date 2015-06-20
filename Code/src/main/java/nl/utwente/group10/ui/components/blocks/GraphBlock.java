package nl.utwente.group10.ui.components.blocks;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class GraphBlock extends Block implements InputBlock {
    private InputAnchor input;

    @FXML
    private Pane inputSpace;

    @FXML
    private LineChart<Double, Double> chart;

    @FXML
    private NumberAxis x;

    @FXML
    private NumberAxis y;

    public GraphBlock(CustomUIPane pane) {
        super(pane);

        loadFXML("GraphBlock");

        input = new InputAnchor(this, pane);
        inputSpace.getChildren().setAll(input);
    }

    @Override
    public Expr asExpr() {
        return input.asExpr();
    }

    @Override
    public Type getInputSignature(InputAnchor input) {
        assert input.equals(this.input);
        return getInputSignature(0);
    }

    @Override
    public Type getInputSignature(int index) {
        assert index == 0;
        return new FuncT(new ConstT("Float"), new ConstT("Float"));
    }

    @Override
    public Type getInputType(InputAnchor input) {
        return getInputSignature(0);
    }

    @Override
    public Type getInputType(int index) {
        return getInputSignature(0);
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
                            asExpr()
                        ),
                        new Ident(String.format("[%f,%f..%f]", min, min+step, max))
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
