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
    private Process ghci;
    private InputStream in;
    private OutputStream out;

    private final static char SENTINEL = 0;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    public GhciSession() throws GhciException {
        try {
            ghci = new ProcessBuilder("ghci").start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        in = ghci.getInputStream();
        out = ghci.getOutputStream();

        // Make it so that GHCI prints a null byte to its standard output when
        // it expects input
        eval(":set prompt " + SENTINEL);

        // Make it so that GHCI resets bindings after every command
        eval(":set +r");
    }


    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        ghci.destroy();
    }

    public String eval(String cmd) throws GhciException {
        StringBuilder response = new StringBuilder();

        try {
            out.write(cmd.getBytes(UTF_8));
            out.write('\n');
            out.flush();

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
}
