/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Function extends Term {
    final Term[] type;
    
    public Function(Term... arguments) {
        if (arguments.length < 2) {
            throw new IllegalArgumentException();
        }
        
        this.type = arguments;
    }
    
    @Override
    public StringProperty stringProperty() {
        if (string == null) {
            string = new SimpleStringProperty();
            
            StringProperty[] stringProperties = new StringProperty[type.length];
            for (int i = 0; i < type.length; i++) {
                stringProperties[i] = type[i].stringProperty();
            }
            
            Object[] concatArgs = new Object[stringProperties.length * 2 + 1];
            int i = 0;
            concatArgs[i] = "(";
            for (i = 1; i < concatArgs.length - 1; i++) {
                
                if ((i-1) % 2 == 0) {
                    concatArgs[i] = stringProperties[(i-1) / 2];
                } else {
                    concatArgs[i] = "->";
                }
            }
            concatArgs[i] = ")";
            
            string.bind(Bindings.concat(concatArgs));
        }
        return string;
    }
    
    public Term[] getArguments() {
        return type;
    }
    
    @Override
    public boolean isApplicable(Term term) {
        if (term == null) {
            return true;
        }
        // Klopt bij lange na niet, geeft alleen een idee
        if (term instanceof Function) {
            Function other = (Function) term;
            if (this.type.length == other.type.length) {
                for (int i = 0; i < type.length; i++) {
                    if (!this.type[i].isApplicable(other.type[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean applyTerm(Term term) {
        if (term == null) {
            for (int i = 0; i < type.length; i++) {
                this.type[i].applyTerm(null);
            }
            return true;
        }
        if (isApplicable(term)) {
            Function other = (Function) term;
            for (int i = 0; i < type.length; i++) {
                this.type[i].applyTerm(other.type[i]);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public Term getAppliedTerm() {
        return this;
    }

    
}
