/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.ui;

import javafx.scene.control.Label;
import nl.utwente.cs.caes.tactile.control.TactilePane;

class TermBlade extends BowtieBlade {
    Label nameLabel;
    
    public TermBlade(Bowtie bowtie, String name) {
        super(bowtie);
        
        nameLabel = new Label(name);
        nameLabel.setFont(NAME_FONT);
        
        getChildren().add(nameLabel);
    }

    @Override
    protected void pushTerm(TermDisplay term) {
        term.setActive(true);
        getChildren().add(term);
    }

    @Override
    protected TermDisplay popTerm() {
        TermDisplay result = null;
        if (getChildren().size() > 1) {
            result = (TermDisplay) getChildren().get(getChildren().size() - 1);
            Bowtie anchoredBowtie = result.getAnchoredBowtie();
            if (anchoredBowtie != null) {
                result.setAnchoredBowtie(null);
                TactilePane.moveAwayFrom(anchoredBowtie, getBowtie().knot, 1000);
            }
            getChildren().remove(result);
        }
        return result;
    }
}
