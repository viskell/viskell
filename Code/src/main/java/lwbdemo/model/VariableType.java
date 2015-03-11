/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VariableType extends Term{
    final String name;
    Term type;
    
    public VariableType(String name) {
        this.name = name;
    }
    
    @Override
    public StringProperty stringProperty() {
        if (string == null) {
            string = new SimpleStringProperty(name);
        }
        return string;
    }
    
    @Override
    public boolean isApplicable(Term term) {
        if (this.type != null) {
            if (term != null) {
                return this.type.applyTerm(term);
            } 
        }
        return true;
    }
    
    @Override
    public boolean applyTerm(Term term) {
        if (this.type != null) {
            if (term != null) {
                return this.type.applyTerm(term);
            } else {
                this.type = term;
                stringProperty().unbind();
                stringProperty().set(this.name);
            }
        } else {
            if (term != null) {
                this.type = term;
                stringProperty().bind(term.stringProperty());
            } else {
                this.type = term;
                stringProperty().unbind();
                stringProperty().set(this.name);
            }
        }
        return true;
    }
    
    @Override
    public Term getAppliedTerm() {
        return type;
    }
    
    
}
