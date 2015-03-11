/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

abstract class BowtieBlade extends HBox {
    static final Font NAME_FONT = Font.font("Verdana", FontWeight.BOLD, 20);
    static final Font TERM_FONT = Font.font("Verdana", FontWeight.NORMAL, 20);
    
    private final Bowtie bowtie;
    
    public BowtieBlade(Bowtie bowtie) {
        this.bowtie = bowtie;
        
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.CENTER);
    }
    
    public Bowtie getBowtie() {
        return bowtie;
    }
    
    protected abstract void pushTerm(TermDisplay term);
    
    protected abstract TermDisplay popTerm();
    
    protected Label buildArrowLabel() {
        Label label = new Label("->");
        label.setFont(TERM_FONT);
        return label;
    }
}
