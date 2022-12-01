package mailclient;

public interface Pop3Client {
    void listMails();

    void showMail(int mailNumber);

    void getNumberOfMails();

    void closeConnection();

    boolean isLoggedIn();

    boolean connectionIsReadyToUse();
}
