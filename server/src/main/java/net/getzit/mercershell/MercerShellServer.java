package net.getzit.mercershell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import javax.net.ServerSocketFactory;

public class MercerShellServer {
    private final int port;
    private final ThreadFactory threadFactory;
    private final ServerSocketFactory serverSocketFactory;
    private final Set<Thread> clientThreads;
    private ServerSocket serverSocket;
    private Thread serverThread;

    public MercerShellServer(int port, ThreadFactory threadFactory,
                             ServerSocketFactory serverSocketFactory) {
        this.port = port;
        this.threadFactory = threadFactory;
        this.serverSocketFactory = serverSocketFactory;
        clientThreads = new HashSet<>();
    }

    public synchronized void start() throws IOException {
        if (serverSocket != null) {
            throw new IllegalStateException();
        }
        serverSocket = serverSocketFactory.createServerSocket(port);
        serverThread = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    acceptLoop();
                } catch (InterruptedIOException e) {
                    /* ignore */
                } catch (IOException e) {
                    handleServerError(e);
                } finally {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        /* ignore */
                    }
                }
            }
        });
        serverThread.start();
    }

    public synchronized void stop() {
        serverThread.interrupt();
        for (Thread thread : clientThreads) {
            thread.interrupt();
        }
    }

    protected void acceptLoop() throws IOException {
        while (!Thread.interrupted() && !serverSocket.isClosed()) {
            addClient(serverSocket.accept());
        }
    }

    protected synchronized void addClient(final Socket socket) {
        if (serverSocket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                /* ignore */
            }
            return;
        }
        Thread thread = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleClient(socket);
                } catch (IOException e) {
                    handleClientError(e, socket);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        /* ignore */
                    }
                }
            }
        });
        clientThreads.add(thread);
        thread.start();
    }

    protected void handleClient(Socket socket) throws IOException {
        handleClient(
                new BufferedReader(new InputStreamReader(socket.getInputStream())),
                new PrintStream(socket.getOutputStream(), true));
    }

    protected void handleClient(BufferedReader in, PrintStream out) throws IOException {
        MercerShell shell = new MercerShell(in, out);
        shell.readLoop();
    }

    protected void handleClientError(Throwable error, Socket socket) {
        handleServerError(error);
    }

    protected void handleServerError(Throwable error) {
        error.printStackTrace();
    }
}
