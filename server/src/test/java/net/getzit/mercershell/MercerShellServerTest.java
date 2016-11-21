package net.getzit.mercershell;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ServerSocketFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MercerShellServerTest {
    BlockingQueue<Socket> sockets;
    MercerShellServer shellServer;
    Set<Thread> createdThreads;
    AtomicReference<Throwable> asyncError;

    @Before
    public void setUpServer() throws Exception {
        createdThreads = Collections.synchronizedSet(new HashSet<Thread>());
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                createdThreads.add(thread);
                return thread;
            }
        };
        asyncError = new AtomicReference<>();

        sockets = new LinkedBlockingQueue<>();
        ServerSocket mockServerSocket = mock(ServerSocket.class);
        when(mockServerSocket.accept()).then(new Answer<Socket>() {
            @Override
            public Socket answer(InvocationOnMock invocation) throws InterruptedIOException {
                try {
                    return sockets.take();
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }
        });
        ServerSocketFactory mockServerSocketFactory = mock(ServerSocketFactory.class);
        when(mockServerSocketFactory.createServerSocket(anyInt())).thenReturn(mockServerSocket);

        shellServer = new MercerShellServer(
                12345, threadFactory, mockServerSocketFactory) {
            @Override
            protected void handleClient(BufferedReader in, PrintWriter out) throws IOException {
                out.println(in.readLine());
            }

            @Override
            protected void handleClientError(Throwable error, Socket socket) {
                handleServerError(error);
            }

            @Override
            protected void handleServerError(Throwable error) {
                asyncError.compareAndSet(null, error);
            }
        };
    }

    @After
    public void tearDownServer() throws InterruptedException {
        if (shellServer != null) {
            shellServer.stop();
        }
        synchronized (createdThreads) {
            for (Thread thread : createdThreads) {
                thread.interrupt();
            }
            for (Thread thread : createdThreads) {
                thread.join();
            }
        }
    }

    @After
    public void checkAsyncError() throws Throwable {
        if (asyncError.get() != null) {
            throw asyncError.get();
        }
    }

    @Test
    public void testConnect() throws Exception {
        final String text = "test\n";

        shellServer.start();

        Socket mockSocket = mock(Socket.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(out);
        ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes("UTF-8"));
        when(mockSocket.getInputStream()).thenReturn(in);
        sockets.add(mockSocket);

        new AssertWaiter() {
            @Override
            protected void test() throws Exception {
                assertEquals(text, out.toString());
            }
        }.await();
    }
}
