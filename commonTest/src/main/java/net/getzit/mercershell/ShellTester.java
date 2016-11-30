package net.getzit.mercershell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class ShellTester {
    private final BufferedReader in;
    private final PrintWriter out;
    private final ExecutorService executor;
    private final String prompt;

    public ShellTester(InputStream in, OutputStream out, ExecutorService executor, String prompt) {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = new PrintWriter(new OutputStreamWriter(out), true);
        this.executor = executor;
        this.prompt = prompt;
    }

    public void sendCommand(String text)
            throws InterruptedException, IOException, TimeoutException {
        sendLine(text);
        expectPrompt();
    }

    public void assertResponse(String command, String expectedReponse)
            throws InterruptedException, IOException, TimeoutException {
        sendCommand(command);
        expectLine(expectedReponse);
    }

    public void ignoreResponse(String command)
            throws InterruptedException, IOException, TimeoutException {
        sendLine(command);
        readLine();
    }

    public void expectPrompt() throws InterruptedException, IOException, TimeoutException {
        expectText(prompt);
    }

    public void sendText(String text) {
        assertFalse("Error in output stream", out.checkError());
        out.print(text);
    }

    public void sendLine(String text) {
        assertFalse("Error in output stream", out.checkError());
        out.println(text);
    }

    private IOException unwrapIOException(ExecutionException e) throws IOException {
        try {
            throw e.getCause();
        } catch (IOException|RuntimeException|Error e2) {
            throw e2;
        } catch (Throwable throwable) {
            throw new Error(throwable);
        }
    }

    public void expectText(final String expected)
            throws InterruptedException, IOException, TimeoutException {
        Future<?> result = executor.submit(new Callable<Object>() {
            @Override
            public String call() throws IOException {
                char[] charsRead = new char[expected.length()];
                in.read(charsRead);
                return new String(charsRead);
            }
        });
        try {
            assertEquals(expected, result.get(2, TimeUnit.SECONDS));
        } catch (ExecutionException e) {
            throw unwrapIOException(e);
        } finally {
            result.cancel(true);
        }
    }

    public void expectLine(String expected)
            throws InterruptedException, IOException, TimeoutException {
        assertEquals(expected, readLine());
    }

    public String readLine() throws InterruptedException, IOException, TimeoutException {
        Future<String> result = executor.submit(new Callable<String>() {
            @Override
            public String call() throws IOException {
                return in.readLine();
            }
        });
        try {
            return result.get(2, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw unwrapIOException(e);
        } finally {
            result.cancel(true);
        }
    }

    /**
     * Closes the output to the shell.
     * @return true if there was an error in the output stream
     */
    public boolean close() {
        out.close();
        return out.checkError();
    }
}
