package mailclient.backend;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public interface Pop3Client {
    int getNumberOfMails();

    TreeMap<Integer, String> fetchMailUIDLs();

    Mail fetchMailEnvelope(Mail mail);

    Mail fetchMailBody(Mail partialMail);

    void closeConnection();

    boolean isLoggedIn();

    boolean connectionIsReadyToUse();

    void markMailForDeletionFromServer(int mailID);

    void reconnect(int retryCount) throws IOException;
}
