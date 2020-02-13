Analisis:
---
* Attribute `initialized` is not used
* Attribute `dbParams` is a raw `Map`
* Static attributes and method are configured through a non static constructor
* Static method `LogMessage` doesn't follow method naming conventions
* Static method `LogMessage` can log the same message in 3 levels at the same time
* Static method `LogMessage` shouldn't throw Exceptions
* Inside `LogMessage`:
    * `messageText.trim();` result is lost
    * Database, file and console configuration shouldn't be inside the `LogMessage` method
    * `dbParams` shouldn't contain configuration parameters for file handling
    * String variable `l` is never initialized and subsequent calls will throw `NullPointerException`
    * `logger.addHandler` is invoked everytime a message is logged
    * `stmt.executeUpdate` is vulnerable to sql injection
    * `"insert into Log_Values('" + message + "', " + String.valueOf(t) + ")"` is not a valid sql query

Proposal:
---
* Use the singleton pattern to initialize and configure a single `JobLogger` instance
* For configuration, use an `init` static method
* Use the attribute `initialized` to decide whether to log a message or not by configuring its value after the `init` call
* Configure every handler individually inside the `init` method
* Use `Map<String, String>` for the `dbParams`  attribute and create a `fileParams` attribute to hold the configuration parameters for file handling
* Create an internal `enum Level` to hold the 3 level constants
* Change `LogMessage` method to `logMessage` with 2 parameters: `String message` and `Level level`
* Use `PreparedStatement` instead of `Statement` to prevent sql injection
* Use `"inser into Log values (?, ?)"` as the query template