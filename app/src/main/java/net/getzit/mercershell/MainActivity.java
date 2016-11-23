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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

import bsh.EvalError;

public class MainActivity extends AppCompatActivity {
    private MercerShellServer shellServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shellServer = new MercerShellServer(
                12345, Executors.defaultThreadFactory(), ServerSocketFactory.getDefault()) {
            @Override
            protected MercerShell createShell(BufferedReader in, PrintStream out) {
                MercerShell shell = super.createShell(in, out);
                try {
                    shell.getShell().set("activity", MainActivity.this);
                } catch (EvalError e) {
                    throw new Error(e);
                }
                return shell;
            }
        };
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
