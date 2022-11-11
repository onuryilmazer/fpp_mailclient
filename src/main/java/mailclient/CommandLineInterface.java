package mailclient;

import java.util.*;

public class CommandLineInterface {
    public static void main(String[] args) {

        int connectionMethod = showMenuDialog("Connection method",
                new String[]{"Websockets", "JavaMail API"});

        int serverChoice = showMenuDialog("Mail server",
                new String[]{"pop3.uni-jena.de", "pop.gmx.net (only encrypted connection possible)", "Enter a new URL."});

        String serverURL;
        boolean onlyEncryptedConnectionAllowed = false;
        int insecurePort=110;
        int securePort=995;

        if (serverChoice == 0) {
            serverURL = "pop3.uni-jena.de";
            onlyEncryptedConnectionAllowed = false;
            insecurePort = 110;
            securePort = 995;
        }

        else if (serverChoice == 1) {
            serverURL = "pop.gmx.net";
            onlyEncryptedConnectionAllowed = true;
            securePort = 995;
        }
        else {
            serverURL = getStringFromUser("Server address");
            System.out.println("Does your server support insecure connections as well?");
            int choice = showMenuDialog("Does your server support insecure connections as well?", new String[]{"Yes", "No"});
            onlyEncryptedConnectionAllowed = (choice == 0 ? false : true);

            securePort = getIntegerFromUser("Port for secure connections: ");

            if (!onlyEncryptedConnectionAllowed) {
                insecurePort = getIntegerFromUser("Port for insecure connections (): ");
            }
        }


        boolean connectEncrypted = true;
        if (!onlyEncryptedConnectionAllowed) {
            int encryptedConnection = showMenuDialog("Encryption", new String[]{"Yes", "No"});
            if (encryptedConnection == 0) {
                connectEncrypted = true;
            }
            else {
                connectEncrypted = false;
            }
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

        Pop3Client myClient;
        if (connectionMethod == 0) {
            if (connectEncrypted) {
                myClient = new Pop3WebSocketsImplementation(serverURL, securePort, true, username, password);
            }
            else {
                myClient = new Pop3WebSocketsImplementation(serverURL, insecurePort, false, username, password);
            }

        }
        else {
            if (connectEncrypted) {
                myClient = new Pop3JavaMailImplementation(serverURL, securePort, username, password, true);
            }
            else {
                myClient = new Pop3JavaMailImplementation(serverURL, insecurePort, username, password, false);
            }
        }


        while (myClient.connectionIsReadyToUse()) {
            int command = showMenuDialog("Pick a command",
                    new String[]{"Show the number of mails", "List all mails", "Read a mail", "End connection"});

            switch (command) {
                case 0:
                    myClient.getNumberOfMails();
                    break;

                case 1:
                    myClient.listMails();
                    break;

                case 2:
                    myClient.showMail(getIntegerFromUser("Enter the mail number"));
                    break;

                case 3:
                    myClient.closeConnection();
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
