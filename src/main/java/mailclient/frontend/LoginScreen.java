package mailclient.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends JFrame implements ActionListener {
    private JTextField username, mailAddress, realName, serverAddress, serverport;
    private JPasswordField password;
    private JCheckBox showPasswordCheckbox, encryptedServerCheckbox;
    private JButton cancelButton, loginButton;
    private JComboBox savedUsersCombobox, connectionProtocolCombobox;
    private JLabel passVisibleEmojiLabel, encryptionEmojiLabel;
    private ImageIcon titleIcon, passwordVisibleIcon, passwordInvisibleIcon, encryptionIcon, noencryptionIcon, cancelIcon, loginIcon;

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


        JPanel serverRelatedInputsPanel = new JPanel();
        serverRelatedInputsPanel.setBorder(BorderFactory.createTitledBorder("Mail server"));
        serverRelatedInputsPanel.setLayout(new GridLayout(3,3, 10, 5));

        serverRelatedInputsPanel.add(new JLabel("Server Address:"));
        serverRelatedInputsPanel.add(serverAddress = new JTextField());
        serverRelatedInputsPanel.add(new JLabel(""));

        serverRelatedInputsPanel.add(new JLabel("Server Port:"));
        serverRelatedInputsPanel.add(serverAddress = new JTextField());

        JPanel encryptionContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
        encryptedServerCheckbox = new JCheckBox("Encryption", false);
        encryptedServerCheckbox.addActionListener(this);  //for changing the emoji.
        encryptionIcon = new ImageIcon(getClass().getResource("/icons/lock.png"));
        noencryptionIcon = new ImageIcon(getClass().getResource("/icons/unlock.png"));
        encryptionEmojiLabel = new JLabel(noencryptionIcon);
        encryptionContainer.add(encryptionEmojiLabel);
        encryptionContainer.add(encryptedServerCheckbox);
        serverRelatedInputsPanel.add(encryptionContainer);


        serverRelatedInputsPanel.add(new JLabel("Connection protocol:"));
        serverRelatedInputsPanel.add(connectionProtocolCombobox = new JComboBox(new String[]{"POP3", "SMTP"}));
        serverRelatedInputsPanel.add(new JLabel(""));

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
        else if (e.getSource() == encryptedServerCheckbox) {
            JCheckBox source = (JCheckBox) e.getSource();
            encryptionEmojiLabel.setIcon( source.isSelected() ? encryptionIcon : noencryptionIcon );
        }
        else if (e.getSource() == loginButton) {
            if (!areInputsValid()) {
                JOptionPane.showMessageDialog(this, "Please make sure that your inputs are valid.", "Invalid inputs.", JOptionPane.ERROR_MESSAGE);
            }
            else {
                //TODO call main window and dispose of this one.
            }
        }
        else if (e.getSource() == cancelButton) {
            int selection = JOptionPane.showConfirmDialog(this, "Are you sure that you want to quit?", "Exit confirmation.", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (selection == 0) {
                this.dispose();
            }
        }
    }

    private boolean areInputsValid() {
        //TODO
        return false;
    }
}
