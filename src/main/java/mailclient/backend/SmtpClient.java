package mailclient.backend;

public interface SmtpClient {
    boolean sendMail(String sender, String[] recipients, String subject, String mailBody);

    void endConnection();

    boolean connectionIsReadyToUse();

    void reconnect(int retryCount);
}
