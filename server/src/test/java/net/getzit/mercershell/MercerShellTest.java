package net.getzit.mercershell;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class MercerShellTest extends MercerShellTestHarness {
    @Test
    public void testAddition() throws Exception {
        testSingleCommand("1+2", "3");
    }

    @Test
    public void testMultipleCommands() throws Exception {
        testSingleCommand("\"hi\"", "hi");
        testSingleCommand("3.5 + 4.5", "8.0");
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
        testSingleCommand(
                "new net.getzit.mercershell.MercerShellTest.TestPojo(8, \"x\").publicField",
                "8");
    }

    @Test
    public void testPojoPublicFieldGet() throws Exception {
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        pojo.publicField = 13;
        testSingleCommand("pojo.publicField", "13");
    }

    @Test
    public void testPojoPublicFieldSet() throws Exception {
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        runSingleCommand("pojo.publicField = 43210");
        assertEquals(43210, pojo.publicField);
    }

    @Test
    public void testPojoPrivateFieldGet() throws Exception {
        shellIn.println("setAccessibility(true)");
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        pojo.privateField = "oi";
        testSingleCommand("pojo.privateField", "oi");
    }

    @Test
    public void testPojoPrivateFieldSet() throws Exception {
        shellIn.println("setAccessibility(true)");
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        runSingleCommand("pojo.privateField = \"stuff\"");
        assertEquals("stuff", pojo.privateField);
    }

    @Test
    public void testPojoPublicBeanPropGet() throws Exception {
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        pojo.publicField = -100;
        testSingleCommand("pojo.publicFieldPublic", "-100");
    }

    @Test
    public void testPojoPublicBeanPropSet() throws Exception {
        TestPojo pojo = new TestPojo();
        shell.getShell().set("pojo", pojo);
        runSingleCommand("pojo.publicFieldPublic = 5");
        assertEquals(5, pojo.publicField);
    }

    @Test
    public void testLocalVariableExternalSet() throws Exception {
        final double value = 13.5;
        shell.getShell().set("x", value);
        testSingleCommand("x", Double.toString(value));
    }

    @Test
    public void testLocalVariableExternalGet() throws Exception {
        runSingleCommand("y = -1");
        assertEquals(-1, shell.getShell().get("y"));
    }

    @Test
    public void testPrint() throws Exception {
        shellIn.println("print(\"hello\");");
        assertOutput("hello");
    }

    @Test
    public void testPrintNoJunkValue() throws Exception {
        shellIn.println("print(\"hello\");");
        assertOutput("hello");
        shellIn.println("12");
        assertOutput("12");
    }

    @Test
    public void testSwallowNull() throws Exception {
        shellIn.println("null");
        shellIn.println("\"Q\"");
        assertOutput("Q");
    }

    @Test
    public void testPrintNull() throws Exception {
        shellIn.println("print(null)");
        assertOutput("null");
    }

    @Test
    public void testScriptedMethod() throws Exception {
        shellIn.println("long sub(long x, long y) { return x - y; }");
        testSingleCommand("sub(15, 4)", "11");
    }

    @Test
    public void testImportClass() throws Exception {
        shellIn.println("import java.util.concurrent.atomic.AtomicInteger");
        runSingleCommand("ai = new AtomicInteger(4)");
        assertEquals(4, ((AtomicInteger) shell.getShell().get("ai")).get());
    }

    @Test
    public void testImplementInterface() throws Exception {
        shellIn.println("import java.util.concurrent.Callable");
        runSingleCommand("callable = new Callable() { public String call() { return \"yes\"; } }");
        assertEquals("yes", ((Callable) shell.getShell().get("callable")).call());
    }

    @Test
    public void testScriptedObject() throws Exception {
        shellIn.println("obj() { x = 3; method() { return x; } return this; }");
        testSingleCommand("obj().method()", "3");
    }

    @Test
    public void testMockitoMockWhen() throws Exception {
        shellIn.println("import org.mockito.Mockito");
        runSingleCommand("list = Mockito.mock(List.class)");
        runSingleCommand("Mockito.when(list.size()).thenReturn(9)");
        testSingleCommand("list.size()", "9");
    }

    @Test
    public void testMultiLineCall() throws Exception {
        shellIn.println(MercerShell.MULTILINE_START);
        shellIn.println("Math.pow(");
        shellIn.println("2.0,");
        shellIn.println("4.0)");
        shellIn.println(MercerShell.MULTILINE_END);
        assertOutput("16.0");
    }

    @Test
    public void testMultiLineSet() throws Exception {
        shellIn.println(MercerShell.MULTILINE_START);
        shellIn.println("x = ");
        shellIn.println("  17 ");
        shellIn.println(MercerShell.MULTILINE_END);
        getOutput();
        assertEquals(17, shell.getShell().get("x"));
    }

    @Test
    public void testMultiLineMult() throws Exception {
        shellIn.println(MercerShell.MULTILINE_START);
        shellIn.println("8");
        shellIn.println("*3");
        shellIn.println(MercerShell.MULTILINE_END);
        assertOutput("24");
    }

    @Test
    public void testLastResult() throws Exception {
        runSingleCommand("15");
        assertEquals(15, shell.getShell().get(MercerShell.LAST_RESULT_VAR));
        runSingleCommand("\"abc\" + 123");
        assertEquals("abc123", shell.getShell().get(MercerShell.LAST_RESULT_VAR));
    }
}
