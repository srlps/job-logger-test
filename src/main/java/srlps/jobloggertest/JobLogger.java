package srlps.jobloggertest;

import java.io.File;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JobLogger {

    private static JobLogger instance = new JobLogger();

    private JobLogger() {
        initialized = false;
        logger = Logger.getLogger("MyLog");
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        logFormatter = new Formatter() {

            @Override
            public String format(LogRecord record) {
                return record.getMessage() + "\n";
            }
        };
    }

    public static JobLogger getInstance() {
        return instance;
    }

    private SimpleDateFormat format;
    private Formatter logFormatter;

    private boolean initialized;

    private boolean logToFile;
    private boolean logToConsole;
    private boolean logToDatabase;

    private boolean logMessage;
    private boolean logWarning;
    private boolean logError;

    private Map<String, String> dbParams;
    private Map<String, String> fileParams;

    private Logger logger;
    private PreparedStatement stmt;

    public static void init(boolean logToFile, boolean logToConsole, boolean logToDatabase, boolean logMessage,
            boolean logWarning, boolean logError, Map<String, String> dbParams, Map<String, String> fileParams) {
        if (!logMessage && !logWarning && !logError) {
            instance.initialized = false;
            return;
        }

        instance.logMessage = logMessage;
        instance.logWarning = logWarning;
        instance.logError = logError;

        instance.dbParams = dbParams;
        instance.fileParams = fileParams;

        instance.logToFile = logToFile && configureFileLogging();
        instance.logToConsole = logToConsole && configureConsoleLogging();
        instance.logToDatabase = logToDatabase && configureDatabaseLogging();

        instance.initialized = instance.logToFile || instance.logToConsole || instance.logToDatabase;
    }

    private static boolean configureFileLogging() {
        try {
            String path = instance.fileParams.get("filePath");
            File logFile = new File(path);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileHandler fh = new FileHandler(path);
            fh.setFormatter(instance.logFormatter);
            instance.logger.addHandler(fh);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean configureConsoleLogging() {
        try {
            ConsoleHandler ch = new ConsoleHandler() {
                @Override
                protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
                    super.setOutputStream(System.out);
                }
            };
            ch.setFormatter(instance.logFormatter);
            instance.logger.addHandler(ch);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean configureDatabaseLogging() {
        try {
            Connection connection = null;
            Properties connectionProps = new Properties();
            connectionProps.put("user", instance.dbParams.get("username"));
            connectionProps.put("password", instance.dbParams.get("password"));

            connection = DriverManager.getConnection(instance.dbParams.get("jdbcUrl"), connectionProps);

            instance.stmt = connection.prepareStatement("INSERT INTO Log (message, t) VALUES (?, ?)");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void logMessage(String message, Level level) {
        // check initialized and null params
        if (!initialized || message == null || level == null) {
            return;
        }
        // check empty message
        String msg = message.trim();
        if (msg.length() == 0) {
            return;
        }
        // check level enabled
        switch (level) {
        case MESSAGE:
            if (!logMessage) {
                return;
            }
            break;
        case WARNING:
            if (!logWarning) {
                return;
            }
            break;
        case ERROR:
            if (!logError) {
                return;
            }
            break;
        }
        // build message
        String l = String.format("%s %s %s", level.prefix, format.format(new Date()), msg);
        // log message
        if (logToFile || logToConsole) {
            logger.log(java.util.logging.Level.INFO, l);
        }
        if (logToDatabase) {
            try {
                stmt.setString(1, l);
                stmt.setInt(2, level.t);
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static enum Level {
        MESSAGE(1, "message"), WARNING(3, "warning"), ERROR(2, "error");

        public final int t;
        public final String prefix;

        private Level(int t, String prefix) {
            this.t = t;
            this.prefix = prefix;
        }
    }
}
