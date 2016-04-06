package nl.utwente.viskell.haskell.env;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.serialize.Bundleable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class FunctionInfo {

    private static final Map<String, String> subClassMap;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put(CatalogFunction.class.getSimpleName(), CatalogFunction.class.getName());
        subClassMap = Collections.unmodifiableMap(aMap);
    }

    /** The function name. */
    protected final String name;
    
    /** The type signature the corresponding function. */
    protected final Type signature;
    
    /**
     * @param name The function name.
     * @param signature The type signature the corresponding function.
     */
    protected FunctionInfo(String name, Type signature) {
        this.name = name;
        this.signature = signature;
    }

    public abstract Map<String, Object> toBundleFragment();

    /** return a new instance of this type deserializing class-specific properties used in constructor **/
    public static FunctionInfo fromBundleFragment(Map<String,Object> bundleFragment) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String kind = (String)bundleFragment.get(Bundleable.KIND);
        String className = subClassMap.get(kind);
        Class<?> clazz = Class.forName(className);

        // Find the static "fromBundleFragment" method for the named type and call it
        Method fromBundleMethod = clazz.getDeclaredMethod("fromBundleFragment", Map.class);
        return (FunctionInfo)fromBundleMethod.invoke(null, bundleFragment);
    }

    /** @return The internal name of this function. */
    public final String getName() {
        return this.name;
    }

    /** @return The name of this function used for the front-end. */
    public String getDisplayName() {
        return getName();
    }

    /** @return The a fresh copy of type signature of this function. */
    public final Type getFreshSignature() {
        return this.signature.getFresh();
    }
    
    /** @return the number of argument this function can take. */
    public int argumentCount() {
        return this.signature.countArguments();
    }

}
