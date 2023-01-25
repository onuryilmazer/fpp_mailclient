package mailclient.backend;

public class Mail {
    public int mailNr;
    public String mailUIDL;

    //mail envelope
    public String from, to, cc, bcc, replyTo, date, subject;
    //mail body
    public String mailBody;

    //set to true only after downloading the relevant parts.
    public boolean envelopeDownloaded=false;
    public boolean bodyDownloaded=false;

}
