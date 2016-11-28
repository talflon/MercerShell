package net.getzit.mercershell;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.net.ssl.SSLContext;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(MainActivity.class);

    Socket socket;
    PrintWriter out;
    BufferedReader in;

    @Before
    public void connectToServer() throws Exception {
        SSLContext sslContext = SslConfig.loadSSLContext(mActivityRule.getActivity());
        socket = sslContext.getSocketFactory().createSocket(
                "localhost", MercerShellServer.DEFAULT_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @After
    public void disconnectFromServer() {
        if (socket != null) {
            if (out != null) {
                assertFalse("Error in output stream", out.checkError());
            }
            try {
                socket.close();
            } catch (IOException e) {
                /* ignore */
            }
        }
    }

    @Test
    public void shellRunning() throws Exception {
        out.println("1+2");
        assertEquals("3", in.readLine());
    }

    @Test
    public void testGetApplication() throws Exception {
        out.println("print(activity.application.getClass().simpleName)");
        assertEquals("Application", in.readLine());
    }

    @Test
    public void testGetResourceString() throws Exception {
        out.println("import net.getzit.mercershell.R");
        out.println("activity.getString(R.string.app_name)");
        assertEquals(mActivityRule.getActivity().getString(R.string.app_name), in.readLine());
    }
}
