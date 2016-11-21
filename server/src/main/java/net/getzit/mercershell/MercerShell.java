package net.getzit.mercershell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import bsh.Interpreter;

public class MercerShell {
    private final BufferedReader in;
    private final PrintWriter out;
    private final Interpreter shell;

    public MercerShell(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
        shell = new Interpreter();
    }

    public Interpreter getShell() {
        return shell;
    }

    public void readLoop() throws IOException {
        while (!Thread.interrupted()) {
            String line = in.readLine();
            if (line == null) {
                break;
            }
            try {
                Object result = shell.eval(line);
                displayResult(result);
            } catch (Throwable t) {
                displayError(t);
            }
        }
    }

    protected void displayResult(Object result) {
        out.println(result);
    }

    protected void displayError(Throwable error) {
        displayResult(error);
    }
}
