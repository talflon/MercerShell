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
import java.util.concurrent.TimeUnit;

public abstract class AssertWaiter {
    protected abstract void test() throws Exception;

    public void await(long timeout, TimeUnit unit) throws Exception {
        long endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        AssertionError assertionError;
        do {
            try {
                test();
                return;
            } catch (AssertionError e) {
                assertionError = e;
                Thread.sleep(1);
            }
        } while (System.currentTimeMillis() < endTime);
        throw assertionError;
    }

    public void await() throws Exception {
        await(2, TimeUnit.SECONDS);
    }
}
