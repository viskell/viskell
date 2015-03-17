package nl.utwente.group10.haskell.exceptions;

import nl.utwente.group10.haskell.HaskellObject;

public class CatalogException extends HaskellException {
    public CatalogException(Exception e) {
        super(e);
    }

    public CatalogException(String msg, HaskellObject obj) {
        super(msg, obj);
    }

    public CatalogException(String msg) {
        super(msg);
    }
}
