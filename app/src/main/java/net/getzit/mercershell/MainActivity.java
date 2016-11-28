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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

public class MainActivity extends AppCompatActivity {
    static final String LOG_TAG = "MainActivity";

    private MercerShellServer shellServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            shellServer = new MercerShellServer() {
                @Override
                protected void handleClient(Socket socket) throws IOException {
                    ((SSLSocket) socket).setNeedClientAuth(true);
                    super.handleClient(socket);
                }

                @Override
                protected void handleServerError(Throwable error) {
                    Log.e(LOG_TAG, "Server error", error);
                    Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_LONG).show();
                }

                @Override
                protected void handleClientError(Throwable error, Socket socket) {
                    Log.e(LOG_TAG, "Client error", error);
                    Toast.makeText(MainActivity.this, "Client error", Toast.LENGTH_LONG).show();
                }
            };
            shellServer.setPort(getResources().getInteger(R.integer.server_port));
            shellServer.setShellFactory(new AndroidMercerShellFactory(this));
            shellServer.setServerSocketFactory(
                    SslConfig.loadSSLContext(this).getServerSocketFactory());
            shellServer.start();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error starting server", e);
            Toast.makeText(this, "Error starting server", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (shellServer != null) {
            shellServer.stop();
        }
        super.onDestroy();
    }
}
