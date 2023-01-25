package mailclient.frontend;

import mailclient.backend.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends JFrame implements ActionListener {
    private JTextField username, mailAddress, realName, pop3ServerAddress, pop3ServerPort, smtpServerAddress, smtpServerPort;
    private JPasswordField password;
    private JCheckBox showPasswordCheckbox, pop3ServerEncryptedCheckbox, smtpServerEncryptedCheckbox;
    private JButton cancelButton, loginButton;
    private JComboBox savedUsersCombobox, connectionMethodCombobox;
    private JLabel passVisibleEmojiLabel, pop3EncryptionEmojiLabel, smtpEncryptionEmojiLabel;
    private ImageIcon titleIcon, passwordVisibleIcon, passwordInvisibleIcon, encryptionIcon, noencryptionIcon, cancelIcon, loginIcon;
    private int parsedPop3Port, parsedSmtpPort;

    public static void main(String[] args) {
        new LoginScreen();
    }

    LoginScreen() {
        this.setTitle("Login");
        titleIcon = new ImageIcon(getClass().getResource("/icons/email.png"));
        this.setIconImage(titleIcon.getImage());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        /*
         *   ---------- User & Server Inputs ---------
         *  One outer container with BoxLayout and two GridLayout containers inside for user and server input groups.
         */
        JPanel centerBoxPanel = new JPanel();
        centerBoxPanel.setLayout(new BoxLayout(centerBoxPanel, BoxLayout.Y_AXIS));
        this.add(centerBoxPanel, BorderLayout.CENTER);

        JPanel savedUsersPanel = new JPanel();
        savedUsersPanel.setBorder(BorderFactory.createTitledBorder("Saved Users"));
        savedUsersPanel.setLayout(new GridLayout(1,3,10,5));
        savedUsersPanel.add(new JLabel("Select a user:"));
        savedUsersPanel.add(savedUsersCombobox = new JComboBox());
        savedUsersPanel.add(new JLabel(""));
        centerBoxPanel.add(savedUsersPanel);


        JPanel userRelatedInputsPanel = new JPanel();
        userRelatedInputsPanel.setBorder(BorderFactory.createTitledBorder("User"));
        userRelatedInputsPanel.setLayout(new GridLayout(4,3,10,5));

        userRelatedInputsPanel.add(new JLabel("Username:"));
        userRelatedInputsPanel.add(username = new JTextField());
        userRelatedInputsPanel.add(new JLabel(""));

        userRelatedInputsPanel.add(new JLabel("Password:"));
        userRelatedInputsPanel.add(password = new JPasswordField());

        JPanel passwordVisibilityContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
        showPasswordCheckbox = new JCheckBox("Show password", false);
        showPasswordCheckbox.addActionListener(this);  //for changing the emoji.
        passwordVisibleIcon = new ImageIcon(getClass().getResource("/icons/hear_no_evil.png"));
        passwordInvisibleIcon = new ImageIcon(getClass().getResource("/icons/see_no_evil.png"));
        passVisibleEmojiLabel = new JLabel(passwordInvisibleIcon);
        passwordVisibilityContainer.add(passVisibleEmojiLabel);
        passwordVisibilityContainer.add(showPasswordCheckbox);
        userRelatedInputsPanel.add(passwordVisibilityContainer);

        userRelatedInputsPanel.add(new JLabel("Mail Address:"));
        userRelatedInputsPanel.add(mailAddress = new JTextField());
        userRelatedInputsPanel.add(new JLabel(""));

        userRelatedInputsPanel.add(new JLabel("Real Name:"));
        userRelatedInputsPanel.add(realName = new JTextField());
        userRelatedInputsPanel.add(new JLabel(""));

        centerBoxPanel.add(userRelatedInputsPanel);

        encryptionIcon = new ImageIcon(getClass().getResource("/icons/lock.png"));
        noencryptionIcon = new ImageIcon(getClass().getResource("/icons/unlock.png"));

        JPanel serverRelatedInputsPanel = new JPanel();
        serverRelatedInputsPanel.setBorder(BorderFactory.createTitledBorder("Mail server"));
        serverRelatedInputsPanel.setLayout(new GridLayout(6,3, 10, 5));

        serverRelatedInputsPanel.add(new JLabel("Connection method:"));
        serverRelatedInputsPanel.add(connectionMethodCombobox = new JComboBox<>(new String[]{"WebSocket", "JavaMail Library"}));
        serverRelatedInputsPanel.add(new JLabel(""));


        serverRelatedInputsPanel.add(new JLabel("POP3 Address:"));
        serverRelatedInputsPanel.add(pop3ServerAddress = new JTextField());
        serverRelatedInputsPanel.add(new JLabel(""));

        serverRelatedInputsPanel.add(new JLabel("POP3 Port:"));
        serverRelatedInputsPanel.add(pop3ServerPort = new JTextField());

        JPanel encryptionContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
        pop3ServerEncryptedCheckbox = new JCheckBox("Encryption", false);
        pop3ServerEncryptedCheckbox.addActionListener(this);  //for changing the emoji.
        pop3EncryptionEmojiLabel = new JLabel(noencryptionIcon);
        encryptionContainer.add(pop3EncryptionEmojiLabel);
        encryptionContainer.add(pop3ServerEncryptedCheckbox);
        serverRelatedInputsPanel.add(encryptionContainer);

        serverRelatedInputsPanel.add(new JLabel(""));
        serverRelatedInputsPanel.add(new JLabel(""));
        serverRelatedInputsPanel.add(new JLabel(""));

        serverRelatedInputsPanel.add(new JLabel("SMTP Address:"));
        serverRelatedInputsPanel.add(smtpServerAddress = new JTextField());
        serverRelatedInputsPanel.add(new JLabel(""));

        serverRelatedInputsPanel.add(new JLabel("SMTP Port:"));
        serverRelatedInputsPanel.add(smtpServerPort = new JTextField());

        JPanel encryptionContainer2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
        smtpServerEncryptedCheckbox = new JCheckBox("Encryption", false);
        smtpServerEncryptedCheckbox.addActionListener(this);  //for changing the emoji.
        smtpEncryptionEmojiLabel = new JLabel(noencryptionIcon);
        encryptionContainer2.add(smtpEncryptionEmojiLabel);
        encryptionContainer2.add(smtpServerEncryptedCheckbox);
        serverRelatedInputsPanel.add(encryptionContainer2);

        centerBoxPanel.add(serverRelatedInputsPanel);


        /*
            Footer for the Cancel & Login Buttons at the bottom of the window.
        */
        JPanel footer = new JPanel();
        footer.setLayout(new FlowLayout(FlowLayout.RIGHT));
        this.add(footer, BorderLayout.SOUTH);

        footer.add(cancelButton = new JButton("Cancel"));
        footer.add(loginButton = new JButton("Login"));
        cancelButton.addActionListener(this);
        loginButton.addActionListener(this);

        cancelIcon = new ImageIcon(getClass().getResource("/icons/x.png"));
        cancelButton.setIcon(cancelIcon);

        loginIcon = new ImageIcon(getClass().getResource("/icons/arrow_right.png"));
        loginButton.setIcon(loginIcon);

        this.pack();                       //Resizes the frame automatically.
        this.setLocationRelativeTo(null);  //Sets the initial position of the frame as the center of the screen.
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == showPasswordCheckbox) {
            JCheckBox source = (JCheckBox) e.getSource();
            password.setEchoChar(source.isSelected() ? (char) 0 : '*');
            passVisibleEmojiLabel.setIcon( source.isSelected() ? passwordVisibleIcon : passwordInvisibleIcon );
        }
        else if (e.getSource() == pop3ServerEncryptedCheckbox) {
            JCheckBox source = (JCheckBox) e.getSource();
            pop3EncryptionEmojiLabel.setIcon( source.isSelected() ? encryptionIcon : noencryptionIcon );
        }
        else if (e.getSource() == smtpServerEncryptedCheckbox) {
            JCheckBox source = (JCheckBox) e.getSource();
            smtpEncryptionEmojiLabel.setIcon( source.isSelected() ? encryptionIcon : noencryptionIcon );
        }
        else if (e.getSource() == loginButton) {
            if (inputsAreValid()) {
                MailServer myServer = new MailServer(pop3ServerAddress.getText(), parsedPop3Port, pop3ServerEncryptedCheckbox.isSelected(), smtpServerAddress.getText(), parsedSmtpPort, smtpServerEncryptedCheckbox.isSelected(), "GUI new server");

                Pop3Client myClientReader;
                SmtpClient myClientSender;

                //TODO call main window and dispose of this one.
                //TODO throw errors in the backend classes and catch them on GUI (instead of printing to console.)

                System.out.println(username.getText() + " " + String.valueOf(password.getPassword()));

                //if (connectionMethodCombobox.getSelectedIndex() == 0) {
                    myClientReader = new Pop3WebSocketsImplementation(myServer, username.getText(), String.valueOf(password.getPassword()));
                    myClientSender = new SmtpWebSocketsImplementation(myServer, username.getText(), String.valueOf(password.getPassword()));
                //}
                //else {
                    //myClientReader = new Pop3JavaMailImplementation(myServer, username.getText(), String.valueOf(password.getPassword()));
                    //myClientSender = new SmtpJavaMailImplementation(myServer, username.getText(), String.valueOf(password.getPassword()));
                //}

                MainWindow main = new MainWindow(myClientReader, myClientSender);
                this.dispose();

            }
            else {
                //Error messages are shown by the method areInputsValid(). No need to do anything here.
                //JOptionPane.showMessageDialog(this, "Please make sure that your inputs are valid.", "Invalid inputs.", JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (e.getSource() == cancelButton) {
            int selection = JOptionPane.showConfirmDialog(this, "Are you sure that you want to quit?", "Exit confirmation.", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (selection == 0) {
                this.dispose();
            }
        }
    }

    private boolean inputsAreValid() {
        //TODO

        try {
            parsedPop3Port = Integer.parseInt(pop3ServerPort.getText());
            parsedSmtpPort = Integer.parseInt(smtpServerPort.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,"Invalid port number(s). Please check your inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println(pop3ServerPort.getText() + " " + smtpServerPort.getText());
            return false;
        }

        if (username.getText().trim().equals("")
                || password.getPassword().toString().trim().equals("")
                || mailAddress.getText().trim().equals("")
                || realName.getText().trim().equals("")
                || pop3ServerAddress.getText().trim().equals("")
                || pop3ServerPort.getText().trim().equals("")
                || smtpServerAddress.getText().trim().equals("")
                || smtpServerPort.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this,"Please make sure that you fill out all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
