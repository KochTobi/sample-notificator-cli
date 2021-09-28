package life.qbic.business.subscription

import life.qbic.business.notification.create.CreateNotification
import life.qbic.business.notification.create.NotificationContent
import life.qbic.business.notification.create.CreateNotificationOutput
import life.qbic.business.notification.create.FetchUpdatedDataSource
import life.qbic.datamodel.samples.Status
import spock.lang.Specification
import java.time.LocalDate

/**
 * This test class tests for the {@link life.qbic.business.notification.create.CreateNotification} use case functionality
 *
 * @since: 1.0.0
 */
class CreateNotificationSpec extends Specification{

    def "If an exception is thrown in the notification fetching process, a fail notification is returned"(){
        given: "The CreateNotification use case"
        FetchUpdatedDataSource ds = Stub(FetchUpdatedDataSource.class)
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
        Map<String,List<String>> projectsWithSamples = ["QMCDP":["QMCDP007A3", "QMCDP007A2", "QMCDP007A1"],
                                                        "QMAAP":["QMAAP007A3", "QMAAP018A2", "QMAAP04525"]]
        Map<String,String> projectsWithTitles = ["QMCDP": "first project",
                                                 "QMAAP": ""]

        and: "Datasource that returns various information needed, but throws an exception when getting subscribers"
        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
        ds.getSubscriberForProject(_ as String) >> {throw new Exception("An error when fetching subscribers")}
        ds.getProjectsWithSamples(_ as List<String>) >> projectsWithSamples
        ds.fetchProjectsWithTitles() >> projectsWithTitles


        when: "The CreateNotification use case is triggered"
        createNotification.createNotifications("2020-08-17")

        then: "A fail notification"
        0* output.createdNotifications(_ as List<NotificationContent>)
        1* output.failNotification(_ as String)
    }
    
//    def "Providing a valid Date will return notifications stored for that date"(){
//        given: "The CreateNotification use case"
//        FetchUpdatedDataSource ds = Stub(FetchUpdatedDataSource.class)
//        CreateNotificationOutput output = Mock()
//        CreateNotification createNotification = new CreateNotification(ds, output)
//
//        and: "a dummy Subscriber list and a map containing Samples with their updated Sample status"
//        Map<String, Status> updatedSamples = ["QMCDP007A3":Status.DATA_AVAILABLE,
//                                              "QMCDP007A2":Status.SAMPLE_QC_FAIL,
//                                              "QMCDP007A1":Status.DATA_AVAILABLE,
//                                              "QMAAP007A3":Status.SAMPLE_QC_FAIL,
//                                              "QMAAP018A2":Status.SAMPLE_RECEIVED,
//                                              "QMAAP04525":Status.SAMPLE_QC_FAIL]
//
//        Subscriber subscriber1 = new Subscriber("Awesome", "Customer", "awesome.customer@provider.com")
//        Subscriber subscriber2 = new Subscriber("Good", "Customer", "good.customer@provider.de")
//        List<Subscriber> subscribers = [subscriber1, subscriber2]
//
//        and: "Datasource that returns the Subscriber list and the updated Sample Status"
//        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
//        ds.getSubscriberForProject(_ as String) >> subscribers
//
//        when: "The CreateNotification use case is triggered"
//        createNotification.createNotifications("2020-08-17")
//
//        then: "A map associating the notifications with the subscriber for the provided date is returned"
//        1* output.createdNotifications(_ as Map<Subscriber, String>)
//    }
    
    def "Providing a valid Date will return notifications stored for that date"(){
        given: "The CreateNotification use case"
        FetchUpdatedDataSource ds = Stub(FetchUpdatedDataSource.class)
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
        Map<String,List<String>> projectsWithSamples = ["QMCDP":["QMCDP007A3", "QMCDP007A2", "QMCDP007A1"],
                                                        "QMAAP":["QMAAP007A3", "QMAAP018A2", "QMAAP04525"]]
        Map<String,String> projectsWithTitles = ["QMCDP": "first project",
                                                 "QMAAP": ""]

        and: "Datasource that returns various information needed"
        ds.getUpdatedSamplesForDay(_ as LocalDate) >> updatedSamples
        ds.getSubscriberForProject(_ as String) >> subscribers
        ds.getProjectsWithSamples(_ as List<String>) >> projectsWithSamples
        ds.fetchProjectsWithTitles() >> projectsWithTitles


        when: "The CreateNotification use case is triggered"
        createNotification.createNotifications("2020-08-17")

        then: "A map associating the notifications with the subscriber for the provided date is returned"
        1* output.createdNotifications(_ as List<NotificationContent>)
    }
}
