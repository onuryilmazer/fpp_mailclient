package mailclient.backend;

public class MailServer {
    private String description;
    private String pop3Address;
    private int pop3Port;
    private boolean pop3Encrypted;
    private String smtpAddress;
    private int smtpPort;
    private boolean smtpEncrypted;

    public MailServer(String pop3Address, int pop3Port, boolean pop3Encrypted, String smtpAddress, int smtpPort, boolean smtpEncrypted, String description) {
        this.pop3Address = pop3Address;
        this.pop3Port = pop3Port;
        this.pop3Encrypted = pop3Encrypted;
        this.smtpAddress = smtpAddress;
        this.smtpPort = smtpPort;
        this.smtpEncrypted = smtpEncrypted;
        this.description = description;
    }

    public String getPop3Address() {
        return pop3Address;
    }

    public int getPop3Port() {
        return pop3Port;
    }

    public String getSmtpAddress() {
        return smtpAddress;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPop3Encrypted() {
        return pop3Encrypted;
    }

    public boolean isSmtpEncrypted() {
        return smtpEncrypted;
    }
}
