package net.getzit.mercershell;

import java.io.BufferedReader;
import java.io.PrintStream;

public interface MercerShellFactory {
    MercerShell createShell(BufferedReader in, PrintStream out);
}
