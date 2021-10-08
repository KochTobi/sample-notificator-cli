package life.qbic.samplenotificator.components

import life.qbic.business.notification.create.CreateNotificationOutput
import life.qbic.business.notification.create.NotificationContent

/**
 * Presenter for the CreateNotification Use Case
 *
 *  This presenter handles the output of the CreateNotification use case and prepares it for the sending process
 *
 * @since: 1.0.0
 *
 */
class CreateNotificationConnector implements CreateNotificationOutput {

    private EmailGenerator emailGenerator
    //ToDo this should be centralized
    String logPath = "./notification-cli.log"

    CreateNotificationConnector(EmailGenerator emailGenerator){
        this.emailGenerator = emailGenerator
    }

    /**
     * Returns a NoticiationContent DTO per project containing the information for updated projects for the provided Date
     *
     * @param notifications List of DTOs containing the update information for one project each to send out
     * @since 1.0.0
     */
    @Override
    void createdNotifications(List<NotificationContent> notifications) {
        sendEmailNotifications(notifications)
    }

    /**
     * Sends failure notifications that have been
     * recorded during the use case.
     * @param notification containing a failure message
     * @since 1.0.0
     */
    @Override
    void failNotification(String notification) {
        File logFile = new File(logPath)
        logFile.append("\n")
        logFile.append(notification)
        emailGenerator.notifyAdminUponFailure(true)
    }

    void sendEmailNotifications(List notifications){
        emailGenerator.sendEmails(notifications)
    }
}
