package mailclient;

import java.util.*;

public class CommandLineInterface {
    public static void main(String[] args) {

        int connectionMethod = showMenuDialog("Connection method",
                new String[]{"Websockets", "JavaMail API"});

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

        int serverChoice = showMenuDialog("Mail server",
                new String[]{"pop3.uni-jena.de", "Enter a new URL."});

        String serverURL;
        if (serverChoice == 0) {
            serverURL = "pop3.uni-jena.de";
        }
        else {
            serverURL = getStringFromUser("Server address");
        }


        Pop3Client myClient;
        if (connectionMethod == 0) {
            myClient = new Pop3WebSocketsImplementation(serverURL, username, password);
        }
        else {
            myClient = new Pop3JavaMailImplementation(serverURL, username, password, true);
        }


        boolean stayInLoop = true;
        while (stayInLoop) {
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
                    System.out.println("Enter the mail number:");
                    myClient.showMail(getIntegerFromUser());
                    break;

                case 3:
                    myClient.closeConnection();
                    stayInLoop = false;
                    break;

                default:
                    stayInLoop = false;
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

    private static int getIntegerFromUser() {
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
