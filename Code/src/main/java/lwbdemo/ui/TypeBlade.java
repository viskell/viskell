package lwbdemo.ui;

import lwbdemo.model.Function;
import lwbdemo.model.Term;

class TypeBlade extends BowtieBlade {
    Bowtie bowtie;
    Term[] terms;
    int termCount;
    
    public TypeBlade(Bowtie bowtie, Term... terms) {
        super(bowtie);
        
        if (terms.length == 0) throw new IllegalArgumentException();

        if (terms.length == 1) {
            if (terms[0] instanceof Function) {
                terms = ((Function) terms[0]).getArguments();
            }
        }

        this.bowtie = bowtie;
        this.terms = terms;
        this.termCount = terms.length;
        
        for (int i = 0; ; i++) {
            getChildren().add(new TermDisplay(terms[i], bowtie));
            if (i == terms.length - 1) break;
            getChildren().add(buildArrowLabel());
        }
    }
    
    public Term getType() {
        Term result = null;
        if (termCount == 1) {
            return terms[terms.length - 1];
        } else {
            Term[] funcArgs = new Term[termCount];
            for (int i = 0; i < termCount ; i++) {
                funcArgs[i] = terms[terms.length - termCount + i];
            }
            result = new Function(funcArgs);
        }
        return result;
    }

    @Override
    public void pushTerm(TermDisplay term) {
        term.setActive(false);
        getChildren().add(0, buildArrowLabel());
        getChildren().add(0, term);
        termCount++;
    }

    @Override
    public TermDisplay popTerm() {
        TermDisplay result = null;
        if (getChildren().size() > 1) {
            result = (TermDisplay) getChildren().get(0);
            getChildren().remove(result);
            getChildren().remove(0);
            termCount--;
        }
        return result;
    }
}