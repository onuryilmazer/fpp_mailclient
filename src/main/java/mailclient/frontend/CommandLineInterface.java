package mailclient.frontend;

import mailclient.backend.*;

import java.util.*;

public class CommandLineInterface {
    public static void main(String[] args) {

        MailServer[] servers = {
                new MailServer("pop3.uni-jena.de", 110, false, "smtp.uni-jena.de", 25, false, "uni.jena.de (POP3 & SMTP unencrypted)"),
                new MailServer("pop3.uni-jena.de", 995, true, "smtp.uni-jena.de", 587, true, "uni.jena.de (POP3 & SMTP encrypted)"),
                new MailServer("pop.gmx.net", 995, true, "mail.gmx.net", 587,true, "gmx.net (POP3 & SMTP) encrypted")
        };

        int connectionMethod = showMenuDialog(
                "Connection method",
                new String[]{"Websockets", "JavaMail API"}
        );

        for (int i = 0; i < servers.length; i++) {
            System.out.println(i + ": " + servers[i].getDescription());
        }

        int serverChoice = getIntegerFromUser("Mail server (enter -1 to pick a custom server): ");

        while (serverChoice < -1 || serverChoice >= servers.length) {
            System.out.println("Invalid choice, try again.");
            serverChoice = getIntegerFromUser("Mail server (enter -1 to pick a custom server): ");
        }

        MailServer myServer;
        if (serverChoice == -1) {
            String description = getStringFromUser("Server name: ");
            String pop3URL = getStringFromUser("POP3 Server Address: ");
            int pop3Port = getIntegerFromUser("POP3 Port: ");
            boolean pop3Encrypted = showMenuDialog("Is this port intended for encrypted connections?", new String[]{"Yes", "No"}) == 0;
            String smtpURL = getStringFromUser("SMTP Server Address: ");
            int smtpPort = getIntegerFromUser("SMTP Port: ");
            boolean smtpEncrypted = showMenuDialog("Is this port intended for encrypted connections?", new String[]{"Yes", "No"}) == 0;

            myServer = new MailServer(pop3URL, pop3Port, pop3Encrypted, smtpURL, smtpPort, smtpEncrypted, description);
        }
        else {
            myServer = servers[serverChoice];
        }

        int credentials = showMenuDialog(
                "User credentials",
                new String[]{"Read the values from the environment variables MAIL_USERNAME, MAIL_PASSWORD.", "Enter a new username and password."}
        );


        String username, password;
        if (credentials == 0) {
            username = System.getenv("MAIL_USERNAME");
            password = System.getenv("MAIL_PASSWORD");
        }
        else {
            username = getStringFromUser("Username");
            password = getStringFromUser("Password");
        }

        Pop3Client myClientReader;
        SmtpClient myClientSender;

        try {
            if (connectionMethod == 0) {
                myClientReader = new Pop3WebSocketsImplementation(myServer, username, password);
                myClientSender = new SmtpWebSocketsImplementation(myServer, username, password);
            }
            else {
                myClientReader = new Pop3JavaMailImplementation(myServer, username, password);
                myClientSender = new SmtpJavaMailImplementation(myServer, username, password);
            }
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Try again.");
            return;
        }


        while (myClientReader.connectionIsReadyToUse() && myClientSender.connectionIsReadyToUse()) {
            int command = showMenuDialog("Pick a command",
                    new String[]{"Show the number of mails", "List all mails", "Read a mail", "Send a mail", "End connection"});

            switch (command) {
                case 0:
                    System.out.println("Number: " + myClientReader.getNumberOfMails());
                    break;

                case 1:
                    TreeMap<Integer, String> allMails = myClientReader.fetchMailUIDLs();
                    for (Map.Entry mapEntry : allMails.entrySet()) {
                        System.out.println(mapEntry.getKey() + " (UID: " + mapEntry.getValue() + ")");
                    }
                    break;

                case 2:
                    int mailNumber = getIntegerFromUser("Enter the mail number");
                    Mail selectedMail = new Mail();
                    selectedMail.mailNr = mailNumber;
                    myClientReader.fetchMailEnvelope(selectedMail);
                    myClientReader.fetchMailBody(selectedMail);
                    System.out.println(selectedMail.mailBody);
                    break;
                case 3:
                    String sender = getStringFromUser("Sender address: ");
                    List<String> receivers = new LinkedList<>();

                    while (true) {
                        String receiver = getStringFromUser("Receiver addresses (enter a single dot (.) after the last receiver): ");
                        if (receiver.equals(".")) {break;}
                        else {receivers.add(receiver);}
                    }

                    String subject = getStringLineFromUser("Mail subject: ");
                    String mailBody = getStringLineFromUser("Your message: ");

                    myClientSender.sendMail(sender, receivers.toArray(new String[receivers.size()]), subject, mailBody);
                    break;

                case 4:
                    myClientReader.closeConnection();
                    break;

                default:
                    break;
            }
        }
    }


    private static int showMenuDialog(String prompt, String[] menuOptions) {
        System.out.println(prompt + ": ");
        for (int i = 0; i < menuOptions.length; i++) {
            System.out.println("    " + i + ": " + menuOptions[i]);
        }

        Scanner consoleReader = new Scanner(System.in);
        int userInput;

        while (true) {
            try {
                userInput = Integer.parseInt(consoleReader.next());

                if (userInput >= 0 && userInput < menuOptions.length) {
                    break;
                }
                else {
                    System.out.println("Please pick a number from the list.");
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Please enter an integer.");
            }
        }

        return userInput;
    }

    private static String getStringFromUser(String prompt) {
        System.out.println(prompt + ": ");
        Scanner consoleReader = new Scanner(System.in);

        return consoleReader.next();
    }

    private static String getStringLineFromUser(String prompt) {
        System.out.println(prompt + ": ");
        Scanner consoleReader = new Scanner(System.in);

        return consoleReader.nextLine();
    }

    private static int getIntegerFromUser(String prompt) {
        System.out.println(prompt + ": ");
        Scanner consoleReader = new Scanner(System.in);
        int userInput;

        while (true) {
            try {
                userInput = Integer.parseInt(consoleReader.next());
                break;
            }
            catch (NumberFormatException e) {
                System.out.println("Please enter an integer.");
            }
        }

        return userInput;
    }

}
