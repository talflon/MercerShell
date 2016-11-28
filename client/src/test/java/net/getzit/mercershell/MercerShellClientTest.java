package net.getzit.mercershell;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MercerShellClientTest {
    ExecutorService executor;

    @Before
    public void setUpExecutor() {
        executor = Executors.newCachedThreadPool();
    }

    @After
    public void tearDownExecutor() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    public void testFeedInput() throws Exception {
        String inputText = "one\ntwo\nthree\n";
        ByteArrayInputStream input = new ByteArrayInputStream(inputText.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Terminal terminal = TerminalBuilder.builder()
                .streams(mock(InputStream.class), output)
                .build();
        MercerShellClient client = new MercerShellClient(terminal, input, mock(OutputStream.class));
        client.feedInput();
        assertEquals(inputText, new String(output.toByteArray()));
    }

    @Test
    public void testFeedOutput() throws Exception {
        final String inputText = "one\ntwo\nthree\n";
        PipedInputStream termInput = new PipedInputStream();
        PipedOutputStream termOutput = new PipedOutputStream();
        PrintWriter termInputWriter = new PrintWriter(new PipedOutputStream(termInput), true);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        Terminal terminal = TerminalBuilder.builder()
                .streams(termInput, termOutput)
                .build();
        final MercerShellClient client = new MercerShellClient(terminal, mock(InputStream.class), output);
        Future<?> feedOutputResult = executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                client.feedOutput();
                return null;
            }
        });
        termInputWriter.print(inputText);
        termInputWriter.flush();
        new AssertWaiter() {
            @Override
            protected void test() throws Exception {
                assertEquals(inputText, new String(output.toByteArray()));
            }
        }.await();

        try {
            feedOutputResult.get(1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            /* ignore */
        }
    }
}
