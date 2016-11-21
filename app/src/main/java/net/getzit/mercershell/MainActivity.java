package net.getzit.mercershell;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

public class MainActivity extends AppCompatActivity {
    private MercerShellServer shellServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shellServer = new MercerShellServer(
                12345, Executors.defaultThreadFactory(), ServerSocketFactory.getDefault());
        try {
            shellServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        shellServer.stop();
        super.onDestroy();
    }
}
