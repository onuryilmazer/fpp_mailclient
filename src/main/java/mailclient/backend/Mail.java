package mailclient.backend;

import java.io.Serializable;

public class Mail implements Serializable {
    public int mailNr = 0;
    public String mailUIDL = "";

    //mail envelope
    public String from = "", to = "", cc = "", bcc = "", replyTo = "", date = "", subject = "";
    //mail body
    public String mailBody = "";

    //set to true only after downloading the relevant parts.
    public boolean envelopeDownloaded=false;
    public boolean bodyDownloaded=false;
    public boolean read = false;

}
