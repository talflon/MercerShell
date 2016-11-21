package net.getzit.mercershell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import bsh.Interpreter;

public class MercerShell {
    private final BufferedReader in;
    private final PrintStream out;
    private final Interpreter shell;

    public MercerShell(BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
        shell = new Interpreter(in, out, out, false);
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
