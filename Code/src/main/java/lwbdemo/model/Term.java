/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.model;

import javafx.beans.property.StringProperty;

public abstract class Term {
    
    protected StringProperty string;
    
    @Override
    public String toString() {
        return stringProperty().get();
    }
    
    public abstract StringProperty stringProperty();
    
    public abstract boolean isApplicable(Term term);
    
    public abstract Term getAppliedTerm();
    
    public abstract boolean applyTerm(Term term);
}
