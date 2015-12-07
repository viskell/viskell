package nl.utwente.viskell.haskell.env;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeCon;

/** All information about a haskell datatype. */
public class DataTypeInfo {
    
    public final class Constructor {
        /** Haskell constructor name */
        private final String name;
        
        /** type of the constructor when used as a function */
        private final Type type;

        private Constructor(String name, Type type) {
            super();
            this.name = name;
            this.type = type;
        }

        /** @return the haskell name of the constructor */
        public String getName() {
            return name;
        }

        /** @return the type of the constructor when used as a function */
        public Type getType() {
            return type;
        }
        
        /** @return the datatype of which this constructor is part of */
        public DataTypeInfo getDataType() {
            return DataTypeInfo.this;
        }
        
        @Override
        public String toString() {
            return this.name + " :: " + this.type.prettyPrint();
        }
    }

    /** type constructor corresponding to this datatype */
    private final TypeCon typecon;
    
    /** number of type arguments of this datatype */
    private final int typeArity;
    
    /** whether this is a builtin datatype and thus can't be matched on */
    private final boolean builtin;
    
    /** list of data constructors */
    private final List<Constructor> constructors;

    public DataTypeInfo(TypeCon typecon, int typeArity, boolean builtin) {
        super();
        this.typecon = typecon;
        this.typeArity = typeArity;
        this.builtin = builtin;
        this.constructors = new ArrayList<>();
    }
    
    /**
     * Add a constructor to this data type
     * @param name of the constructor
     * @param type as function of the constructor
     */
    protected void addConstructor(String name, Type type) {
        this.constructors.add(this.new Constructor(name, type));
    }

    /** @return the type constructor corresponding to this datatype */
    public TypeCon getTypecon() {
        return typecon;
    }

    /** @return the number of type arguments of this datatype */
    public int getTypeArity() {
        return typeArity;
    }

    /** @return whether this is a builtin datatype and thus can't be matched on */
    public boolean isBuiltin() {
        return builtin;
    }

    /** @return the list of all data constructors */
    public List<Constructor> getConstructors() {
        return constructors;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("data ");
        sb.append(this.typecon.prettyPrint());
        sb.append(" :: ");
        sb.append(Strings.repeat("* -> ", this.typeArity));
        sb.append("*");
        if (!this.builtin) {
            sb.append(" where");
            for (Constructor cons : this.constructors) {
                sb.append("\n\t");
                sb.append(cons.toString());
            }  
        }
        
        return sb.toString();
    }
}
