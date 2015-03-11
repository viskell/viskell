/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConstantType extends Term {
    public static ConstantType CHAR = new ConstantType("Char");
    public static ConstantType BOOL = new ConstantType("Bool");
    public static ConstantType INT = new ConstantType("Int");
            
    protected ConstantType(String typeName) {
        string = new SimpleStringProperty(typeName);
    }
    
    @Override
    public StringProperty stringProperty() {
        return string;
    }

    @Override
    public boolean isApplicable(Term term) {
        return (term instanceof ConstantType && term.toString().equals(this.toString()));
    }
    
    @Override
    public boolean applyTerm(Term term) {
        return isApplicable(term);
    }
    
    @Override
    public Term getAppliedTerm() {
        return null;
    }
    
}
