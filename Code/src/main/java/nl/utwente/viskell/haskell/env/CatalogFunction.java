package nl.utwente.viskell.haskell.env;

import com.google.common.base.MoreObjects;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.serialize.Bundleable;

import java.util.HashMap;
import java.util.Map;

/**
 * A function entry in the Haskell catalog.
 */
public class CatalogFunction extends FunctionInfo implements Comparable<CatalogFunction> {
    /** The category this function belongs to. */
    private final String category;

    /** The documentation string for this Entry. */
    private final String documentation;

    /** Whether this function is a constructor **/
    private final boolean isConstructor;

    /** Whether this function is common **/
    private final boolean isCommon;
    
    /**
     * @param name The function name.
     * @param category The category this function belongs to.
     * @param signature The type signature for this function.
     * @param documentation The documentation for this function.
     */
    CatalogFunction(String name, String category, Type signature, String documentation, boolean isConstructor, boolean isCommon) {
        super(name, signature);
        this.category = category;
        this.documentation = documentation;
        this.isConstructor = isConstructor;
        this.isCommon = isCommon;
    }

    @Override
    public Map<String, Object> toBundleFragment() {
        Map<String, Object> bundleFragment = new HashMap<>();
        bundleFragment.put(Bundleable.KIND, this.getClass().getSimpleName());
        bundleFragment.put("name", name);
        bundleFragment.put("signature", signature.toString());
        bundleFragment.put("category", category);
        bundleFragment.put("documentation", documentation);
        bundleFragment.put("isConstructor", isConstructor);
        bundleFragment.put("isCommon", isCommon);
        return bundleFragment;
    }

    /** return a new instance of this type deserializing class-specific properties used in constructor **/
    public static CatalogFunction fromBundleFragment(Map<String,Object> bundleFragment) {
        String name = (String)bundleFragment.get("name");
        // TODO Create real Type from String
        // Type signature = (Type)bundleFragment.get("signature");
        String category = (String)bundleFragment.get("category");
        String documentation = (String)bundleFragment.get("documentation");
        Boolean isConstructor = (Boolean)bundleFragment.get("isConstructor");
        Boolean isCommon = (Boolean)bundleFragment.get("isCommon");
        return new CatalogFunction(name, category, null /* TODO */, documentation, isConstructor, isCommon);
    }

    /**
     * @return The category of this function.
     */
    public final String getCategory() {
        return this.category;
    }

    /**
     * @return The documentation of this function.
     */
    public final String getDocumentation() {
        return this.documentation;
    }

    @Override
    public final int compareTo(final CatalogFunction entry) {
        if (this.isCommon == entry.isCommon) {
            return this.getName().compareTo(entry.getName());
        } else {
            return this.isCommon ? -1 : 1;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", getName())
                .add("category", getCategory())
                .toString();
    }

    public boolean isConstructor() {
        return isConstructor;
    }

}
