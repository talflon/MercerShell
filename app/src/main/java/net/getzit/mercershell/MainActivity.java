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
