package nl.utwente.group10.ui.components.blocks;

import com.google.common.collect.ImmutableList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

import java.util.List;

public class GraphBlock extends Block implements InputBlock {
    private InputAnchor input;

    @FXML
    private Pane anchorSpace;

    @FXML
    private LineChart<Double, Double> chart;

    public GraphBlock(CustomUIPane pane) {
        super(pane);

        loadFXML("GraphBlock");

        input = new InputAnchor(this, pane);
        anchorSpace.getChildren().setAll(input);
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

        LineChart.Series<Double, Double> series1 = new LineChart.Series<>();
        series1.getData().add(new XYChart.Data<Double, Double>(0.0, 1.0));
        series1.getData().add(new XYChart.Data<Double, Double>(1.2, 1.4));
        series1.getData().add(new XYChart.Data<Double, Double>(2.2, 1.9));
        series1.getData().add(new XYChart.Data<Double, Double>(2.7, 2.3));
        series1.getData().add(new XYChart.Data<Double, Double>(2.9, 0.5));

        lineChartData.add(series1);

        chart.setData(lineChartData);
    }
}
