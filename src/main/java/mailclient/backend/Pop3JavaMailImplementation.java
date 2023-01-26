/*
    Sources: https://www.tutorialspoint.com/javamail_api/index.htm
             https://www.baeldung.com/java-properties
 */


package mailclient.backend;

import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.util.MailConnectException;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;

public class Pop3JavaMailImplementation implements Pop3Client {
    private Properties mailProperties;
    private Session emailSession;
    private POP3Store emailStore;
    private POP3Folder emailFolder;
    private Message[] messageArray;

    public Pop3JavaMailImplementation(MailServer myServer, String username, String password) {
        mailProperties = new Properties();
        mailProperties.put("mail.pop3.host", myServer.getPop3Address());
        mailProperties.put("mail.pop3.port", String.valueOf(myServer.getPop3Port()));
        mailProperties.put("mail.pop3.starttls.enable", (myServer.isPop3Encrypted() ? "true" : "false"));
        mailProperties.put("username", username);
        mailProperties.put("password", password);

        establishConnection();
    }

    private void establishConnection() {
        emailSession = Session.getInstance(mailProperties);
        try {
            emailStore = (POP3Store) emailSession.getStore("pop3s");
            emailStore.connect(mailProperties.getProperty("mail.pop3.host"), mailProperties.getProperty("username"), mailProperties.getProperty("password"));
            emailFolder = (POP3Folder) emailStore.getFolder("INBOX");  //Only possible folder for the POP3 Protocol.
            emailFolder.open(Folder.READ_ONLY);
        } catch (AuthenticationFailedException e) {
            System.out.println("Invalid username/password.");
        } catch (NoSuchProviderException | MailConnectException e) {
            System.out.println("Couldn't connect to host: " + e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getNumberOfMails() {
        try {
            return emailFolder.getMessageCount();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TreeMap<Integer, String> fetchMailUIDLs() {
        TreeMap<Integer, String> mailUIDLs = new TreeMap<>();
        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);

        try {
            //downloads all UIDL's aka unique pop3 mail identifiers.
            emailFolder.fetch(emailFolder.getMessages(), fp);
            messageArray = emailFolder.getMessages();

            for(int i = 0; i < messageArray.length; i++) {
                mailUIDLs.put(messageArray[i].getMessageNumber(), emailFolder.getUID(messageArray[i]));
            }
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return mailUIDLs;
    }

    @Override
    public Mail fetchMailEnvelope(Mail mail) {
        try {
            Message message = emailFolder.getMessage(mail.mailNr);

            if (message.getFrom() != null) {
                for (Address addr : message.getFrom()) {
                    mail.from += addr.toString() + ", ";
                }
            }

            if (message.getRecipients(Message.RecipientType.TO) != null) {
                for (Address addr : message.getRecipients(Message.RecipientType.TO)) {
                    mail.to += addr.toString() + ", ";
                }
            }

            if (message.getRecipients(Message.RecipientType.CC) != null) {
                for (Address addr : message.getRecipients(Message.RecipientType.CC)) {
                    mail.cc += addr.toString() + ", ";
                }
            }

            if (message.getRecipients(Message.RecipientType.BCC) != null) {
                for (Address addr : message.getRecipients(Message.RecipientType.BCC)) {
                    mail.bcc += addr.toString() + ", ";
                }
            }

            if (message.getReplyTo() != null) {
                mail.replyTo = (message.getReplyTo()[0].toString());
            }

            if (message.getReceivedDate() != null) {
                mail.date = message.getReceivedDate().toString();
            }

            if (message.getSubject() != null) {
                mail.subject = message.getSubject();
            }

            mail.envelopeDownloaded = true;
        }
        catch (MessagingException e) {
            System.out.println("Error: Mail could not be processed. There might be formatting issues with it: " + e.getMessage());
        }

        return mail;
    }

    @Override
    public Mail fetchMailBody(Mail mail) {
        if (!mail.envelopeDownloaded) {
            System.out.println("Error: Mail envelope was not downloaded. UIDL: " + mail.mailUIDL);
        }

        try {
            mail.mailBody = getMailContent(emailFolder.getMessage(mail.mailNr));
            mail.bodyDownloaded = true;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mail;
    }

    public void closeConnection() {
        try {
            emailFolder.close();
            emailStore.close();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLoggedIn() {
        return emailStore.isConnected();
    }

    @Override
    public boolean connectionIsReadyToUse() {
        return emailStore.isConnected() && emailFolder.isOpen();
    }

    @Override
    public void reconnect(int retryCount) {
        for (int i = 0; i < retryCount; i++) {
            if (connectionIsReadyToUse()) {
                System.out.println("You are connected!");
                break;
            }
            System.out.println("Trying to reconnect. Attempt nr. " + i);
            establishConnection();
        }
    }

    private String getMailContent(Part mailpart) throws MessagingException, IOException {   //Message implements Part
        if (mailpart.isMimeType("text/plain")) {
            //https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types ,
            return ((String) mailpart.getContent());
        }
        else if (mailpart.isMimeType("message/rfc822")) {
            //Nested message.
            return getMailContent((Part) mailpart.getContent());
        }
        else if (mailpart.isMimeType("multipart/*")) {
            //Multiple message parts.
            String textPieces = "";
            Multipart parts = (Multipart) mailpart.getContent();
            for (int i = 0; i < parts.getCount(); i++) {
                textPieces += getMailContent(parts.getBodyPart(i)) + "\n";
            }
            return textPieces;
        }
        else {
            //TODO: expand accepted mimetypes.
            return ("Unknown mimetype: " + mailpart.getContent().toString());
        }
    }

}
