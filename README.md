# Sample Notificator Service
This CLI is intended to search the database for projects (samples of a project)
that have received a new status. This is only done for the current day the tool is executed.

People who subscribed to be notified about their projects changes
will be notified via email. This service creates the messages and sends them.

## Usage

Build the tool with
```
mvn clean package
```

You can find the executable jar-with-dependencies in the target folder.

Execute the jar

```
java -jar <path-to-jar>/target/sample-notificator-app-<version>-jar-with-dependencies.jar -c <path-to-config>
```

#### Provide a config file
To run the tool you need to provide the credentials to access and read
data from the database. Therefore, you need to set up a credential file
which should contain the following content:

- **Host**: provide the host name to access the DB
- **User credentials**: provide user and password 
- **DB**: Specify which database you want to access 
- **Port**: Define the port through which you access the DB

```
mysql.host = 123.456.789
mysql.pass = myPassWord
mysql.user = myUserName
mysql.db = myDatabase
mysql.port = 8888
```