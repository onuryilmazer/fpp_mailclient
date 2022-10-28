/*
Sources used:
    Marc Loy, Patrick Niemeyer, Daniel Leuck - Learning Java - An Introduction to Real-World Programming with Java-O'Reilly: Chapter 11 Sockets & Streams
    A Guide to Java Sockets: https://www.baeldung.com/a-guide-to-java-sockets
    Java IO - Streams: https://jenkov.com/tutorials/java-io/streams.html
 */

package mailclient;

import java.io.*;
import java.net.*;

public class Pop3Client {
    private String serverAddress;
    private int serverPort;
    private Socket connection;
    static final String CRLF = "\r\n";  //Line termination character: carriage return line feed pair.

    public Pop3Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        connectToTheMailServer();
    }

    public void login(String username, String password) {
        writeToSocket("USER " + username);
        System.out.println("Response: " + readSocket(false));
        writeToSocket("PASS " + password);
        System.out.println("Response: " + readSocket(false));
    }

    public void stat() {
        writeToSocket("STAT");
        System.out.println("Response: " + readSocket(false));
    }

    public void list() {
        writeToSocket("LIST");
        System.out.println("Response: " + readSocket(true));
    }

    public void retr(int number) {
        writeToSocket("RETR " + number);
        System.out.println("Response: " + readSocket(true));
    }

    private void connectToTheMailServer() {
        if (connection == null || connection.isClosed()) {
            try {
                connection = new Socket(serverAddress, serverPort);
                if (readSocket(false).statusIndicator == ServerResponse.STATUS.OK) {
                    System.out.println("Connection successful.");
                }
            }
            catch (UnknownHostException e) {
                System.out.println("Error: Hostname couldn't be resolved. " + e.getMessage());
            }
            catch (IOException e) {
                System.out.println("Error: IOException. " + e.getMessage());
            }
        }
        else {
            System.out.println("Already connected.");
        }
    }

    private ServerResponse readSocket(boolean multiline) {
        try {
            Reader pop3reader = new InputStreamReader(connection.getInputStream());
            StringBuilder response = new StringBuilder();

            while (true) {
                response.append((char) pop3reader.read());
                if (!multiline && response.length() > 2 && response.substring(response.length()-2).equals("\r\n")) { break; }
                else if (multiline && response.length() > 5 && response.substring(response.length()-5).equals("\r\n.\r\n")) { break; }
            }

            return new ServerResponse(response.toString());
        }
        catch (IOException e) {
            System.out.println("An I/O error occurred. Connection might have timed out. " + e.getMessage());
            return new ServerResponse("");
        }
    }

    private void writeToSocket(String message) {
        try {
            Writer pop3writer = new OutputStreamWriter(connection.getOutputStream());
            pop3writer.write(message + CRLF);
            pop3writer.flush();
        }
        catch (IOException e) {
            System.out.println("An I/O error occurred. Connection might have timed out. " + e.getMessage());
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
