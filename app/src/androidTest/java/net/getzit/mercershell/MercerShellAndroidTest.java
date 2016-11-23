package net.getzit.mercershell;

import android.text.InputFilter;

import org.junit.Test;

import static org.junit.Assert.*;

public class MercerShellAndroidTest extends MercerShellTestHarness {
    @Test
    public void testImplementAndroidInterface() throws Exception {
        shellIn.println("import android.text.*");
        runSingleCommand("filter = new InputFilter() {"
                + " public CharSequence filter(CharSequence source, int start, int end,"
                + " Spanned dest, int dstart, int dend) {"
                + " return source; } }");
        assertEquals("same", ((InputFilter) shell.getShell().get("filter")).filter(
                "same", 0, 0, null, 0, 0));
    }
}
