/*
    Sources:
        Marc Loy, Patrick Niemeyer, Daniel Leuck - Learning Java - An Introduction to Real-World Programming with Java-O'Reilly: Chapter 11 Sockets & Streams
        A Guide to Java Sockets: https://www.baeldung.com/a-guide-to-java-sockets
        Java IO - Streams: https://jenkov.com/tutorials/java-io/streams.html
 */

package mailclient;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;

public class Pop3WebSocketsImplementation implements Pop3Client {
    private String serverAddress;
    private int serverPort;
    private String username;
    private String password;
    private Socket connection;
    private boolean encryptedConnection;
    private boolean loggedIn = false;
    private static final String CRLF = "\r\n";  //Line termination character: carriage return line feed pair. Source: POP3 Specification.

    @Override
    public void listMails() {
        list();
    }

    @Override
    public void showMail(int mailNumber) {
        retr(mailNumber);
    }

    @Override
    public void getNumberOfMails() {
        stat();
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

    public Pop3WebSocketsImplementation(String host, int port, boolean encryptedConnection, String username, String password) {
        this.serverAddress = host;
        this.serverPort = port;
        this.encryptedConnection = encryptedConnection;
        this.username = username;
        this.password = password;
        establishConnection();
    }

    private void stat() {
        writeToSocket("STAT");
        ServerResponse response = readSocket(false);
        String numberToBeParsed = response.info.substring(0, response.info.indexOf(" "));
        int numberOfMails = Integer.parseInt(numberToBeParsed);
        System.out.println("Number of mails: " + numberOfMails);
    }

    private void list() {
        writeToSocket("LIST");
        System.out.println("Response: " + readSocket(true));
    }

    private void retr(int number) {
        writeToSocket("RETR " + number);
        System.out.println("Response: " + readSocket(true));
    }

    private void quit() {
        writeToSocket("QUIT");
        System.out.println("Response: " + readSocket(false));
        try {
            connection.close();
        }
        catch (IOException e) {

        }

    }

    private void establishConnection() {
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
                    System.out.println("Error: Couldn't connect to the server");
                }

                writeToSocket("USER " + username);
                if (readSocket(false).statusIndicator != ServerResponse.STATUS.OK) {
                    System.out.println("Error: Username was not accepted by the server.");
                }

                writeToSocket("PASS " + password);
                if (readSocket(false).statusIndicator != ServerResponse.STATUS.OK) {
                    System.out.println("Error: Username and/or password was not accepted by the server.");
                }
                else {
                    loggedIn = true;
                }
            }
            catch (UnknownHostException e) {
                System.out.println("Error: Hostname couldn't be resolved: " + e.getMessage());
            }
            catch (IOException e) {
                System.out.println("Error: IOException: " + e.getMessage());
            }
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
        String info;

        ServerResponse(String response) {
            if (response.startsWith("+OK")) { statusIndicator = STATUS.OK; }
            else if (response.startsWith("-ERR")) { statusIndicator = STATUS.ERR; }
            else { statusIndicator = STATUS.UNKNOWN; }

            info = response.replaceFirst("(\\+OK[ ]?|-ERR[ ]?)", "");
        }

        @Override
        public String toString() { return statusIndicator.toString() + " " + info; }
    }

    private class Mail {
        //TODO
    }
}
