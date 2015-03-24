package hindley;

import java.util.Optional;

class TypeVar extends Type {
    private String name;
    private Optional<Type> instance;

    public TypeVar(String name) {
        this(name, null);
    }

    public TypeVar(String name, Type instance) {
        this.name = name;
        this.instance = Optional.ofNullable(instance);
    }

    public Optional<Type> getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (instance.isPresent()) {
            return String.format("%s (%s)", name, instance.get());
        } else {
            return name;
        }
    }

    public void setInstance(Optional<Type> instance) {
        this.instance = instance;
    }
}
