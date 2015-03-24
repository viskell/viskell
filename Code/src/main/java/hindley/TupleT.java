package hindley;

class TupleT extends TypeOp {
    TupleT(Type... args) {
        super("(,)", args);
    }
}
