package life.qbic.business.subscription

import life.qbic.business.notification.create.CreateNotification
import life.qbic.business.notification.create.NotificationContent
import life.qbic.business.notification.create.CreateNotificationOutput
import life.qbic.business.notification.create.FetchUpdatedSamplesDataSource
import life.qbic.datamodel.samples.Status
import spock.lang.Specification
import java.time.LocalDate

/**
 * This test class tests for the {@link life.qbic.business.notification.create.CreateNotification} use case functionality
 *
 * @since: 1.0.0
 */
class CreateNotificationSpec extends Specification{

    def "If an exception is thrown in the notification creation process because no subscribers are found, a fail notification is returned"(){
        given: "The CreateNotification use case"
        FetchUpdatedSamplesDataSource ds = Stub(FetchUpdatedSamplesDataSource.class)
        CreateNotificationOutput output = Mock()
        CreateNotification createNotification = new CreateNotification(ds, output)

        and: "a dummy Subscriber list and a map containing Samples with their updated Sample status"
        Map<String, Status> updatedSamples = ["QMCDP007A3":Status.DATA_AVAILABLE,
                                              "QMCDP007A2":Status.SAMPLE_QC_FAIL,
                                              "QMCDP007A1":Status.DATA_AVAILABLE,
                                              "QMAAP007A3":Status.SAMPLE_QC_FAIL,
                                              "QMAAP018A2":Status.SAMPLE_RECEIVED,
                                              "QMAAP04525":Status.SAMPLE_QC_FAIL]

        Map<String,String> projectsWithTitles = ["QMCDP": "first project",
                                                 "QMAAP": ""]

        and: "Datasource that returns various information needed, but throws an exception when getting subscribers"
        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
        ds.getSubscriberForProject(_ as String) >> {throw new Exception("An error when fetching subscribers")}
        ds.fetchProjectsWithTitles() >> projectsWithTitles

        when: "The CreateNotification use case is triggered"
        createNotification.createNotifications("2020-08-17")

        then: "A fail notification"
        0* output.createdNotifications(_ as List<NotificationContent>)
        1* output.failNotification(_ as String)
    }

    def "If no subscribers are found no notifications are returned"(){
        given: "The CreateNotification use case"
        FetchUpdatedSamplesDataSource ds = Stub(FetchUpdatedSamplesDataSource.class)
        CreateNotificationOutput output = Mock()
        CreateNotification createNotification = new CreateNotification(ds, output)

        and: "a dummy Subscriber list and a map containing Samples with their updated Sample status"
        Map<String, Status> updatedSamples = ["QMCDP007A3":Status.DATA_AVAILABLE,
                                              "QMCDP007A2":Status.SAMPLE_QC_FAIL,
                                              "QMCDP007A1":Status.DATA_AVAILABLE,
                                              "QMAAP007A3":Status.SAMPLE_QC_FAIL,
                                              "QMAAP018A2":Status.SAMPLE_RECEIVED,
                                              "QMAAP04525":Status.SAMPLE_QC_FAIL]

        Map<String,String> projectsWithTitles = ["QMCDP": "first project",
                                                 "QMAAP": ""]

        and: "Datasource that returns various information needed, but throws an exception when getting subscribers"
        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
        ds.getSubscriberForProject(_ as String) >> {[]}
        ds.fetchProjectsWithTitles() >> projectsWithTitles

        when: "The CreateNotification use case is triggered"
        createNotification.createNotifications("2020-08-17")

        then: "A fail notification"
        1* output.createdNotifications(_ as List<NotificationContent>)
        0* output.failNotification(_ as String)
    }

    def "Providing a valid Date will return notifications stored for that date"(){
        given: "The CreateNotification use case"
        FetchUpdatedSamplesDataSource ds = Stub(FetchUpdatedSamplesDataSource.class)
        CreateNotificationOutput output = Mock()
        CreateNotification createNotification = new CreateNotification(ds, output)

        and: "a dummy Subscriber list and a map containing Samples with their updated Sample status"
        Map<String, Status> updatedSamples = ["QMCDP007A3":Status.DATA_AVAILABLE,
                                              "QMCDP007A2":Status.SAMPLE_QC_FAIL,
                                              "QMCDP007A1":Status.DATA_AVAILABLE,
                                              "QMAAP007A3":Status.SAMPLE_QC_FAIL,
                                              "QMAAP018A2":Status.SAMPLE_RECEIVED,
                                              "QMAAP04525":Status.SAMPLE_QC_FAIL]

        Subscriber subscriber1 = new Subscriber("Awesome", "Customer", "awesome.customer@provider.com")
        Subscriber subscriber2 = new Subscriber("Good", "Customer", "good.customer@provider.de")
        List<Subscriber> subscribers = [subscriber1, subscriber2]
        Map<String,String> projectsWithTitles = ["QMCDP": "first project",
                                                 "QMAAP": ""]

        and: "Datasource that returns various information needed"
        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
        ds.getSubscriberForProject(_ as String) >> subscribers
        ds.fetchProjectsWithTitles() >> projectsWithTitles

        when: "The CreateNotification use case is triggered"
        createNotification.createNotifications("2020-08-17")

        then: "A map associating the notifications with the subscriber for the provided date is returned"
        1* output.createdNotifications(_ as List<NotificationContent>)
    }

    def "2 projects result in 2 email for the same subscriber"(){
        given: "The CreateNotification use case"
        FetchUpdatedSamplesDataSource ds = Stub(FetchUpdatedSamplesDataSource.class)
        CreateNotificationOutput output = Mock()
        CreateNotification createNotification = new CreateNotification(ds, output)

        and: "a dummy Subscriber list and a map containing Samples with their updated Sample status"
        Map<String, Status> updatedSamples = ["QMCDP007A3":Status.DATA_AVAILABLE,
                                              "QMCDP007A2":Status.SAMPLE_QC_FAIL,
                                              "QMCDP007A1":Status.DATA_AVAILABLE,
                                              "QMAAP007A3":Status.SAMPLE_QC_FAIL,
                                              "QMAAP018A2":Status.SAMPLE_RECEIVED,
                                              "QMAAP04525":Status.SAMPLE_QC_FAIL]

        Subscriber subscriber1 = new Subscriber("Awesome", "Customer", "awesome.customer@provider.com")
        List<Subscriber> subscribers = [subscriber1]
        Map<String,String> projectsWithTitles = ["QMCDP": "first project",
                                                 "QMAAP": ""]

        and: "Datasource that returns various information needed"
        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
        ds.getSubscriberForProject(_ as String) >> subscribers
        ds.fetchProjectsWithTitles() >> projectsWithTitles

        when: "The CreateNotification use case is triggered"
        createNotification.createNotifications("2020-08-17")

        then: "A map associating the notifications with the subscriber for the provided date is returned"
        1* output.createdNotifications(_ as List<NotificationContent>) >> {arguments ->
            List<NotificationContent> notifications = arguments.get(0)
            assert notifications.size() == 2
        }
    }

    def "The message contains the correct number of failing samples and available datasets"(){
        given: "The CreateNotification use case"
        FetchUpdatedSamplesDataSource ds = Stub(FetchUpdatedSamplesDataSource.class)
        CreateNotificationOutput output = Mock()
        CreateNotification createNotification = new CreateNotification(ds, output)

        and: "a dummy Subscriber list and a map containing Samples with their updated Sample status"
        Map<String, Status> updatedSamples = ["QMCDP007A3":Status.DATA_AVAILABLE,
                                              "QMCDP007A2":Status.SAMPLE_QC_FAIL,
                                              "QMCDP007A1":Status.DATA_AVAILABLE,
                                              "QMAAP007A3":Status.SAMPLE_QC_FAIL,
                                              "QMAAP018A2":Status.SAMPLE_RECEIVED,
                                              "QMAAP04525":Status.SAMPLE_QC_FAIL]

        Subscriber subscriber1 = new Subscriber("Awesome", "Customer", "awesome.customer@provider.com")
        List<Subscriber> subscribers = [subscriber1]
        Map<String,String> projectsWithTitles = ["QMCDP": "first project",
                                                 "QMAAP": ""]

        and: "Datasource that returns various information needed"
        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
        ds.getSubscriberForProject(_ as String) >> subscribers
        ds.fetchProjectsWithTitles() >> projectsWithTitles

        when: "The CreateNotification use case is triggered"
        createNotification.createNotifications("2020-08-17")

        then: "A map associating the notifications with the subscriber for the provided date is returned"
        1* output.createdNotifications(_ as List<NotificationContent>) >> {arguments ->
            List<NotificationContent> notifications = arguments.get(0)
            assert notifications.get(0).availableDataCount == 2
            assert notifications.get(0).failedQCCount == 1
            assert notifications.get(1).failedQCCount == 2
            assert notifications.get(1).availableDataCount == 0
        }
    }

    def "The message is created multiple times if there is more then one subscriber"(){
        given: "The CreateNotification use case"
        FetchUpdatedSamplesDataSource ds = Stub(FetchUpdatedSamplesDataSource.class)
        CreateNotificationOutput output = Mock()
        CreateNotification createNotification = new CreateNotification(ds, output)

        and: "a dummy Subscriber list and a map containing Samples with their updated Sample status"
        Map<String, Status> updatedSamples = ["QMCDP007A3":Status.DATA_AVAILABLE,
                                              "QMCDP007A2":Status.SAMPLE_QC_FAIL,
                                              "QMCDP007A1":Status.DATA_AVAILABLE]

        Subscriber subscriber1 = new Subscriber("Awesome", "Customer", "awesome.customer@provider.com")
        Subscriber subscriber2 = new Subscriber("Good", "Customer", "good.customer@provider.de")
        List<Subscriber> subscribers = [subscriber1, subscriber2]
        Map<String,String> projectsWithTitles = ["QMCDP": "first project"]

        and: "Datasource that returns various information needed"
        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
        ds.getSubscriberForProject(_ as String) >> subscribers
        ds.fetchProjectsWithTitles() >> projectsWithTitles

        when: "The CreateNotification use case is triggered"
        createNotification.createNotifications("2020-08-17")

        then: "A map associating the notifications with the subscriber for the provided date is returned"
        1* output.createdNotifications(_ as List<NotificationContent>) >> {arguments ->
            List<NotificationContent> notifications = arguments.get(0)
            assert notifications.size() == 2
            assert notifications.get(0).getCustomerEmailAddress() == subscriber1.getEmail()
            assert notifications.get(1).getCustomerEmailAddress() == subscriber2.getEmail()
        }
    }
}
