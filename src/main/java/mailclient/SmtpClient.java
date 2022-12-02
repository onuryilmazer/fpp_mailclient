package mailclient;

public interface SmtpClient {
    void sendMail(String sender, String[] recipients, String subject, String message);

    void endConnection();

    boolean connectionIsReadyToUse();

    void reconnect(int retryCount);
}
