package nl.utwente.group10.ghcj;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A conversation with an instance of ghci.
 */
public class GhciSession implements Closeable {
    /** The process this GhciSession will communicate with. */
    private Process ghci;

    /** Raw input stream for result data from ghci to the application. */
    private InputStream in;

    /** Raw output stream from the application to ghci. */
    private OutputStream out;

    /** Responses from ghci are terminated by a null byte. */
    private final static char SENTINEL = 0;

    /** All communication is done over UTF_8. */
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * Builds a new communication session with ghci.
     *
     * @throws GhciException when ghci can not be found, can not be executed,
     *         or does not understand our setup sequence.
     */
    public GhciSession() throws GhciException {
        try {
            ghci = new ProcessBuilder("ghci").start();
            in = ghci.getInputStream();
            out = ghci.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Make it so that GHCI prints a null byte to its standard output when
            // it expects input
            eval(":set prompt " + SENTINEL);

            // Make it so that GHCI resets bindings after every command
            eval(":set +r");
        } catch (HaskellException e) {
            throw new GhciException(e);
        }
    }


    /**
     * Evaluate a Haskell expression and wait for it to compute.
     *
     * @param cmd The (complete) Haskell
     * @return the result, including newline, as a string.
     * @throws GhciException when ghci is not ready to evaluate the expression.
     * @throws HaskellException when the expression can not be computed.
     */
    public String eval(String cmd) throws GhciException, HaskellException {
        StringBuilder response = new StringBuilder();

        try {
            // Send the expression to ghci.
            out.write(cmd.getBytes(UTF_8));
            out.write('\n');
            out.flush();

            // Wait for the sentinel.
            while (true) {
                int ch = in.read();

                if (ch == 0) {
                    break;
                } else {
                    response.append((char) ch);
                }
            }
        } catch (IOException e) {
            throw new GhciException(e);
        }

        return response.toString();
    }

    /**
     * Destroys the ghci instance and closes communications channels.
     * @throws IOException when closing the channels fails.
     */
    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        ghci.destroy();
    }
}
