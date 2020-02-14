package srlps.jobloggertest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import srlps.jobloggertest.JobLogger.Level;

public class JobLoggerTestFileBased {

    File file;
    Map<String, String> fileParams;

    @Before
    public void init() throws IOException {
        file = File.createTempFile("test", ".txt");
        fileParams = new HashMap<>();
        fileParams.put("filePath", file.getAbsolutePath());
    }

    @After
    public void end() {
        file.delete();
    }

    // main functionality

    @Test
    public void testlogToFile() throws IOException, ParseException {
        JobLogger.init(true, false, false, true, true, true, null, fileParams);
        JobLogger log = JobLogger.getInstance();

        log.logMessage("testlogToFile", Level.MESSAGE);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String[] loggedTokens = br.readLine().split(" ");
        fr.close();

        assertTrue(loggedTokens.length == 4);
        assertEquals(loggedTokens[0], Level.MESSAGE.prefix);
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(String.format("%s %s", loggedTokens[1], loggedTokens[2]));
        assertEquals(loggedTokens[3], "testlogToFile");
    }

    // refactors

    @Test
    public void testlogLevelMessage() throws IOException, ParseException {
        JobLogger.init(true, false, false, true, false, false, null, fileParams);
        JobLogger log = JobLogger.getInstance();

        log.logMessage("testlogLevelMessage", Level.MESSAGE);
        log.logMessage("testlogLevelWarning", Level.WARNING);
        log.logMessage("testlogLevelError", Level.ERROR);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        List<String[]> loggedTokensList = br.lines().map(line -> line.split(" ")).collect(Collectors.toList());
        fr.close();

        assertTrue(loggedTokensList.size() == 1);
        String[] loggedTokens = loggedTokensList.get(0);
        assertEquals(loggedTokens[0], Level.MESSAGE.prefix);
        assertEquals(loggedTokens[3], "testlogLevelMessage");
    }

    @Test
    public void testlogLevelWarning() throws IOException, ParseException {
        JobLogger.init(true, false, false, false, true, false, null, fileParams);
        JobLogger log = JobLogger.getInstance();

        log.logMessage("testlogLevelMessage", Level.MESSAGE);
        log.logMessage("testlogLevelWarning", Level.WARNING);
        log.logMessage("testlogLevelError", Level.ERROR);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        List<String[]> loggedTokensList = br.lines().map(line -> line.split(" ")).collect(Collectors.toList());
        fr.close();

        assertTrue(loggedTokensList.size() == 1);
        String[] loggedTokens = loggedTokensList.get(0);
        assertEquals(loggedTokens[0], Level.WARNING.prefix);
        assertEquals(loggedTokens[3], "testlogLevelWarning");
    }

    @Test
    public void testlogLevelError() throws IOException, ParseException {
        JobLogger.init(true, false, false, false, false, true, null, fileParams);
        JobLogger log = JobLogger.getInstance();

        log.logMessage("testlogLevelMessage", Level.MESSAGE);
        log.logMessage("testlogLevelWarning", Level.WARNING);
        log.logMessage("testlogLevelError", Level.ERROR);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        List<String[]> loggedTokensList = br.lines().map(line -> line.split(" ")).collect(Collectors.toList());
        fr.close();

        assertTrue(loggedTokensList.size() == 1);
        String[] loggedTokens = loggedTokensList.get(0);
        assertEquals(loggedTokens[0], Level.ERROR.prefix);
        assertEquals(loggedTokens[3], "testlogLevelError");
    }

    @Test
    public void testlogAllLevels() throws IOException, ParseException {
        JobLogger.init(true, false, false, true, true, true, null, fileParams);
        JobLogger log = JobLogger.getInstance();

        log.logMessage("testlogLevelMessage", Level.MESSAGE);
        log.logMessage("testlogLevelWarning", Level.WARNING);
        log.logMessage("testlogLevelError", Level.ERROR);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        List<String[]> loggedTokensList = br.lines().map(line -> line.split(" ")).collect(Collectors.toList());
        fr.close();

        assertTrue(loggedTokensList.size() == 3);
        assertEquals(loggedTokensList.get(0)[0], Level.MESSAGE.prefix);
        assertEquals(loggedTokensList.get(0)[3], "testlogLevelMessage");
        assertEquals(loggedTokensList.get(1)[0], Level.WARNING.prefix);
        assertEquals(loggedTokensList.get(1)[3], "testlogLevelWarning");
        assertEquals(loggedTokensList.get(2)[0], Level.ERROR.prefix);
        assertEquals(loggedTokensList.get(2)[3], "testlogLevelError");
    }
}
