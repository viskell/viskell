/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.ui;

import lwbdemo.model.Term;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import nl.utwente.cs.caes.tactile.control.TactilePane;
import nl.utwente.cs.caes.tactile.control.Anchor;

class TermDisplay extends StackPane{
    private final Term term;
    private final Label termLabel;
    private final Map<Bowtie, ChangeListener<Boolean>> inUseListenerByBowtie;
    private final ChangeListener<Anchor> anchorListener;
    private final ChangeListener<Bounds> boundsListener;
    
    private final Bowtie parentBowtie;
    private Bowtie anchoredBowtie;
    private boolean active;
    
    public TermDisplay(Term term, Bowtie parentBowtie) {
        this.term = term;
        this.parentBowtie = parentBowtie;
        this.inUseListenerByBowtie = new HashMap<>();
        
        termLabel = new Label();
        termLabel.textProperty().bind(term.stringProperty());
        termLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
        
        getChildren().add(termLabel);
        
        anchorListener = (observable, oldVal, newVal) -> {
            if (newVal == null) {
                setAnchoredBowtie(null);
            }
        };
        
        boundsListener = (observable, oldVal, newVal) -> {
            setMinSize(newVal.getWidth() + 15, newVal.getHeight() + 15);
        };
    }
    
    public Term getTerm() {
        return term;
    }
    
    public void setActive(boolean active) {
        if (active) {
            setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
            setMinWidth(50);
            
            TactilePane.setTracker(this, (TactilePane) parentBowtie.getParent());
            TactilePane.setOnAreaEntered(this, event -> {
                if (event.getOther() instanceof TypeBlade && event.getOther() != parentBowtie.typeBlade) {
                    onAreaEntered(((TypeBlade) event.getOther()).getBowtie());
                }
            });
            TactilePane.setOnAreaLeft(this, event -> {
                if (event.getOther() instanceof TypeBlade && event.getOther() != parentBowtie.typeBlade) {
                    onAreaLeft(((TypeBlade) event.getOther()).getBowtie());
                }
            });
            TactilePane.setOnProximityLeft(this, event -> {
                if (event.getOther() instanceof TypeBlade && event.getOther() != parentBowtie.typeBlade) {
                    onProximityLeft(((TypeBlade) event.getOther()).getBowtie());
                }
            });
            TactilePane.setOnInArea(this, event -> {
                if (event.getOther() instanceof TypeBlade && event.getOther() != parentBowtie.typeBlade) {
                    onInProximityOrArea(((TypeBlade) event.getOther()).getBowtie());
                }
            });
            TactilePane.setOnInProximity(this, event -> {
                if (event.getOther() instanceof TypeBlade && event.getOther() != parentBowtie.typeBlade) {
                    onInProximityOrArea(((TypeBlade) event.getOther()).getBowtie());
                }
            });
        } else {
            TactilePane.setTracker(this, null);
            TactilePane.setOnAreaEntered(this, null);
            TactilePane.setOnAreaLeft(this, null);
            TactilePane.setOnInProximity(this, null);
            TactilePane.setOnInArea(this, null);
             
            setBackground(null);
            setMinWidth(-1);
        }
        this.active = active;
    }
    
    public boolean isActive() {
        return active;
    }
    
    // When a Bowtie enters the area of a TermDisplay, starting listening for drop gesture
    private void onAreaEntered(Bowtie bowtie) {
        ChangeListener<Boolean> listener = (observable, oldVal, newVal) -> {
            if (!newVal) {
                onDropped(bowtie);
            }
        };
        TactilePane.inUseProperty(bowtie).addListener(listener);
        inUseListenerByBowtie.put(bowtie, listener);
    }
    
    // When a Bowtie is dropped on a TermDisplay, try to anchor it (apply its type to this term)
    private void onDropped(Bowtie bowtie) {
        setAnchoredBowtie(bowtie);
    }
    
    // When a Bowtie leaves the area of a TermDisplay, stop listening for drop gesture
    private void onAreaLeft(Bowtie bowtie) {
        ChangeListener listener = inUseListenerByBowtie.remove(bowtie);
        if (listener != null) {
            TactilePane.inUseProperty(bowtie).removeListener(listener);
        }
    }
    
    // When a Bowtie is in the proximity, set border green or red when its type may be applied to this term or not respectively.
    private void onInProximityOrArea(Bowtie bowtie) {
        if (!term.isApplicable(bowtie.getType())) {
            Border red = new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2)));
            bowtie.typeBlade.setBorder(red);
            this.setBorder(red);
        } else {
            Border green = new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2)));
            bowtie.typeBlade.setBorder(green);
            this.setBorder(green);
        }
    }
    
    // When a Bowtie leaves the proximity, set border back to default
    private void onProximityLeft(Bowtie bowtie) {
        bowtie.typeBlade.setBorder(null);
        this.setBorder(null);
    }
    
    // Try to apply a Bowties type to this term. If succesful, anchor that Bowtie to this TermDisplay. 
    // Giving null as argument will remove the previous anchor.
    void setAnchoredBowtie(Bowtie bowtie) {
        if (bowtie == anchoredBowtie) {
            return;
        }
        
        if (bowtie == null) {
            // Revert application of previous term
            term.applyTerm(null);
            
            // Stop accomodating size to bowtie
            anchoredBowtie.termBlade.boundsInParentProperty().removeListener(boundsListener);
            setMinSize(-1, -1);
            
            // Remove anchor
            TactilePane.anchorProperty(anchoredBowtie).removeListener(anchorListener);
            anchoredBowtie.setAnchor(null);
            anchoredBowtie = null;
            
            getChildren().add(termLabel);
            setActive(true);
        } else if (TactilePane.getAnchor(bowtie) == null && term.applyTerm(bowtie.getType())) {
            setActive(false);
            getChildren().remove(termLabel);
            
            // Anchor bowtie to TermDisplay
            bowtie.setAnchor(this);
            TactilePane.anchorProperty(bowtie).addListener(anchorListener);
            anchoredBowtie = bowtie;
            
            // Accomodate size to Anchor
            Bounds anchoredBounds = anchoredBowtie.termBlade.getBoundsInParent();
            setMinSize(anchoredBounds.getWidth() + 15, anchoredBounds.getHeight() + 15);
            anchoredBowtie.termBlade.boundsInParentProperty().addListener(boundsListener);
        } else if (TactilePane.getAnchor(bowtie) == null) {
            // If a Bowtie tried to anchor that doesn't "fit", repell it
            TactilePane.moveAwayFrom(bowtie.typeBlade, this, 500);
            TactilePane.moveAwayFrom(this, bowtie.typeBlade, 500);
        }
    }
    
    Bowtie getAnchoredBowtie() {
        return anchoredBowtie;
    }
}
