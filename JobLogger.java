import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class JobLogger {

    private static JobLogger instance = new JobLogger();

    private JobLogger() {
        initialized = false;
        logger = Logger.getLogger("MyLog");
    }

    public static JobLogger getInstance() {
        return instance;
    }

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
            String path = instance.fileParams.get("logFileFolder") + "/logFile.txt";
            File logFile = new File(path);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileHandler fh = new FileHandler(path);
            instance.logger.addHandler(fh);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean configureConsoleLogging() {
        try {
            ConsoleHandler ch = new ConsoleHandler();
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
            connectionProps.put("user", instance.dbParams.get("userName"));
            connectionProps.put("password", instance.dbParams.get("password"));

            connection = DriverManager.getConnection("jdbc:" + instance.dbParams.get("dbms") + "://"
                    + instance.dbParams.get("serverName") + ":" + instance.dbParams.get("portNumber") + "/",
                    connectionProps);

            instance.stmt = connection.prepareStatement("insert into Log values (?, ?)");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void logMessage(String message, Level level) {
        if (!initialized || message == null || level == null) {
            return;
        }

        String msg = message.trim();
        if (msg.length() == 0) {
            return;
        }

        String l = null;

        switch (level) {
        case MESSAGE:
            if (!logMessage) {
                return;
            }
            l = String.format("%s %s %s", "message", DateFormat.getDateInstance(DateFormat.LONG).format(new Date()),
                    msg);
            break;
        case WARNING:
            if (!logWarning) {
                return;
            }
            l = String.format("%s %s %s", "warning", DateFormat.getDateInstance(DateFormat.LONG).format(new Date()),
                    msg);
            break;
        case ERROR:
            if (!logError) {
                return;
            }
            l = String.format("%s %s %s", "error", DateFormat.getDateInstance(DateFormat.LONG).format(new Date()), msg);
            break;
        }

        if (logToFile || logToConsole) {
            logger.log(java.util.logging.Level.INFO, l);
        }

        if (logToDatabase) {
            try {
                stmt.setString(1, l);
                stmt.setString(2, String.valueOf(level.t));
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static enum Level {
        MESSAGE(1), WARNING(3), ERROR(2);

        public final int t;

        private Level(int t) {
            this.t = t;
        }
    }
}
