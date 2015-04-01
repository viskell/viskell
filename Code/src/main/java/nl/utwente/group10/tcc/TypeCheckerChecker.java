package nl.utwente.group10.tcc;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * User interface for the Haskell type checker.
 */
public class TypeCheckerChecker extends Application implements Initializable {
    /** Field for the function to apply an argument to. */
    @FXML private TextField fun;

    /** Field for the argument to apply. */
    @FXML private TextField arg;

    /** Label containing the result of the type checker. */
    @FXML private Label res;

    /** TypeBuilder instance that converts strings into Type instances. */
    private TypeBuilder tb;

    @Override
    public final void start(Stage stage) throws Exception {
        tb = new TypeBuilder();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/tcc/TypeCheckerChecker.fxml"));
        fxmlLoader.setController(this);
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Type inference demo");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Takes the input and performs the tasks again to produce a new output.
     */
    public final void recalculate() {
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
        }
    }

    /**
     * @param args The command line arguments for the program.
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
