package mailclient.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MailSender extends JFrame implements ActionListener {
    private JToolBar buttonsToolbar;
    private JButton cancelButton, sendButton;
    private JLabel senderLabel, receiverLabel, ccLabel, bccLabel, subjectLabel;
    private JTextField senderField, receiverField, ccField, bccField, subjectField;
    private JTextArea mailTextArea;
    private ImageIcon cancelIcon, sendIcon, titleIcon;
    private MainWindow parentFrame;
    private String sender, receiver, cc, bcc, subject, mailBody;

    public static void main(String[] args) {
        new MailSender(null);
    }

    MailSender(MainWindow parentFrame) {
        this.parentFrame = parentFrame;

        this.setTitle("New Mail...");
        titleIcon = new ImageIcon(getClass().getResource("/icons/email.png"));
        this.setIconImage(titleIcon.getImage());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        buttonsToolbar = new JToolBar();

        sendButton = new JButton();
        sendButton.setText("Send Mail");
        sendIcon = new ImageIcon(getClass().getResource("/icons/writing_hand.png"));
        sendButton.setIcon(sendIcon);
        sendButton.addActionListener(this);
        buttonsToolbar.add(sendButton);

        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelIcon = new ImageIcon(getClass().getResource("/icons/x.png"));
        cancelButton.setIcon(cancelIcon);
        cancelButton.addActionListener(this);
        buttonsToolbar.add(cancelButton);

        this.add(buttonsToolbar, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        JPanel info1 = new JPanel(new FlowLayout(FlowLayout.LEFT,4,4));
        senderLabel = new JLabel("Sender: ");
        senderLabel.setPreferredSize(new Dimension(70,25));
        senderField = new JTextField(sender);
        senderField.setPreferredSize(new Dimension(250,25));
        info1.add(senderLabel);
        info1.add(senderField);
        centerContainer.add(info1);

        JPanel info2 = new JPanel(new FlowLayout(FlowLayout.LEFT,4,4));
        receiverLabel = new JLabel("Receiver: ");
        receiverLabel.setPreferredSize(new Dimension(70,25));
        receiverField = new JTextField(receiver);
        receiverField.setPreferredSize(new Dimension(250,25));
        info2.add(receiverLabel);
        info2.add(receiverField);
        centerContainer.add(info2);

        JPanel info3 = new JPanel(new FlowLayout(FlowLayout.LEFT,4,4));
        ccLabel = new JLabel("CC: ");
        ccLabel.setPreferredSize(new Dimension(70,25));
        ccField = new JTextField(cc);
        ccField.setPreferredSize(new Dimension(250,25));
        info3.add(ccLabel);
        info3.add(ccField);
        centerContainer.add(info3);


        JPanel info4 = new JPanel(new FlowLayout(FlowLayout.LEFT,4,4));
        bccLabel = new JLabel("BCC: ");
        bccLabel.setPreferredSize(new Dimension(70,25));
        bccField = new JTextField(bcc);
        bccField.setPreferredSize(new Dimension(250,25));
        info4.add(bccLabel);
        info4.add(bccField);
        centerContainer.add(info4);

        JPanel info5 = new JPanel(new FlowLayout(FlowLayout.LEFT,4,4));
        subjectLabel = new JLabel("Subject: ");
        subjectLabel.setPreferredSize(new Dimension(70,25));
        subjectField = new JTextField(subject);
        subjectField.setPreferredSize(new Dimension(250,25));
        info5.add(subjectLabel);
        info5.add(subjectField);
        centerContainer.add(info5);

        centerContainer.add(Box.createRigidArea(new Dimension(4, 4)));

        mailTextArea = new JTextArea(mailBody);
        JScrollPane mailScrollPane = new JScrollPane(mailTextArea);
        mailScrollPane.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        centerContainer.add(mailScrollPane);

        this.add(centerContainer, BorderLayout.CENTER);

        this.setSize(500, 500);
        this.setLocationRelativeTo(parentFrame);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
