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
import android.content.Context;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SslConfig {
    public static SSLContext loadSSLContext(Context context)
            throws GeneralSecurityException, IOException {
        char[] password = context.getString(R.string.keystore_password).toCharArray();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(context.getResources().openRawResource(R.raw.keystore), password);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }
}
