package nl.utwente.viskell.ui.components;

/**
 * A generic 2-tuple
 * @param <A> type of the first element, a
 * @param <B> type of the second element, b
 */
public class Pair<A, B> {
    public A a;
    public B b;
    
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }
}
