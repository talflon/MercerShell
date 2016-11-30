# MercerShell
A remote REPL for exercising Android's Java API.

Runs a (slightly modified) [BeanShell](https://github.com/beanshell/beanshell)
shell on your Android device, which is accessed remotely.

## Notes

Currently uses [JLine 3.x](https://github.com/jline/jline3) on the client side.

Currently has a lot of rough edges, hence version 0.1.
In theory, it should work on most platforms that support the Android SDK,
and on Android versions 2.3.3+.

**THIS PROGRAM EXECUTES REMOTE CODE.** This is why it uses TLS, and
authenticates connections with a self-signed certificate.
Keep this certificate safe. Keep the APK, which contains the certificate's
private key and password, safe. Close the app when not in use.
Uninstall the app when no longer needed, or when you think its certificate may
no longer be safe.

## How to build

To build and use MercerShell, you will need a Java SDK version 7 or greater,
and the Android SDK's Build Tools version 25.0.1.

Before building anything, you need to create a couple files.
First, create a [PKCS#12](https://en.wikipedia.org/wiki/PKCS_12) file with a
self-signed certificate. This certificate will be used:

- to sign the server app
- to authenticate the server app to the client
- to authenticate the client to the server app

This file can have any name, and be in any location, but it's simplest to keep
it in the base directory of the project. For example:

    $ keytool -genkeypair -keystore keystore.p12 -storetype PKCS12 \
              -keyalg RSA -keysize 2048 -validity 10000 \
              -alias myalias -storepass mypassword

Next, create a file in the base directory of the project named
`keystore.properties` to tell Gradle how to access the keystore:

    storeFile=keystore.p12
    keyAlias=myalias
    storePassword=mypassword

Now, you should be able to build the project:

    $ ./gradlew build

## How to start

Once built, you can install the app to your device. This is easier with adb:

    $ adb install -r app/build/outputs/apk/app-release.apk

Open the app on your device (it doesn't look like much, I know), and find its
IP address on the local network. Let's pretend that address is 192.168.1.234.
Now you should be able to connect to it with the client:

    $ ./gradlew installDist
    $ client/build/install/client/bin/client 192.168.1.234
    > 1 + 1
    2
    >

You can also run the server on a computer with Java installed.
To run it on the same computer you're building it on, you can either run:

    $ server/build/install/server/bin/server

or

    $ ./gradlew -q :server:run

and then connect to it with the client:

    $ client/build/install/client/bin/client localhost

## How to use

MercerShell, by using BeanShell, has many of the features of BeanShell.
BeanShell's syntax is mostly the same as Java's, so if you're developing
Android with Java, you already know how to do a lot of things with BeanShell.
For more features and syntax, you might want to read
[the BeanShell documentation](https://github.com/beanshell/beanshell/wiki).

Unlike normal BeanShell, where each line is a statement,
each line in MercerShell can be either an expression or a statement.
If, treated as an expression, your line has a non-`null` value,
you will see it output to you:

    > "hello" + " world"
    hello world
    > x = 5 * 2
    10
    > x - 1
    9

The last non-`null` expression result is stored in the variable `RESULT`:

    > 2 + 2
    4
    > RESULT + 1
    5

When run on your device, the variable `activity` is a reference to the activity:

    > print(activity.getClass().name)
    net.getzit.mercershell.MainActivity

MercerShell includes the [Mockito](http://mockito.org) library,
so you can `mock()` classes:

    > import org.mockito.Mockito
    > import java.net.Socket
    > import java.io.*
    > mockSocket = Mockito.mock(Socket.class)
    Mock for Socket, hashCode: 1110281064
    > Mockito.when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("hello".bytes))
    org.mockito.internal.stubbing.ConsecutiveStubbing@41c5bdf8
    > (char) mockSocket.inputStream.read()
    h

### Multi-line code

MercerShell currently uses a quick hack to enable multi-line code. This will be
replaced with something better in the future. To write multiple lines code,
start and end them with `##` on a line of its own:

    > ##
    > sayHi() {
    >   print("Hi!");
    > }
    > ##
    > sayHi()
    Hi!

If you've started writing multi-line code, but wish to cancel, send `#-`
on a line of its own:

    > ##
    > x = 5 + 3z
    > oops
    > #-
    > x = 5 + 3
    8
