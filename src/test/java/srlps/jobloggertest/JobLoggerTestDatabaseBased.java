package srlps.jobloggertest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import srlps.jobloggertest.JobLogger.Level;

public class JobLoggerTestDatabaseBased {

    static Map<String, String> dbParams;
    static Connection connection;

    @BeforeClass
    public static void initClass() throws SQLException {
        dbParams = new HashMap<>();
        dbParams.put("jdbcUrl", "jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1");
        dbParams.put("username", "user");
        dbParams.put("password", "pass");

        Properties connectionProps = new Properties();
        connectionProps.put("user", dbParams.get("username"));
        connectionProps.put("password", dbParams.get("password"));

        connection = DriverManager.getConnection(dbParams.get("jdbcUrl"), connectionProps);
        PreparedStatement prepStmnt = connection.prepareStatement(
                "CREATE TABLE Log (id bigint auto_increment, message varchar, t int, PRIMARY KEY (id))");
        prepStmnt.executeUpdate();
        prepStmnt.close();
    }

    // main functionality

    @Test
    public void testlogToDatabase() throws ParseException, SQLException, IOException {
        JobLogger.init(false, false, true, true, true, true, dbParams, null);
        JobLogger log = JobLogger.getInstance();

        log.logMessage("testlogToDatabase", Level.MESSAGE);

        Statement stmnt = connection.createStatement();
        ResultSet rs = stmnt.executeQuery("SELECT * FROM Log WHERE message like '%testlogToDatabase'");
        assertTrue(rs.first());

        StringBuilder textBuilder = new StringBuilder();
        Reader reader = new BufferedReader(
                new InputStreamReader(rs.getAsciiStream("message"), Charset.forName(StandardCharsets.UTF_8.name())));
        int c = 0;
        while ((c = reader.read()) != -1) {
            textBuilder.append((char) c);
        }
        String[] loggedTokens = textBuilder.toString().split(" ");

        assertTrue(loggedTokens.length == 4);
        assertEquals(loggedTokens[0], Level.MESSAGE.prefix);
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(String.format("%s %s", loggedTokens[1], loggedTokens[2]));
        assertEquals(loggedTokens[3], "testlogToDatabase");
        assertTrue(rs.getInt("t") == Level.MESSAGE.t);
    }

    // refactors

    @Test
    public void testSqlInjectionPrevented() throws SQLException {
        JobLogger.init(false, false, true, true, true, true, dbParams, null);
        JobLogger log = JobLogger.getInstance();

        log.logMessage("testSqlInjectionPrevented',500)--", Level.MESSAGE);

        Statement stmnt = connection.createStatement();
        ResultSet rs = stmnt.executeQuery("SELECT * FROM Log WHERE t = 500");
        assertFalse(rs.first());
    }
}
