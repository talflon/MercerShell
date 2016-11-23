package net.getzit.mercershell;

import org.junit.After;
import org.junit.Before;

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

public class MercerShellTestHarness {
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
        assertEquals(expectedOutput, getOutput());
    }

    String getOutput() throws Exception {
        Future<String> result = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return shellOut.readLine();
            }
        });
        try {
            return result.get(2, TimeUnit.SECONDS);
        } finally {
            result.cancel(true);
        }
    }

    void runSingleCommand(String input) throws Exception {
        shellIn.println(input);
        getOutput();
    }
}
