package net.getzit.mercershell;

import org.junit.After;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MercerShellTestHarness {
    MercerShell shell;
    ShellTester term;
    ExecutorService executor;

    @Before
    public void setUpShell() throws IOException {
        executor = Executors.newCachedThreadPool();
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        shell = new MercerShell(
                new BufferedReader(new InputStreamReader(new PipedInputStream(pipeOut))),
                new PrintStream(new PipedOutputStream(pipeIn), true));
        term = new ShellTester(pipeIn, pipeOut, executor, MercerShell.PROMPT);
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
        boolean outputError = false;
        if (term != null) {
            outputError = term.close();
            Thread.yield();
        }
        executor.shutdownNow();
        assertTrue("Something still running", executor.awaitTermination(2, TimeUnit.SECONDS));
        assertFalse("Error in output stream", outputError);
    }
}
