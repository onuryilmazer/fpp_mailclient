package mailclient;

public interface Pop3Client {
    public void listMails();

    public void showMail(int mailNumber);

    public void getNumberOfMails();

    public void closeConnection();
}
