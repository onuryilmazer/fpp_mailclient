/*
    Sources:
        Marc Loy, Patrick Niemeyer, Daniel Leuck - Learning Java - An Introduction to Real-World Programming with Java-O'Reilly: Chapter 11 Sockets & Streams
        A Guide to Java Sockets: https://www.baeldung.com/a-guide-to-java-sockets
        Java IO - Streams: https://jenkov.com/tutorials/java-io/streams.html
 */

package mailclient.backend;

import com.sun.source.tree.Tree;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.TreeMap;

public class Pop3WebSocketsImplementation implements Pop3Client {
    private String serverAddress;
    private int serverPort;
    private String username;
    private String password;
    private Socket connection;
    private boolean encryptedConnection;
    private boolean loggedIn = false;
    private static final String CRLF = "\r\n";  //Line termination character: carriage return line feed pair. Source: POP3 Specification.


    public Pop3WebSocketsImplementation(MailServer myServer, String username, String password) throws IOException {
        this.serverAddress = myServer.getPop3Address();
        this.serverPort = myServer.getPop3Port();
        this.encryptedConnection = myServer.isPop3Encrypted();
        this.username = username;
        this.password = password;
        establishConnection();
        fetchMailUIDLs();
    }

    private void establishConnection() throws IOException, RuntimeException {
        if (connection == null || connection.isClosed()) {
            try {
                if (encryptedConnection) {
                    SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    connection = (SSLSocket) sslsocketfactory.createSocket(serverAddress, serverPort);
                }
                else {
                    connection = new Socket(serverAddress, serverPort);
                }

                if (readSocket(false).statusIndicator != ServerResponse.STATUS.OK) {
                    throw new RuntimeException("Error: Couldn't connect to the server");
                }

                writeToSocket("USER " + username);
                if (readSocket(false).statusIndicator != ServerResponse.STATUS.OK) {
                    throw new RuntimeException("Error: Username was not accepted by the server.");
                }

                writeToSocket("PASS " + password);
                if (readSocket(false).statusIndicator != ServerResponse.STATUS.OK) {
                    throw new RuntimeException("Error: Username and/or password was not accepted by the server.");
                }
                else {
                    loggedIn = true;
                }
            }
            catch (UnknownHostException e) {
                throw new UnknownHostException("Error: Hostname couldn't be resolved: " + e.getMessage());
            }
            catch (IOException e) {
                throw new IOException("Error: IOException: " + e.getMessage());
            }
        }
    }

    @Override
    public TreeMap<Integer, String> fetchMailUIDLs() {
        ServerResponse response = uidl();
        String[] lines = response.message.split(CRLF);
        int numberOfMails = lines.length -2; //first and last lines are not id's.

        if(numberOfMails <= 0) {
            return null;
        }

        TreeMap<Integer, String> mailUIDLs = new TreeMap<>();

        for (int i = 1; i < lines.length-1; i++) {
            String line = lines[i];
            int mailNumber = Integer.parseInt(line.substring(0, line.indexOf(" ")));
            String UIDL = line.substring(line.indexOf(" ")+1, line.length());
            mailUIDLs.put(mailNumber, UIDL);
        }

        return mailUIDLs;
    }

    @Override
    public Mail fetchMailEnvelope(Mail mail) throws IllegalArgumentException {
        //top returns mail metadata and the first line of the mail body.
        ServerResponse mailRequest = top(mail.mailNr, 1);

        if (mailRequest.statusIndicator == ServerResponse.STATUS.ERR) {
            throw new IllegalArgumentException("Error: Mail " + mail.mailNr + " was not found.\n" + mailRequest.message);
        }

        String base64DecodedMessage = mailRequest.decodeBase64Parts();

        mail.to="";
        int toIndex = base64DecodedMessage.indexOf("To: ");
        int toEndIndex = base64DecodedMessage.indexOf(CRLF, toIndex);
        if (toIndex != -1 && toEndIndex != -1 && toIndex < toEndIndex) {
            mail.to = base64DecodedMessage.substring(toIndex+4, toEndIndex);
        }

        mail.subject="";
        int subjectIndex = base64DecodedMessage.indexOf("Subject: ");
        int subjectEndIndex = base64DecodedMessage.indexOf(CRLF, subjectIndex);
        if (subjectIndex != -1 && subjectEndIndex != -1 && subjectIndex < subjectEndIndex) {
            mail.subject = base64DecodedMessage.substring(subjectIndex + 9, subjectEndIndex);
        }

        mail.date="";
        int dateIndex = base64DecodedMessage.indexOf("Date: ");
        int dateEndIndex = base64DecodedMessage.indexOf(CRLF, dateIndex);
        if (dateIndex != -1 && dateEndIndex != -1 && dateIndex < dateEndIndex) {
            mail.date = base64DecodedMessage.substring(dateIndex+6,dateEndIndex);
        }

        mail.from="";
        int fromIndex = base64DecodedMessage.indexOf("From: ");
        int fromEndIndex = base64DecodedMessage.indexOf(CRLF, fromIndex);
        if (fromIndex != -1 && fromEndIndex != -1 && fromIndex < fromEndIndex) {
            mail.from = base64DecodedMessage.substring(fromIndex + 6, fromEndIndex);
        }

        mail.replyTo="";
        int replyToIndex = base64DecodedMessage.indexOf("Reply-To: ");
        int replyToEndIndex = base64DecodedMessage.indexOf(CRLF, replyToIndex);
        if (replyToIndex != -1 && replyToEndIndex != -1 && replyToIndex < replyToEndIndex) {
            mail.replyTo = base64DecodedMessage.substring(replyToIndex + 10, replyToEndIndex);
        }

        //TODO: set cc & bcc. Send yourself a mail with someone cc'ed and see how it is encoded.
        mail.cc = "";

        mail.bcc = "";

        mail.envelopeDownloaded = true;

        return mail;
    }

    @Override
    public Mail fetchMailBody(Mail mail) {
        ServerResponse mailRequest = retr(mail.mailNr);

        String base64DecodedMessage = mailRequest.decodeBase64Parts();

        if (mailRequest.statusIndicator == ServerResponse.STATUS.ERR) {
            throw new IllegalArgumentException("Error: Mail " + mail.mailNr + " was not found.\n" + mailRequest.message);
        }

        int boundaryIndex = base64DecodedMessage.indexOf("boundary=\"");
        int boundaryEndIndex = base64DecodedMessage.indexOf("\"", boundaryIndex+10);

        if (boundaryIndex == -1 || boundaryEndIndex == -1 || boundaryIndex <= boundaryEndIndex) {
            mail.mailBody = "Couldn't parse mail body. Showing it as it is:\n" + base64DecodedMessage;
            mail.bodyDownloaded = true;
            return mail;
        }

        String boundary = base64DecodedMessage.substring(boundaryIndex+10, boundaryEndIndex);

        int mailBodyIndex = base64DecodedMessage.indexOf("--" + boundary, boundaryEndIndex);
        int mailBodyEndIndex = base64DecodedMessage.indexOf("--" + boundary + "--", mailBodyIndex);

        if (mailBodyIndex == -1 || mailBodyEndIndex == -1 || mailBodyIndex <= mailBodyIndex) {
            mail.mailBody = "Couldn't parse mail body. Showing it as it is:\n" + base64DecodedMessage;
            mail.bodyDownloaded = true;
            return mail;
        }

        mail.mailBody = base64DecodedMessage.substring(mailBodyIndex, mailBodyEndIndex);
        mail.bodyDownloaded = true;
        return mail;
    }

    @Override
    public int getNumberOfMails() {
        ServerResponse response = stat();
        //TODO check if response OK
        String numberToBeParsed = response.message.substring(0, response.message.indexOf(" "));
        int numberOfMails = Integer.parseInt(numberToBeParsed);
        return numberOfMails;
    }

    @Override
    public void closeConnection() {
        quit();
        loggedIn = false;
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public boolean connectionIsReadyToUse() {
        if (!isLoggedIn()) {
            return false;
        }
        else {
            writeToSocket("NOOP");
            if (readSocket(false).statusIndicator == ServerResponse.STATUS.OK) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void reconnect(int retryCount) throws IOException {
        for (int i = 0; i < retryCount; i++) {
            System.out.println("Trying to reconnect. Attempt nr. " + i);
            establishConnection();
            if (connectionIsReadyToUse()) {
                System.out.println("Reconnected.");
                break;
            }
        }
    }



    private ServerResponse stat() {
        writeToSocket("STAT");
        return readSocket(false);
    }

    private ServerResponse list() {
        writeToSocket("LIST");
        return readSocket(true);
    }

    private ServerResponse top(int mailNumber, int lines) {
        writeToSocket("TOP " + mailNumber + " " + lines);
        return readSocket(true);
    }

    private ServerResponse retr(int number) {
        writeToSocket("RETR " + number);
        return readSocket(true);
    }

    private ServerResponse uidl(int msgnr) {
        writeToSocket("UIDL " + msgnr);
        return readSocket(false);
    }

    private ServerResponse uidl() {
        writeToSocket("UIDL");
        return readSocket(true);
    }

    private void quit() {
        writeToSocket("QUIT");
        System.out.println("Response: " + readSocket(false));
        try {
            connection.close();
        }
        catch (IOException e) {
            //TODO throw error.
        }
    }

    private ServerResponse readSocket(boolean multiline) {
        try {
            Reader pop3Reader = new InputStreamReader(connection.getInputStream());
            StringBuilder response = new StringBuilder();

            while (true) {
                response.append((char) pop3Reader.read());
                if (!multiline && response.length() > 2 && response.substring(response.length()-2).equals("\r\n")) { break; }
                else if (multiline && response.length() > 5 && response.substring(response.length()-5).equals("\r\n.\r\n")) { break; }
            }

            return new ServerResponse(response.toString());
        }
        catch (IOException e) {
            System.out.println("An I/O error occurred. The connection might have timed out. " + e.getMessage());
            return new ServerResponse("");
        }
    }

    private void writeToSocket(String message) {
        try {
            Writer pop3Writer = new OutputStreamWriter(connection.getOutputStream());
            pop3Writer.write(message + CRLF);
            pop3Writer.flush();
        }
        catch (IOException e) {
            System.out.println("An I/O error occurred. The connection might have timed out. " + e.getMessage());
        }
    }

    private class ServerResponse {
        enum STATUS {OK, ERR, UNKNOWN}
        STATUS statusIndicator;
        String message;

        ServerResponse(String response) {
            if (response.startsWith("+OK")) { statusIndicator = STATUS.OK; }
            else if (response.startsWith("-ERR")) { statusIndicator = STATUS.ERR; }
            else { statusIndicator = STATUS.UNKNOWN; }

            message = response.replaceFirst("(\\+OK[ ]?|-ERR[ ]?)", "");
        }

        public String decodeBase64Parts() {
            String base64DecodedMessage = message;
            String prefix = "=?utf-8?B?";
            String suffix = "?=";

            int currentIndex = base64DecodedMessage.indexOf(prefix);
            int currentEndIndex = base64DecodedMessage.indexOf(suffix, currentIndex);

            while (currentIndex != -1 && currentEndIndex != -1) {
                String encodedText = base64DecodedMessage.substring(currentIndex + prefix.length(), currentEndIndex);
                String decodedText = new String(Base64.getDecoder().decode(encodedText));
                //System.out.println("Replacing: " + encodedText + "\n" + decodedText);
                base64DecodedMessage = base64DecodedMessage.substring(0, currentIndex) + decodedText + base64DecodedMessage.substring(currentEndIndex+suffix.length(), base64DecodedMessage.length());
                currentIndex = base64DecodedMessage.indexOf(prefix);
                currentEndIndex = base64DecodedMessage.indexOf(suffix, currentIndex);
            }
            return base64DecodedMessage;
        }

        @Override
        public String toString() { return statusIndicator.toString() + " " + message; }
    }

}
