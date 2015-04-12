package nl.utwente.group10.ui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import nl.utwente.group10.haskell.catalog.Entry;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.DisplayBlock;
import nl.utwente.group10.ui.components.FunctionBlock;
import nl.utwente.group10.ui.components.ValueBlock;

import java.io.IOException;

public class MainMenu extends ContextMenu {
	private CustomUIPane parent;

	public MainMenu(HaskellCatalog catalog, CustomUIPane tactilePane) {
		parent = tactilePane;

		for (String category : catalog.getCategories()) {
			Menu submenu = new Menu(category);

			for (Entry entry : catalog.getCategory(category)) {
				MenuItem item = new MenuItem(entry.getName());
				item.setOnAction(event -> addFunctionBlock(entry));
				submenu.getItems().add(item);
			}

			this.getItems().addAll(submenu);
		}

		MenuItem quitItem = new MenuItem("Quit");
		quitItem.setOnAction(event -> System.exit(0));

		SeparatorMenuItem sep = new SeparatorMenuItem();

		this.getItems().addAll(sep, quitItem);
	}

	private void addFunctionBlock(Entry entry) {
		try {
			if(entry.getBlockType().equals("function_basic")){
				FunctionBlock fb = new FunctionBlock(entry.getName(), entry.getType(), parent);
				parent.getChildren().add(fb);
			}else if(entry.getBlockType().equals("display")){
				DisplayBlock db = new DisplayBlock(parent);
				parent.getChildren().add(db);
				parent.invalidate();
			}else if(entry.getBlockType().equals("value")){
				ValueBlock vb = new ValueBlock(parent);
				parent.getChildren().add(vb);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
