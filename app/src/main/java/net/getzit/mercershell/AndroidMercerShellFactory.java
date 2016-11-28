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
import android.app.Activity;

import java.io.BufferedReader;
import java.io.PrintStream;

import bsh.EvalError;

public class AndroidMercerShellFactory implements MercerShellFactory {
    private final Activity activity;

    public AndroidMercerShellFactory(Activity activity) {
        this.activity = activity;
    }

    @Override
    public MercerShell createShell(BufferedReader in, PrintStream out) {
        MercerShell shell = new MercerShell(in, out);
        try {
            shell.getShell().set("activity", activity);
        } catch (EvalError e) {
            throw new Error(e);
        }
        return shell;
    }
}
