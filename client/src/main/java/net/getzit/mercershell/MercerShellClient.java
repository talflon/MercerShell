package net.getzit.mercershell;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MercerShellClient {
    public static final int DEFAULT_PORT = 12345;

    private final Terminal terminal;
    private final InputStream remoteInput;
    private final OutputStream remoteOutput;

    public MercerShellClient(Terminal terminal,
                             InputStream remoteInput, OutputStream remoteOutput) {
        this.terminal = terminal;
        this.remoteInput = remoteInput;
        this.remoteOutput = remoteOutput;
    }

    public void run() throws IOException, InterruptedException {
        final Thread consoleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                feedOutput();
            }
        });
        consoleThread.start();
        try {
            feedInput();
        } finally {
            consoleThread.interrupt();
        }
        consoleThread.join(1000);
    }

    protected void feedInput() throws IOException {
        final char[] buffer = new char[4096];
        final InputStreamReader reader = new InputStreamReader(remoteInput);
        final PrintWriter writer = terminal.writer();
        try {
            int numRead;
            while ((numRead = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, numRead);
                writer.flush();
            }
        } finally {
            reader.close();
        }
    }

    protected void feedOutput() {
        final LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        final PrintWriter writer = new PrintWriter(remoteOutput, true);
        try {
            while (!writer.checkError()) {
                try {
                    writer.println(lineReader.readLine("> "));
                } catch (UserInterruptException e) {
                    /* ignore */
                }
            }
        } catch (EndOfFileException e) {
            /* just finish */
        } finally {
            writer.close();
        }
    }

    private static void exitWithUsageError() {
        System.err.println("Takes a single HOST or HOST:PORT argument");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            exitWithUsageError();
        }
        String remoteHost = args[0];
        int port = DEFAULT_PORT;
        if (remoteHost.contains(":")) {
            String[] splitHost = remoteHost.split(":");
            if (splitHost.length != 2) {
                exitWithUsageError();
            } else {
                remoteHost = splitHost[0];
                try {
                    port = Integer.parseInt(splitHost[1]);
                } catch (NumberFormatException e) {
                    exitWithUsageError();
                }
            }
        }

        Terminal terminal = null;
        Socket socket = null;
        try {
            terminal = TerminalBuilder.terminal();
            socket = new Socket(remoteHost, port);
            MercerShellClient client = new MercerShellClient(
                    terminal, socket.getInputStream(), socket.getOutputStream());
            client.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (terminal != null) {
                try {
                    terminal.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
