package mailclient;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;

public class SmtpClientWebSocketsImplementation implements SmtpClient {
    private String serverAddress;
    private int serverPort;
    private String username;
    private String password;
    private Socket connection;
    private boolean encryptedConnection;
    private boolean loggedIn = false;
    private static final String CRLF = "\r\n";  //Line termination character.

    public SmtpClientWebSocketsImplementation(String serverAddress, int serverPort, boolean encryptedConnection, String username, String password) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.username = username;
        this.password = password;
        this.encryptedConnection = encryptedConnection;
        establishConnection();
    }

    @Override
    public void sendMail(String sender, String[] recipients, String subject, String message) {
        if (!connectionIsReadyToUse()) {
            System.out.println("Error: connection is not ready to use. Can't send mail.");
        }

        writeToSocket("MAIL FROM: " + sender);
        System.out.println(readSocket().toString());

        for(String r : recipients) {
            writeToSocket("RCPT TO: " + r);
            System.out.println(readSocket().toString());
        }

        writeToSocket("DATA");
        System.out.println(readSocket().toString());
        writeToSocket("Subject: " + subject);
        writeToSocket(message);
        writeToSocket(CRLF + "." + CRLF);
        System.out.println(readSocket().toString());
    }

    @Override
    public boolean connectionIsReadyToUse() {
        if (!loggedIn) {
            return false;
        }
        else {
            writeToSocket("NOOP");
            return readSocket().statusCode == 250;
        }
    }

    @Override
    public void reconnect(int retryCount) {
        for (int i = 0; i < retryCount; i++) {
            System.out.println("Trying to reconnect. Attempt nr. " + i);
            establishConnection();
            if (connectionIsReadyToUse()) {
                break;
            }
        }
    }

    @Override
    public void endConnection() {
        writeToSocket("QUIT");
        System.out.println(readSocket().toString());
    }

    private void establishConnection() {
        if (connection == null || connection.isClosed()) {
            try {
                connection = new Socket(serverAddress, serverPort);

                ServerResponse s1 = readSocket();
                System.out.println(s1.toString());
                if (s1.statusCode != 220) {
                    System.out.println("Error: Couldn't connect to the server");
                }

                writeToSocket("EHLO " + "host1");
                ServerResponse s2 = readSocket();
                System.out.println(s2.toString());
                if (s2.statusCode != 250) {
                    System.out.println("Error: EHLO command was not accepted.");
                }

                if (encryptedConnection) {
                    writeToSocket("STARTTLS");
                    ServerResponse tlsResponse = readSocket();
                    System.out.println(tlsResponse.toString());
                    if (tlsResponse.statusCode != 220) {
                        System.out.println("Error: STARTTLS command was not accepted.");
                    }
                    SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    connection = (SSLSocket) sslsocketfactory.createSocket(connection, connection.getInetAddress().getHostAddress(), connection.getPort(), true);
                    ((SSLSocket)connection).startHandshake();
                }

                writeToSocket("AUTH LOGIN");
                if (readSocket().statusCode != 334) {
                    System.out.println("Error: Can't login.");
                }

                writeToSocket(Base64.getEncoder().encodeToString((username).getBytes()));
                if (readSocket().statusCode != 334) {
                    System.out.println("Error: Username was not accepted by the server.");
                }

                writeToSocket(Base64.getEncoder().encodeToString((password).getBytes()));
                if (readSocket().statusCode != 235) {
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

    private ServerResponse readSocket() {
        try {
            BufferedReader smtpReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            while (true) {
                String line = smtpReader.readLine();
                response.append(line);
                if (line.charAt(3) == ' ') { break; }
                else if (line.charAt(3) == '-') { continue; }
            }

            return new ServerResponse(response.toString());
        }
        catch (IOException e) {
            System.out.println("An I/O error occurred. " + e.getMessage());
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
            System.out.println("An I/O error occurred. " + e.getMessage());
        }
    }

    private class ServerResponse {
        int statusCode;
        boolean multiline;
        String infoText;

        ServerResponse(String response) {
            statusCode = response.length() == 0 ? -1 : Integer.parseInt(response.substring(0,3));
            infoText = response.length() == 0 ? "" : response.substring(3);
            multiline = response.length() == 0 ? false : response.charAt(3) == '-';
        }

        @Override
        public String toString() { return statusCode + " " + infoText; }
    }
}
