/*
    Sources: https://www.tutorialspoint.com/javamail_api/index.htm
             https://www.baeldung.com/java-properties
 */


package mailclient;

import com.sun.mail.util.MailConnectException;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;

public class Pop3JavaMailImplementation implements Pop3Client {
    private Properties mailProperties;
    Session emailSession;
    Store emailStore;
    private boolean loggedIn = false;
    private Message[] mails;

    @Override
    public void listMails() {
        if (mails == null) {
            downloadMails();
        }

        for (int i = 0; i < mails.length; i++) {
            System.out.println("Mail #" + i);
            printMailMetadata(mails[i]);
        }
    }

    @Override
    public void showMail(int mailNumber) {
        try {
            printMailMetadata(mails[mailNumber]);
            printMailContent(mails[mailNumber]);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            //TODO handle exception
        }
    }

    @Override
    public void getNumberOfMails() {
        if (mails == null) {
            downloadMails();
        }

        System.out.println("Number of mails: " + mails.length);
    }

    public void closeConnection() {
        try {
            emailStore.close();
            loggedIn = false;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public boolean connectionIsReadyToUse() {
        return emailStore.isConnected() && isLoggedIn();
    }

    public Pop3JavaMailImplementation(String host, int port, boolean encryptedConnection, String username, String password) {
        mailProperties = new Properties();
        mailProperties.put("mail.pop3.host", host);
        mailProperties.put("mail.pop3.port", String.valueOf(port));
        mailProperties.put("mail.pop3.starttls.enable", (encryptedConnection ? "true" : "false"));
        mailProperties.put("username", username);
        mailProperties.put("password", password);

        establishConnection();
    }

    private void establishConnection() {
        emailSession = Session.getInstance(mailProperties);
        try {
            emailStore = emailSession.getStore("pop3s");
            emailStore.connect(mailProperties.getProperty("mail.pop3.host"), mailProperties.getProperty("username"), mailProperties.getProperty("password"));
            loggedIn = true;
        } catch (AuthenticationFailedException e) {
            System.out.println("Invalid username/password.");
        } catch (NoSuchProviderException | MailConnectException e) {
            System.out.println("Couldn't connect to host: " + e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadMails() {
        try {
            Folder emailFolder = emailStore.getFolder("INBOX");  //Only possible folder for the POP3 Protocol.
            emailFolder.open(Folder.READ_ONLY);
            mails = emailFolder.getMessages();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printMailMetadata(javax.mail.Message mail) {
        try {
            // FROM
            Address[] senders = mail.getFrom();
            if (senders != null) {
                System.out.print("FROM: ");
                for (int i = 0; i < senders.length; i++) {
                    System.out.print(senders[i].toString());
                    System.out.print( i < senders.length-1 ? ", " : "." );   //Komma bis auf die letzte Mail und danach einen Punkt.
                }
                System.out.println();
            }

            // TO
            Address[] recipientsTO = mail.getRecipients(Message.RecipientType.TO);
            Address[] recipientsCC = mail.getRecipients(Message.RecipientType.CC);
            Address[] recipientsBCC = mail.getRecipients(Message.RecipientType.BCC);

            if (recipientsTO != null) {
                System.out.print("TO: ");
                for (int i = 0; i < recipientsTO.length; i++) {
                    System.out.print(recipientsTO[i]);
                    System.out.print( i < recipientsTO.length-1 ? ", " : "." );
                }
                System.out.println();
            }

            if (recipientsCC != null) {
                System.out.print("CC: ");
                for (int i = 0; i < recipientsCC.length; i++) {
                    System.out.print(recipientsCC[i]);
                    System.out.print( i < recipientsCC.length-1 ? ", " : "." );
                }
                System.out.println();
            }

            if (recipientsBCC != null) {
                System.out.print("BCC: ");
                for (int i = 0; i < recipientsBCC.length; i++) {
                    System.out.print(recipientsBCC[i]);
                    System.out.print( i < recipientsBCC.length-1 ? ", " : "." );
                }
                System.out.println();
            }

            // SUBJECT
            if (mail.getSubject() != null) {
                System.out.println("SUBJECT: " + mail.getSubject());
            }
        } catch (MessagingException e) {
            System.out.println("Error: Mail could not be processed. There might be formatting issues with it: " + e.getMessage());
        }
    }

    private static void printMailContent(Part mailpart) throws MessagingException, IOException {   //Message implements Part
        if (mailpart.isMimeType("text/plain")) {  //https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types ,
            System.out.println((String) mailpart.getContent());
        }
        else if (mailpart.isMimeType("message/rfc822")) {  //Nested message.
            printMailContent((Part) mailpart.getContent());
        }
        else if (mailpart.isMimeType("multipart/*")) {
            Multipart parts = (Multipart) mailpart.getContent();
            for (int i = 0; i < parts.getCount(); i++) {  //Bearbeite jeden Teil einzeln.
                printMailContent(parts.getBodyPart(i));
            }
        }
        else {
            //TODO: expand accepted mimetypes.
            System.out.println("Unknown mimetype: " + mailpart.getContent().toString());
        }

    }

}
