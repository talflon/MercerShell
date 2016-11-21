package net.getzit.mercershell;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MercerShellTest {
    BufferedReader shellOut;
    PrintWriter shellIn;
    MercerShell shell;
    ExecutorService executor;

    @Before
    public void setUpShell() throws IOException {
        executor = Executors.newCachedThreadPool();
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        shell = new MercerShell(
                new BufferedReader(new InputStreamReader(new PipedInputStream(pipeOut))),
                new PrintStream(new PipedOutputStream(pipeIn), true));
        shellOut = new BufferedReader(new InputStreamReader(pipeIn));
        shellIn = new PrintWriter(new OutputStreamWriter(pipeOut), true);
        executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                shell.readLoop();
                return null;
            }
        });
    }

    @After
    public void tearDownShell() throws InterruptedException {
        boolean shellInError = false;
        if (shellIn != null) {
            shellInError = shellIn.checkError();
            shellIn.close();
            Thread.yield();
        }
        executor.shutdownNow();
        assertTrue("Something still running", executor.awaitTermination(2, TimeUnit.SECONDS));
        assertFalse("Error in output stream", shellInError);
    }

    void testSingleCommand(String input, String expectedOutput) throws Exception {
        shellIn.println(input);
        assertOutput(expectedOutput);
    }

    void assertOutput(String expectedOutput) throws Exception {
        Future<String> result = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return shellOut.readLine();
            }
        });
        try {
            assertEquals(expectedOutput, result.get(2, TimeUnit.SECONDS));
        } finally {
            result.cancel(true);
        }
    }

    @Test
    public void testAddition() throws Exception {
        testSingleCommand("1+2", "3");
    }

    @Test
    public void testMultipleCommands() throws Exception {
        testSingleCommand("\"hi\"", "hi");
        testSingleCommand("3.5 + 4.5", "8.0");
    }

    public static class TestPojo {
        public int publicField;
        private String privateField;

        public TestPojo(int publicField, String privateField) {
            this.publicField = publicField;
            this.privateField = privateField;
        }
    }

    @Test
    public void testConstructPojo() throws Exception {
        testSingleCommand(
                "new net.getzit.mercershell.MercerShellTest.TestPojo(8, \"x\").publicField",
                "8");
    }

    @Test
    public void testLocalVariableExternalSet() throws Exception {
        final double value = 13.5;
        shell.getShell().set("x", value);
        testSingleCommand("x", Double.toString(value));
    }

    @Test
    public void testLocalVariableExternalGet() throws Exception {
        testSingleCommand("y = -1", "-1");
        assertEquals(-1, shell.getShell().get("y"));
    }

    @Test
    public void testPrint() throws Exception {
        shellIn.println("print(\"hello\");");
        assertOutput("hello");
    }

    @Test
    public void testPrintNoJunkValue() throws Exception {
        shellIn.println("print(\"hello\");");
        assertOutput("hello");
        shellIn.println("12");
        assertOutput("12");
    }

    @Test
    public void testSwallowNull() throws Exception {
        shellIn.println("null");
        shellIn.println("\"Q\"");
        assertOutput("Q");
    }

    @Test
    public void testPrintNull() throws Exception {
        shellIn.println("print(null)");
        assertOutput("null");
    }
}
