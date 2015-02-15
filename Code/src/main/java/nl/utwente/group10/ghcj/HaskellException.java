package nl.utwente.group10.ghcj;

/**
 * Exception that is thrown when Ghci balks on our input.
 */
public class HaskellException extends GhciException {
    public HaskellException(Exception e) {
        super(e);
    }
}
