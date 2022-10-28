package mailclient;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Pop3Client unijena = new Pop3Client("pop3.uni-jena.de", 110);

        //credentials are stored as environment variables for security reasons: https://www.twilio.com/blog/working-with-environment-variables-in-java
        unijena.login(System.getenv("JENAMAIL_UNAME"), System.getenv("JENAMAIL_PASSW"));

        unijena.stat();
        unijena.list();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Pick a mail: ");
        unijena.retr(scanner.nextInt());

    }
}
