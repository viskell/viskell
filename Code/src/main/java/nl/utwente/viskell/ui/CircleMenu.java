package nl.utwente.viskell.ui;

import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import jfxtras.scene.layout.CircularPane;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.ui.components.*;

/**
 * Circle menu is a context based menu implementation for {@link Block} classes.
 * Preferably each block class has it's own instance of circle menu. When a
 * block based class has significant differences to other block classes that
 * result in different context based actions it should use a specialized
 * extension of circle menu instead of this one.
 * <p>
 * Current context based features include delete. Copy, Paste and Save
 * functionality is under development.
 * </p>
 */
public class CircleMenu extends CircularPane {

    /** The context of the menu. */
    private Block block;

    /** Timed delay for hide of this menu after inactivity. */
    private Timeline hideDelay;
    
    /** Show the Circle menu for a specific block. */
    public static void showFor(Block block, Point2D pos, boolean byMouse) {
        CircleMenu menu = new CircleMenu(block, byMouse);
        double centerX = pos.getX() - (menu.prefWidth(-1) / 2);
        double centerY = pos.getY() - (menu.prefHeight(-1) / 2);
        menu.setLayoutX(centerX);
        menu.setLayoutY(centerY);
        block.getToplevel().addMenu(menu);
    }
    
    private CircleMenu(Block block, boolean byMouse) {
        super();
        this.block = block;

        this.hideDelay = new Timeline(new KeyFrame(Duration.millis(4000), e-> this.hide()));
        this.hideDelay.play();
        this.setMouseTransparent(true);
        
        // Define menu items
        
        // Cut Option
        ImageView image = makeImageView("/ui/icons/appbar.scissor.png");
        MenuButton delete = new MenuButton("cut", image);
        delete.setOnActivate(() -> delete());
        this.add(delete);

        if (block instanceof LambdaBlock) {
            image = makeImageView("/ui/icons/appbar.arrow.collapsed.png");
            MenuButton resize = new MenuButton("resize", image);
            resize.setOnActivate(() -> ((LambdaBlock)block).resizeToFitAll());
            this.add(resize);
        } else {
            // Copy Option
            image = makeImageView("/ui/icons/appbar.page.copy.png");
            MenuButton copy = new MenuButton("copy", image);
            copy.setOnActivate(() -> copy());
            this.add(copy);
        }

        if (block instanceof LambdaBlock) {
            image = makeImageView("/ui/icons/appbar.input.pen.png");
            MenuButton editSig = new MenuButton("editSignature", image);
            editSig.setOnActivate(() -> ((LambdaBlock)block).editSignature());
            this.add(editSig);
            
            if (! ((LambdaBlock)block).isTypedLambda()) {
                image = makeImageView("/ui/icons/appbar.edit.add.png");
                MenuButton addInput = new MenuButton("add input", image);
                addInput.setOnActivate(() -> ((LambdaBlock)block).getBody().addExtraInput());
                this.add(addInput);

                image = makeImageView("/ui/icons/appbar.edit.minus.png");
                MenuButton removeInput = new MenuButton("remove input", image);
                removeInput.setOnActivate(() -> ((LambdaBlock)block).getBody().removeLastInput());
                this.add(removeInput);
            }
            
        } else if (block instanceof ChoiceBlock) {
            image = makeImageView("/ui/icons/appbar.layout.collapse.right.png");
            MenuButton addLane = new MenuButton("add lane", image);
            addLane.setOnActivate(() -> ((ChoiceBlock)block).addLane());
            this.add(addLane);
            
            image = makeImageView("/ui/icons/appbar.layout.expand.left.variant.png");
            MenuButton removeLane = new MenuButton("remove lane", image);
            removeLane.setOnActivate(() -> ((ChoiceBlock)block).removeLastLane());
            this.add(removeLane);

        } else {
            if (block instanceof ConstantBlock) {
                image = makeImageView("/ui/icons/appbar.page.edit.png");
                MenuButton edit = new MenuButton("edit", image);
                edit.setOnActivate(() -> ((ConstantBlock)block).editValue(Optional.empty()));
                this.add(edit);
                
            } else {
                // Paste Option
                image = makeImageView("/ui/icons/appbar.clipboard.paste.png");
                MenuButton paste = new MenuButton("paste", image);
                paste.setOnActivate(() -> paste());
                this.add(paste);
            }

            // Save Option
            image = makeImageView("/ui/icons/appbar.save.png");
            MenuButton save = new MenuButton("save", image);
            save.setOnActivate(() -> saveBlock());
            this.add(save);
        }
        
        if (block instanceof ValueBlock) {
            image = makeImageView("/ui/icons/appbar.layer.up.png");
            MenuButton lift = new MenuButton("lift", image);
            lift.setOnActivate(() -> {
                ToplevelPane toplevel = this.block.getToplevel();
                toplevel.removeBlock(block);
                Block lifter = new LiftingBlock(toplevel, new NestedValue((ValueBlock)block));
                lifter.setLayoutX(block.getLayoutX()-10);
                lifter.setLayoutY(block.getLayoutY()-10);
                toplevel.addBlock(lifter);
                lifter.initiateConnectionChanges();
            });
            this.add(lift);
        } else if (block instanceof FunctionBlock) {
            image = makeImageView("/ui/icons/appbar.layer.up.png");
            MenuButton lift = new MenuButton("lift", image);
            lift.setOnActivate(() -> {
                ToplevelPane toplevel = this.block.getToplevel();
                FunctionReference funRef = ((FunctionBlock)block).getFunReference();
                if (funRef instanceof LibraryFunUse) {
                    toplevel.removeBlock(block);
                    Block lifter = new LiftingBlock(toplevel, new NestedFunction(((LibraryFunUse)funRef).getFunInfo(), block));
                    lifter.setLayoutX(block.getLayoutX()-10);
                    lifter.setLayoutY(block.getLayoutY()-10);
                    toplevel.addBlock(lifter);
                    lifter.initiateConnectionChanges();
                }
            });
            this.add(lift);
        } else if (block instanceof FunApplyBlock) {
            image = makeImageView("/ui/icons/appbar.layer.up.png");
            MenuButton lift = new MenuButton("lift", image);
            lift.setOnActivate(() -> {
                ToplevelPane toplevel = this.block.getToplevel();
                FunctionReference funRef = ((FunApplyBlock)block).getFunReference();
                if (funRef instanceof LibraryFunUse) {
                    toplevel.removeBlock(block);
                    Block lifter = new LiftingBlock(toplevel, new NestedFunction(((LibraryFunUse)funRef).getFunInfo(), block));
                    lifter.setLayoutX(block.getLayoutX()-10);
                    lifter.setLayoutY(block.getLayoutY()-10);
                    toplevel.addBlock(lifter);
                    lifter.initiateConnectionChanges();
                }
            });
            this.add(lift);
        } else if (block instanceof BinOpApplyBlock) {
            image = makeImageView("/ui/icons/appbar.layer.up.png");
            MenuButton lift = new MenuButton("lift", image);
            lift.setOnActivate(() -> {
                ToplevelPane toplevel = this.block.getToplevel();
                FunctionInfo funInfo = ((BinOpApplyBlock)block).getFunInfo();
                toplevel.removeBlock(block);
                Block lifter = new LiftingBlock(toplevel, new NestedFunction(funInfo, block));
                lifter.setLayoutX(block.getLayoutX()-10);
                lifter.setLayoutY(block.getLayoutY()-10);
                toplevel.addBlock(lifter);
                lifter.initiateConnectionChanges();
            });
            this.add(lift);
        } else if (block instanceof LiftingBlock) {
            image = makeImageView("/ui/icons/appbar.layer.down.png");
            MenuButton unlift = new MenuButton("unlift", image);
            unlift.setOnActivate(() -> {
                ToplevelPane toplevel = this.block.getToplevel();
                Block original = ((LiftingBlock)block).getNested().getOriginal();
                toplevel.removeBlock(block);
                original.setLayoutX(block.getLayoutX());
                original.setLayoutY(block.getLayoutY());
                toplevel.addBlock(original);
                original.initiateConnectionChanges();
            });
            this.add(unlift);
        }
        
        // opening animation
        this.setScaleX(0.1);
        this.setScaleY(0.1);
        ScaleTransition opening = new ScaleTransition(byMouse ? Duration.ONE : Duration.millis(250), this);
        opening.setToX(1);
        opening.setToY(1);
        opening.setOnFinished(e -> this.setMouseTransparent(false));
        opening.play();
    }
    
    /** hide this menu by removing it */
    private void hide() {
        this.block.getToplevel().removeMenu(this);
    }
    
    private ImageView makeImageView(String path) {
        ImageView image = new ImageView(new Image(this.getClass().getResourceAsStream(path)));
        return image;
    }

    /** Copy the {@link Block} in this context. */
    private void copy() {
        block.getToplevel().copyBlock(block);
    }

    /** Paste {@link Block} from memory. */
    private void paste() {
    }

    /** Delete the {@link Block} in this context. */
    private void delete() {
        block.getToplevel().removeBlock(block);
    }

    /** Saves the {@link Block} in this context. */
    private void saveBlock() {
        // TODO store block in custom catalog?
    }
    
    /** A touch enabled button within this circle menu */
    private class MenuButton extends StackPane {
        /** Whether this button has been pressed */
        private boolean wasPressed;
        
        /** The action to execute on a 'click' */
        private Runnable clickAction;
        
        /**
         * @param name of the button
         * @param image node shown on the button
         */
        private MenuButton(String name, Node image) {
            super();
            Circle backing = new Circle(0, 0, 32, Color.GOLD);
            backing.setEffect(new DropShadow(20, 5, 5, Color.BLACK));
            backing.setStroke(Color.BLACK);
            backing.setStrokeWidth(1);
            this.getChildren().addAll(backing, image);
            this.setPrefSize(64, 64);
            
            this.wasPressed = false;
            
            this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onPress);
            this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::onPress);
            this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onRelease);
            this.addEventHandler(TouchEvent.TOUCH_RELEASED, this::onRelease);
        }
        
        /** Sets the action to execute on a 'click' */
        private void setOnActivate(Runnable action) {
            this.clickAction = action;
        }
        
        /** Handles a press event for this button. */
        private void onPress(Event event) {
            this.wasPressed = true;
            CircleMenu.this.hideDelay.playFromStart();
            event.consume();
        }
        
        /** Handles a release event for this button. */
        private void onRelease(Event event) {
            if (this.wasPressed) {
                if (this.clickAction != null) {
                    this.clickAction.run();
                    // avoid double actions
                    this.clickAction = null;
                }
                
                CircleMenu.this.hide();
            }
            event.consume();
        }
    }
}
