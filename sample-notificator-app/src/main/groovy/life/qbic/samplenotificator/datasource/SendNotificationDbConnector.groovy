package life.qbic.samplenotificator.datasource

import life.qbic.business.notification.send.SendNotificationDataSource
import life.qbic.business.subscription.Subscriber
import life.qbic.datamodel.samples.Status
import life.qbic.samplenotificator.datasource.database.ConnectionProvider
import org.apache.commons.collections.map.HashedMap

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.Instant

/**
 * <h1>A database connector to retrieve information for sending notifications</h1>
 *
 * <p>This connector reads the notification queue table and finds the associated people that subscribed for a notification</p>
 *
 * @since 1.0.0
 *
*/
class SendNotificationDbConnector implements SendNotificationDataSource{
    private ConnectionProvider connectionProvider

    SendNotificationDbConnector(ConnectionProvider connectionProvider){
        this.connectionProvider = connectionProvider
    }


    @Override
    List<Subscriber> getSubscribersForTodaysNotifications(Instant today) {
        List<Subscriber> subscriberList = []
        //1. get todays notifications
        Map sampleToStatus = getTodaysNotifications(today)
        // retrieve the project code
        Map subscriberIdsToSamples = getSubscriberIdForSamples(sampleToStatus)
        //2. get the subscribers for the subscriptions
        subscriberIdsToSamples.each { Map.Entry<Integer,List<String>> subscriberMap ->
            Map allSamplesToStatus = sampleToStatus.findAll {it.key in subscriberMap.value}
            subscriberList << getSubscriber(subscriberMap.key,allSamplesToStatus)
        }

        return subscriberList
    }

    private Map<String, Status> getTodaysNotifications(Instant today){
        //todo is this efficient?
        String sqlQuery = SELECT_NOTIFICATIONS + " WHERE year(arrival_time) = year(?) AND month(arrival_time) = month(?) AND day(arrival_time) = day(?)"
        Map<String, Status> foundNotifications = new HashMap<>()

        Connection connection = connectionProvider.connect()

        connection.withCloseable {
            PreparedStatement preparedStatement = it.prepareStatement(sqlQuery)
            Timestamp todaysTimeStamp = Timestamp.from(today)
            preparedStatement.setTimestamp(1, todaysTimeStamp)
            preparedStatement.setTimestamp(2, todaysTimeStamp)
            preparedStatement.setTimestamp(3, todaysTimeStamp)
            preparedStatement.execute()

            def resultSet = preparedStatement.getResultSet()
            while (resultSet.next()) {
                String sampleCode = resultSet.getString("sample_code")
                Status status = Status.valueOf(resultSet.getString("sample_status"))
                foundNotifications.put(sampleCode,status)
            }
        }
        return foundNotifications
    }

    private Map<Integer,List<String>> getSubscriberIdForSamples(Map<String,Status> sampleToStatus){

        Connection connection = connectionProvider.connect()
        Map<Integer,List<String>> userToString = new HashedMap()

        connection.withCloseable { Connection con ->
            sampleToStatus.each {
                //get all subscriptions for a person, id to list of project codes
                String sqlQuery = SELECT_SUBSCRIPTIONS + " WHERE ? LIKE CONCAT(project_code ,'%') "

                PreparedStatement preparedStatement = con.prepareStatement(sqlQuery)
                preparedStatement.setString(1,it.key)
                preparedStatement.execute()
                def resultSet = preparedStatement.getResultSet()

                while(resultSet.next()){
                    int user = resultSet.getInt("subscriber_id")

                    if(userToString.containsKey(user)){
                        userToString.get(user) << it.key
                    }
                    else{
                        userToString.put(user,[it.key])
                    }
                }
            }
        }

        return userToString
    }

    private Subscriber getSubscriber(Integer subscriberId, Map<String,Status> sampleToStatus){
        Connection connection = connectionProvider.connect()
        Subscriber subscriber = null

        connection.withCloseable {
            String sqlStatement = SELECT_SUBSCRIBERS + " WHERE id = ?"

            PreparedStatement preparedStatement = it.prepareStatement(sqlStatement)
            preparedStatement.setInt(1,subscriberId)
            preparedStatement.execute()
            def resultSet = preparedStatement.getResultSet()

            while(resultSet.next()) {
                String firstName = resultSet.getString("first_name")
                String lastName = resultSet.getString("last_name")
                String email = resultSet.getString("email")
                subscriber = new Subscriber(firstName,lastName,email,sampleToStatus)
            }
        }
        return subscriber
    }


    private String SELECT_SUBSCRIBERS = "SELECT first_name, last_name, email FROM subscriber"
    private String SELECT_SUBSCRIPTIONS = "SELECT project_code, subscriber_id FROM subscription"
    private String SELECT_NOTIFICATIONS = "SELECT sample_code, sample_status FROM notification"

}