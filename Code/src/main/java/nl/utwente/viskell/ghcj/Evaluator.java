package nl.utwente.viskell.ghcj;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

abstract public class Evaluator {
    /** Responses from ghci are terminated by a null byte. */
    protected static final char SENTINEL = 0;

    /** All communication is done over UTF_8. */
    protected static final Charset UTF_8 = StandardCharsets.UTF_8;

    /** Raw input stream for result data from ghci to the application. */
    protected final InputStream in;

    /** Raw output stream from the application to ghci. */
    protected final OutputStream out;

    /** A newline character. */
    protected final String NL;

    public Evaluator() throws HaskellException {
        this.NL = System.getProperty("line.separator");

        try {
            /* The ghci process to use. */
            Process process = new ProcessBuilder(getCommand())
                    .redirectErrorStream(true)
                    .start();

            this.in = process.getInputStream();
            this.out = process.getOutputStream();
        } catch (IOException io) {
            throw new HaskellException(io);
        }

        /* Make it so that GHCi prints a null byte to its standard output when
           it expects input. By setting the prompt to a zero byte, GHCi will
           print a zero byte whenever it expects the user (that's us) to enter
           the next expression. In UTF-8, zero bytes are not part of any
           character except for NUL, the zero character, which makes them a
           useful sentinel. */
        this.eval(":set prompt " + SENTINEL);

        /* Load some useful modules. */
        this.eval(":module " + Joiner.on(" ").join(getModules()));

        /* Make it so that GHCi resets bindings after every command. This makes
           it slightly less likely that GHCi state will affect our results. */
        this.eval(":set +r");
    }

    /**
     * Destroys the ghci instance and closes communications channels.
     * @throws IOException when closing the channels fails.
     */
    public final void close() throws HaskellException {
        try {
            this.in.close();
            this.out.close();
        } catch (IOException e) {
            throw new HaskellException(e);
        }
    }

    /**
     * Evaluates a Haskell expression and wait for it to compute.
     *
     * @param cmd The (complete) Haskell
     * @return the result, including newline, as a string.
     * @throws HaskellException when ghci is not ready to evaluate, or expression can not be computed.
     */
    public final String eval(final String cmd) throws HaskellException {
        StringBuilder responseBuilder = new StringBuilder();

        try {
            // Send the expression to ghci.
            this.out.write(cmd.getBytes(UTF_8));
            this.out.write('\n');
            this.out.flush();

            // Wait for the sentinel.
            int input;
            while ((input = this.in.read()) != 0) {
                responseBuilder.append((char) input);
            }
        } catch (IOException e) {
            throw new HaskellException(e);
        }

        String response = responseBuilder.toString();

        // Check for hints that something went wrong
        // To do: Make this better

        String exceptionHeader = "*** Exception: ";
        String parseErrorHeader = "<interactive>";

        List<String> lines = Splitter.on(this.NL).splitToList(response);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.startsWith(exceptionHeader)) {
                String msg = line.substring(exceptionHeader.length());
                throw new HaskellException(msg);
            }

            if (line.startsWith(parseErrorHeader)) {
                List<String> sublines = lines.subList(i, lines.size());
                String msg = Joiner.on(this.NL).join(sublines);
                throw new HaskellException(msg);
            }
        }

        return response;
    }

    protected abstract List<String> getCommand();
    protected abstract List<String> getModules();
}
