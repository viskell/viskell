package nl.utwente.group10;

import nl.utwente.group10.ghcj.GhciSession;

public class App {
    public static void main(String[] args) throws Exception {
        GhciSession session = new GhciSession();
        session.close();
    }
}
