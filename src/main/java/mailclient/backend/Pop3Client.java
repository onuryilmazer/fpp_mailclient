package mailclient.backend;

public interface Pop3Client {
    int getNumberOfMails();

    Mail[] fetchMailUIDLs();

    Mail fetchMailEnvelope(Mail mail);

    Mail fetchMailBody(Mail partialMail);

    void closeConnection();

    boolean isLoggedIn();

    boolean connectionIsReadyToUse();

    void reconnect(int retryCount);
}
