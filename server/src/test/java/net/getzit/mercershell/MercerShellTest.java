package net.getzit.mercershell;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class MercerShellTest {
    @Test
    public void testAddition() throws IOException {
        StringWriter out = new StringWriter();
        MercerShell shell = new MercerShell(
                new BufferedReader(new StringReader("1+2\n")),
                new PrintWriter(out));
        shell.readLoop();
        assertEquals("3\n", out.toString());
    }
}
