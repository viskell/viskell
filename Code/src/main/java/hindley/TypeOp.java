package hindley;

import com.google.common.base.Joiner;

class TypeOp extends Type {
    private String constructor;
    private Type[] args;

    TypeOp(String constructor, Type... args) {
        this.constructor = constructor;
        this.args = args;
    }

    public Type[] getArgs() {
        return args;
    }

    public String getConstructor() {
        return constructor;
    }

    public String toString() {
        if (args.length > 0) {
            return String.format("(%s %s)", constructor, Joiner.on(' ').join(args));
        } else {
            return constructor;
        }
    }
}
