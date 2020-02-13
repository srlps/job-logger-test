package srlps.jobloggertest;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static srlps.jobloggertest.JobLogger.Level;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JobLoggerTest {

    // main functionality

    @Test
    public void testlogToFile() {
        JobLogger.init(false, true, false, true, true, true, null, null);
        JobLogger log = JobLogger.getInstance();

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

        System.setErr(ps);

        log.logMessage("test", Level.MESSAGE);

        assertTrue(os.toString().equals("message "));
    }

    @Test
    public void testSingletonInstance() {
        JobLogger instance1 = JobLogger.getInstance();
        JobLogger instance2 = JobLogger.getInstance();
        assertTrue(instance1 == instance2);
    }
}
