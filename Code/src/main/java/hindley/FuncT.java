package hindley;

class FuncT extends TypeOp {
    FuncT(Type argType, Type resType) {
        super("->", argType, resType);
    }
}
