package srlps.jobloggertest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

import srlps.jobloggertest.JobLogger.Level;

public class JobLoggerTestConsoleBased {

    // main functionality

    @Test
    public void testlogToConsole() throws IOException, ParseException {
        OutputStream os = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int x) throws IOException {
                this.string.append((char) x);
            }

            public String toString() {
                return this.string.toString();
            }
        };

        PrintStream ps = new PrintStream(os, true);
        System.setOut(ps);

        JobLogger.init(false, true, false, true, true, true, null, null);
        JobLogger log = JobLogger.getInstance();

        log.logMessage("testlogToConsole", Level.MESSAGE);

        os.close();
        String[] loggedTokens = os.toString().split("\n")[0].split(" ");

        assertTrue(loggedTokens.length == 4);
        assertEquals(loggedTokens[0], Level.MESSAGE.prefix);
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(String.format("%s %s", loggedTokens[1], loggedTokens[2]));
        assertEquals(loggedTokens[3], "testlogToConsole");
    }

    // refactors

    @Test
    public void testSingletonInstance() {
        JobLogger instance1 = JobLogger.getInstance();
        JobLogger instance2 = JobLogger.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testNoExceptionWhileNotInitialized() {
        JobLogger log = JobLogger.getInstance();
        log.logMessage("testNoExceptionWhileNotInitialized", Level.MESSAGE);
    }

    @Test
    public void testNoExceptionWhenInitializedAllFalse() {
        JobLogger.init(false, false, false, false, false, false, null, null);
        JobLogger log = JobLogger.getInstance();
        log.logMessage("testNoExceptionWhenInitializedAllFalse", Level.MESSAGE);
    }

    @Test
    public void testNoExceptionWhenInitializedWrong() {
        JobLogger.init(true, true, true, true, true, true, null, null);
        JobLogger log = JobLogger.getInstance();
        log.logMessage("testNoExceptionWhenInitializedWrong", Level.MESSAGE);
    }

}
