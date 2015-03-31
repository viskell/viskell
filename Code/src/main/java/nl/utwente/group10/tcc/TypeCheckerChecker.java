package nl.utwente.group10.tcc;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

import java.net.URL;
import java.util.ResourceBundle;

public class TypeCheckerChecker extends Application implements Initializable {
    @FXML private TextField fun;
    @FXML private TextField arg;
    @FXML private Label res;

    private TypeBuilder tb;

    @Override
    public void start(Stage stage) throws Exception {
        tb = new TypeBuilder();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/tcc/TypeCheckerChecker.fxml"));
        fxmlLoader.setController(this);
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Type inference demo");
        stage.setScene(scene);
        stage.show();
    }

    public void recalculate() {
        Type funT = tb.build(fun.getText());
        Type argT = tb.build(arg.getText());
        Type resT = HindleyMilner.makeVariable();

        // First, check if funT is a function
        try {
            HindleyMilner.unify(funT, new FuncT(new VarT(""), new VarT("")));
        } catch (HaskellTypeError haskellTypeError) {
            res.setText("⊥ (Invalid function type.)");
            return;
        }

        // Then check if the argument is reasonable
        try {
            HindleyMilner.unify(funT, new FuncT(argT, resT));
            res.setText(resT.prune().toHaskellType());
        } catch (HaskellTypeError haskellTypeError) {
            res.setText("⊥ (Types do not unify.)");
            return;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
