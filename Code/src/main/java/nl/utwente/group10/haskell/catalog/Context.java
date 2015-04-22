package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;

import java.util.Map;

/**
 * Context for the catalog.
 */
public class Context {
    /** Available type classes. */
    Map<String, TypeClass> typeClasses;

    /** Available types. */
    Map<String, Type> types;
}
