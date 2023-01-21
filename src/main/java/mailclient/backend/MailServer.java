package mailclient.backend;

public class MailServer {
    private String description;
    private String pop3Address;
    private int pop3InsecurePort;
    private int pop3SecurePort;
    private String smtpAddress;
    private int smtpInsecurePort;
    private int smtpSecurePort;
    private boolean insecureConnectionsAllowed;

    public MailServer(String pop3Address, int pop3InsecurePort, int pop3SecurePort, String smtpAddress, int smtpInsecurePort, int smtpSecurePort, boolean insecureConnectionsAllowed, String description) {
        this.pop3Address = pop3Address;
        this.pop3InsecurePort = pop3InsecurePort;
        this.pop3SecurePort = pop3SecurePort;
        this.smtpAddress = smtpAddress;
        this.smtpInsecurePort = smtpInsecurePort;
        this.smtpSecurePort = smtpSecurePort;
        this.insecureConnectionsAllowed = insecureConnectionsAllowed;
        this.description = description;
    }

    public String getPop3Address() {
        return pop3Address;
    }

    public int getPop3InsecurePort() {
        return pop3InsecurePort;
    }

    public int getPop3SecurePort() {
        return pop3SecurePort;
    }

    public int getPop3Port(boolean secure) {
        return secure ? getPop3SecurePort() : getPop3InsecurePort();
    }

    public String getSmtpAddress() {
        return smtpAddress;
    }

    public int getSmtpInsecurePort() {
        return smtpInsecurePort;
    }

    public int getSmtpSecurePort() {
        return smtpSecurePort;
    }

    public String getDescription() {
        return description;
    }

    public int getSmtpPort(boolean secure) {
        return secure ? getSmtpSecurePort() : getSmtpInsecurePort();
    }

    public boolean insecureConnectionsAllowed() {
        return insecureConnectionsAllowed;
    }
}
