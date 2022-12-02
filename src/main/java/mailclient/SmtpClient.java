package mailclient;

public interface SmtpClient {
    void sendMail(String sender, String[] recipients, String subject, String mailBody);

    void endConnection();

    boolean connectionIsReadyToUse();

    void reconnect(int retryCount);
}
