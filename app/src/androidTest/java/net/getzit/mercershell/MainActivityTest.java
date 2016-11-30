package net.getzit.mercershell;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(MainActivity.class);

    Socket socket;
    ShellTester term;
    ExecutorService executor;

    @Before
    public void connectToServer() throws Exception {
        SSLContext sslContext = SslConfig.loadSSLContext(mActivityRule.getActivity());
        socket = sslContext.getSocketFactory().createSocket(
                "localhost", MercerShellServer.DEFAULT_PORT);
        executor = Executors.newCachedThreadPool();
        term = new ShellTester(
                socket.getInputStream(), socket.getOutputStream(), executor, MercerShell.PROMPT);
    }

    @After
    public void disconnectFromServer() throws InterruptedException {
        boolean outputError = false;
        if (socket != null) {
            if (term != null) {
                outputError = term.close();
                Thread.yield();
            }
            try {
                socket.close();
            } catch (IOException e) {
                /* ignore */
            }
        }
        executor.shutdownNow();
        assertTrue("Something still running", executor.awaitTermination(2, TimeUnit.SECONDS));
        assertFalse("Error in output stream", outputError);
    }

    @Test
    public void shellRunning() throws Exception {
        term.assertResponse("1+2", "3");
    }

    @Test
    public void testGetApplication() throws Exception {
        term.assertResponse("print(activity.application.getClass().simpleName)", "Application");
    }

    @Test
    public void testGetResourceString() throws Exception {
        term.sendCommand("import net.getzit.mercershell.R");
        term.assertResponse("activity.getString(R.string.app_name)", mActivityRule.getActivity().getString(R.string.app_name));
    }
}
