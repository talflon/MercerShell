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
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class MercerShellServer {
    public static final int DEFAULT_PORT = 9923;

    private final Set<Thread> clientThreads = new HashSet<>();
    private int port = DEFAULT_PORT;
    private MercerShellFactory shellFactory = MercerShell.defaultFactory();
    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
    private ServerSocket serverSocket;
    private Thread serverThread;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (serverSocket != null) {
            throw new IllegalStateException();
        }
        this.port = port;
    }

    public MercerShellFactory getShellFactory() {
        return shellFactory;
    }

    public void setShellFactory(MercerShellFactory shellFactory) {
        if (serverSocket != null) {
            throw new IllegalStateException();
        }
        this.shellFactory = shellFactory;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        if (serverSocket != null) {
            throw new IllegalStateException();
        }
        this.threadFactory = threadFactory;
    }

    public ServerSocketFactory getServerSocketFactory() {
        return serverSocketFactory;
    }

    public void setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
        if (serverSocket != null) {
            throw new IllegalStateException();
        }
        this.serverSocketFactory = serverSocketFactory;
    }

    public synchronized void start() throws IOException {
        if (serverSocket != null) {
            throw new IllegalStateException();
        }
        serverSocket = serverSocketFactory.createServerSocket(port);
        boolean started = false;
        try {
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
            started = true;
        } finally {
            if (!started) {
                serverSocket.close();
            }
        }
    }

    public synchronized void stop() {
        if (serverThread != null) {
            serverThread.interrupt();
            for (Thread thread : clientThreads) {
                thread.interrupt();
            }
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
        shellFactory.createShell(in, out).readLoop();
    }

    protected void handleClientError(Throwable error, Socket socket) {
        handleServerError(error);
    }

    protected void handleServerError(Throwable error) {
        error.printStackTrace();
    }

    public static void main(String[] args) throws IOException {
        MercerShellServer server = new MercerShellServer() {
            @Override
            protected void handleClient(Socket socket) throws IOException {
                ((SSLSocket) socket).setNeedClientAuth(true);
                super.handleClient(socket);
            }
        };
        server.setServerSocketFactory(SSLServerSocketFactory.getDefault());
        server.start();
    }
}
