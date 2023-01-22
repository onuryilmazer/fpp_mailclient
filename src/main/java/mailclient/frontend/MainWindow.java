package mailclient.frontend;

import com.sun.tools.javac.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame implements ActionListener {
    private JToolBar controlsToolbar;
    private JButton newMailButton, syncMailsButton, optionsButton, logoutButton;
    private JTable emailsTable;
    private ImageIcon titleIcon, newMailIcon, syncMailsIcon, optionsIcon, logoutIcon;

    private String[] tableHeaders;
    private String[][] placeholderData;

    public static void main(String[] args) {
        //for testing purposes.
        new MainWindow();
    }

    MainWindow() {
        this.setTitle("My Mails");
        titleIcon = new ImageIcon(getClass().getResource("/icons/email.png"));
        this.setIconImage(titleIcon.getImage());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        controlsToolbar = new JToolBar();
        newMailButton = new JButton();
        newMailIcon = new ImageIcon(getClass().getResource("/icons/writing_hand.png"));
        newMailButton.setIcon(newMailIcon);
        newMailButton.addActionListener(this);
        newMailButton.setText("Send a mail");
        controlsToolbar.add(newMailButton);

        syncMailsButton = new JButton();
        syncMailsIcon = new ImageIcon(getClass().getResource("/icons/arrows_counterclockwise.png"));
        syncMailsButton.setIcon(syncMailsIcon);
        syncMailsButton.addActionListener(this);
        syncMailsButton.setText("Sync inbox");
        controlsToolbar.add(syncMailsButton);

        optionsButton = new JButton();
        optionsIcon = new ImageIcon(getClass().getResource("/icons/gear.png"));
        optionsButton.setIcon(optionsIcon);
        optionsButton.addActionListener(this);
        optionsButton.setText("Options");
        controlsToolbar.add(optionsButton);

        logoutButton = new JButton();
        logoutIcon = new ImageIcon(getClass().getResource("/icons/system-shutdown-icon.png"));
        logoutButton.setIcon(logoutIcon);
        logoutButton.addActionListener(this);
        logoutButton.setText("Log out");
        controlsToolbar.add(logoutButton);

        tableHeaders = new String[]{"Subject", "Sender", "Date", "Read"};
        placeholderData = placeholderDataBootloader();

        emailsTable = new JTable(placeholderData, tableHeaders);
        JScrollPane emailTableContainer = new JScrollPane(emailsTable);

        this.add(controlsToolbar, BorderLayout.NORTH);
        this.add(emailTableContainer, BorderLayout.CENTER);

        this.pack();                       //Resizes the frame automatically.
        this.setLocationRelativeTo(null);  //Sets the initial position of the frame as the center of the screen.
        this.setVisible(true);

    }

    private String[][] placeholderDataBootloader() {
        return new String[][]{
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"},
                {"Mail 1", "onur.yilmazer@example.com", "21.1.2023", "No"},
                {"Mail 2", "asd@qwe.c", "11.22.33", "Yes"}
        };
    }

    public void deleteMail(int mailID) {
        //TODO
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
