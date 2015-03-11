/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class List extends Term{
    final Term type;
    
    public List(Term type) {
        this.type = type;
    }
    
    @Override
    public StringProperty stringProperty() {
        if (string == null) {
            string = new SimpleStringProperty();
            string.bind(Bindings.concat("[", this.type.stringProperty(), "]"));
        }
        return string;
    }
    
    @Override
    public boolean isApplicable(Term term) {
        if (term == null) {
            return this.type.isApplicable(null);
        }
        if (term instanceof List) {
            List other = (List) term;
            return this.type.isApplicable(other.getAppliedTerm());
        }
        return false;
    }
    
    @Override
    public boolean applyTerm(Term term) {
        if (term == null) {
            return this.type.applyTerm(null);
        }
        if (term instanceof List) {
            List other = (List) term;
            return this.type.applyTerm(other.getAppliedTerm());
        }
        return false;
    }
    
    @Override
    public Term getAppliedTerm() {
        return type;
    }
}
