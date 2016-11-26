package net.getzit.mercershell;
/*
Copyright (C) 2016  Daniel Getz

This file is part of MercerShell.

MercerShell is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MercerShell is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MercerShell.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import bsh.Interpreter;

public class MercerShell {
    public static final String MULTILINE_START = "##";
    public static final String MULTILINE_END = MULTILINE_START;

    protected final BufferedReader in;
    protected final PrintStream out;
    private final Interpreter shell;
    private String cmdBuffer;

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
            if (out.checkError()) {
                throw new IOException("Error in output stream");
            }
            String line = in.readLine();
            if (line == null) {
                break;
            } else if (cmdBuffer != null) {
                if (MULTILINE_END.equals(line)) {
                    line = cmdBuffer;
                    cmdBuffer = null;
                } else {
                    cmdBuffer += line;
                    continue;
                }
            } else if (MULTILINE_START.equals(line)) {
                cmdBuffer = "";
                continue;
            }
            try {
                Object result = shell.eval(line);
                if (result != null) {
                    displayResult(result);
                }
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
