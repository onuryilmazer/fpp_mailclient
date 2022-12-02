package mailclient;

import java.util.*;

public class CommandLineInterface {
    public static void main(String[] args) {

        MailServer[] servers = {new MailServer("pop3.uni-jena.de", 110, 995, "smtp.uni-jena.de", 25, 587, true, "uni.jena.de (POP3 & SMTP)"),
                                    new MailServer("pop.gmx.net", -1, 995, "mail.gmx.net", -1, 587,false, "gmx.net (POP3 & SMTP)")};

        int connectionMethod = showMenuDialog("Connection method",
                new String[]{"Websockets", "JavaMail API"});

        for (int i = 0; i < servers.length; i++) { System.out.println(i + ": " + servers[i].getDescription()); }
        int serverChoice = getIntegerFromUser("Mail server (enter -1 to pick a custom server): ");

        while (serverChoice < -1 || serverChoice >= servers.length) {
            System.out.println("Invalid choice, try again.");
            serverChoice = getIntegerFromUser("Mail server (enter -1 to pick a custom server): ");
        }

        MailServer myServer;
        if (serverChoice == -1) {
            String description = getStringFromUser("Server name: ");
            boolean insecureConnectionsAllowed = showMenuDialog("Does your server support insecure connections?", new String[]{"Yes", "No"}) == 0;
            String pop3URL = getStringFromUser("Server POP3 address: ");
            int securePop3Port = getIntegerFromUser("Port for secure POP3 connections: ");
            int insecurePop3Port = insecureConnectionsAllowed ? getIntegerFromUser("Port for insecure POP3 connections: ") : -1;
            String smtpURL = getStringFromUser("Server SMTP address: ");
            int secureSmtpPort = getIntegerFromUser("Port for secure SMTP connections: ");
            int insecureSmtpPort = insecureConnectionsAllowed ? getIntegerFromUser("Port for insecure SMTP connections: ") : -1;

            myServer = new MailServer(pop3URL, insecurePop3Port, securePop3Port, smtpURL, secureSmtpPort, insecureSmtpPort, insecureConnectionsAllowed, description);
        }
        else {
            myServer = servers[serverChoice];
        }

        boolean connectEncrypted = true;
        if (myServer.insecureConnectionsAllowed()) {
            connectEncrypted = showMenuDialog("Encryption", new String[]{"Yes", "No"}) == 0;
        }

        int credentials = showMenuDialog("User credentials",
                new String[]{"Read the values from the environment variables MAIL_USERNAME, MAIL_PASSWORD.", "Enter a new username and password."});

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

        if (connectionMethod == 0) {
            myClientReader = new Pop3WebSocketsImplementation(myServer.getPop3Address(), myServer.getPop3Port(connectEncrypted), connectEncrypted, username, password);
            myClientSender = new SmtpClientWebSocketsImplementation(myServer.getSmtpAddress(), myServer.getSmtpPort(connectEncrypted), connectEncrypted, username, password);
        }
        else {
            myClientReader = new Pop3JavaMailImplementation(myServer.getPop3Address(), myServer.getPop3Port(connectEncrypted), connectEncrypted, username, password);
            myClientSender = null;
            //TODO: JavaMail smtp
        }


        while (myClientReader.connectionIsReadyToUse() && myClientSender.connectionIsReadyToUse()) {
            int command = showMenuDialog("Pick a command",
                    new String[]{"Show the number of mails", "List all mails", "Read a mail", "Send a mail", "End connection"});

            switch (command) {
                case 0:
                    myClientReader.getNumberOfMails();
                    break;

                case 1:
                    myClientReader.listMails();
                    break;

                case 2:
                    myClientReader.showMail(getIntegerFromUser("Enter the mail number"));
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
        String userInput = consoleReader.next();

        return userInput;
    }

    private static String getStringLineFromUser(String prompt) {
        System.out.println(prompt + ": ");
        Scanner consoleReader = new Scanner(System.in);
        String userInput = consoleReader.nextLine();

        return userInput;
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
