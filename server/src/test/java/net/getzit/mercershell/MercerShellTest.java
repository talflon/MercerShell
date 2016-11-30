package net.getzit.mercershell;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class MercerShellTest extends MercerShellTestHarness {
    @Test
    public void testAddition() throws Exception {
        term.assertResponse("1+2", "3");
    }

    @Test
    public void testMultipleCommands() throws Exception {
        term.assertResponse("\"hi\"", "hi");
        term.assertResponse("3.5 + 4.5", "8.0");
    }

    public static class TestPojo {
        public volatile int publicField;
        private volatile String privateField;

        public TestPojo(int publicField, String privateField) {
            this.publicField = publicField;
            this.privateField = privateField;
        }
        public TestPojo() { }

        public int getPublicFieldPublic() { return publicField; }
        public void setPublicFieldPublic(int value) { publicField = value; }
    }

    @Test
    public void testConstructPojo() throws Exception {
        term.assertResponse(
                "new net.getzit.mercershell.MercerShellTest.TestPojo(8, \"x\").publicField",
                "8");
    }

    @Test
    public void testPojoPublicFieldGet() throws Exception {
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        pojo.publicField = 13;
        term.assertResponse("pojo.publicField", "13");
    }

    @Test
    public void testPojoPublicFieldSet() throws Exception {
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        term.ignoreResponse("pojo.publicField = 43210");
        assertEquals(43210, pojo.publicField);
    }

    @Test
    public void testPojoPrivateFieldGet() throws Exception {
        term.sendCommand("setAccessibility(true)");
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        pojo.privateField = "oi";
        term.assertResponse("pojo.privateField", "oi");
    }

    @Test
    public void testPojoPrivateFieldSet() throws Exception {
        term.sendCommand("setAccessibility(true)");
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        term.ignoreResponse("pojo.privateField = \"stuff\"");
        assertEquals("stuff", pojo.privateField);
    }

    @Test
    public void testPojoPublicBeanPropGet() throws Exception {
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        pojo.publicField = -100;
        term.assertResponse("pojo.publicFieldPublic", "-100");
    }

    @Test
    public void testPojoPublicBeanPropSet() throws Exception {
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        term.ignoreResponse("pojo.publicFieldPublic = 5");
        assertEquals(5, pojo.publicField);
    }

    @Test
    public void testLocalVariableExternalSet() throws Exception {
        final double value = 13.5;
        shell.getShell().set("x", value);
        term.assertResponse("x", Double.toString(value));
    }

    @Test
    public void testLocalVariableExternalGet() throws Exception {
        term.ignoreResponse("y = -1");
        assertEquals(-1, shell.getShell().get("y"));
    }

    @Test
    public void testPrint() throws Exception {
        term.assertResponse("print(\"hello\");", "hello");
    }

    @Test
    public void testPrintNoJunkValue() throws Exception {
        term.assertResponse("print(\"hello\");", "hello");
        term.assertResponse("12", "12");
    }

    @Test
    public void testSwallowNull() throws Exception {
        term.sendCommand("null");
        term.assertResponse("\"Q\"", "Q");
    }

    @Test
    public void testPrintNull() throws Exception {
        term.assertResponse("print(null)", "null");
    }

    @Test
    public void testScriptedMethod() throws Exception {
        term.sendCommand("long sub(long x, long y) { return x - y; }");
        term.assertResponse("sub(15, 4)", "11");
    }

    @Test
    public void testImportClass() throws Exception {
        term.sendCommand("import java.util.concurrent.atomic.AtomicInteger");
        term.ignoreResponse("ai = new AtomicInteger(4)");
        assertEquals(4, ((AtomicInteger) shell.getShell().get("ai")).get());
    }

    @Test
    public void testImplementInterface() throws Exception {
        term.sendCommand("import java.util.concurrent.Callable");
        term.ignoreResponse("callable = new Callable() { public String call() { return \"yes\"; } }");
        assertEquals("yes", ((Callable) shell.getShell().get("callable")).call());
    }

    @Test
    public void testScriptedObject() throws Exception {
        term.sendCommand("obj() { x = 3; method() { return x; } return this; }");
        term.assertResponse("obj().method()", "3");
    }

    @Test
    public void testMockitoMockWhen() throws Exception {
        term.sendCommand("import org.mockito.Mockito");
        term.ignoreResponse("list = Mockito.mock(List.class)");
        term.ignoreResponse("Mockito.when(list.size()).thenReturn(9)");
        term.assertResponse("list.size()", "9");
    }

    @Test
    public void testMultiLineCall() throws Exception {
        term.sendCommand(MercerShell.MULTILINE_START);
        term.sendCommand("Math.pow(");
        term.sendCommand("2.0,");
        term.sendCommand("4.0)");
        term.assertResponse(MercerShell.MULTILINE_END, "16.0");
    }

    @Test
    public void testMultiLineSet() throws Exception {
        term.sendCommand(MercerShell.MULTILINE_START);
        term.sendCommand("x = ");
        term.sendCommand("  17 ");
        term.ignoreResponse(MercerShell.MULTILINE_END);
        assertEquals(17, shell.getShell().get("x"));
    }

    @Test
    public void testMultiLineMult() throws Exception {
        term.sendCommand(MercerShell.MULTILINE_START);
        term.sendCommand("8");
        term.sendCommand("*3");
        term.assertResponse(MercerShell.MULTILINE_END, "24");
    }

    @Test
    public void testLastResult() throws Exception {
        term.ignoreResponse("15");
        assertEquals(15, shell.getShell().get(MercerShell.LAST_RESULT_VAR));
        term.ignoreResponse("\"abc\" + 123");
        assertEquals("abc123", shell.getShell().get(MercerShell.LAST_RESULT_VAR));
    }

    @Test
    public void testMultilineCancel() throws Exception {
        term.sendCommand(MercerShell.MULTILINE_START);
        term.sendCommand("8 + ");
        term.sendCommand(MercerShell.MULTILINE_CANCEL);
        term.assertResponse("5 - 1", "4");
    }

    @Test
    public void testMultilineAfterCancel() throws Exception {
        term.sendCommand(MercerShell.MULTILINE_START);
        term.sendCommand("abcdefg");
        term.sendCommand("hijklmnop");
        term.sendCommand(MercerShell.MULTILINE_CANCEL);
        term.ignoreResponse("0");
        term.sendCommand(MercerShell.MULTILINE_START);
        term.sendCommand("print(");
        term.sendCommand("\"this\"");
        term.sendCommand(")");
        term.assertResponse(MercerShell.MULTILINE_END, "this");
    }
}
