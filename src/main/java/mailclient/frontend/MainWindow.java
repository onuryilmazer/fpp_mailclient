package mailclient.frontend;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.Main;
import mailclient.backend.Mail;
import mailclient.backend.Pop3Client;
import mailclient.backend.SmtpClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.*;

public class MainWindow extends JFrame implements ActionListener, MouseListener {
    private Pop3Client mailReader;
    private SmtpClient mailSender;
    private JToolBar controlsToolbar;
    private JButton newMailButton, syncMailsButton, optionsButton, logoutButton;
    private JTable emailsTable;
    private ImageIcon titleIcon, newMailIcon, syncMailsIcon, optionsIcon, logoutIcon;

    private String[] tableHeaders;
    private String mailRootFolder = "applicationData/savedMails/";
    private ArrayList<Mail> mailsFromDisk = new ArrayList<>();

    public static void main(String[] args) {
        //for testing purposes.
        new MainWindow(null, null);
    }

    MainWindow(Pop3Client mailReader, SmtpClient mailSender) {
        this.mailReader = mailReader;
        this.mailSender = mailSender;

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

        tableHeaders = new String[]{"UID", "ID", "Subject", "Sender", "Date", "Read"};
        DefaultTableModel model = new DefaultTableModel( tableHeaders, 0 ){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        emailsTable = new JTable(model);
        JScrollPane emailTableContainer = new JScrollPane(emailsTable);
        emailsTable.addMouseListener(this);

        this.add(controlsToolbar, BorderLayout.NORTH);
        this.add(emailTableContainer, BorderLayout.CENTER);

        this.pack();                       //Resizes the frame automatically.
        this.setLocationRelativeTo(null);  //Sets the initial position of the frame as the center of the screen.
        this.setVisible(true);

        loadSavedMails();

        for (int i = mailsFromDisk.size()-1; i >= 0; i--) {
            if (mailsFromDisk.get(i) == null) continue;
            model.addRow(new String[]{mailsFromDisk.get(i).mailUIDL, String.valueOf(mailsFromDisk.get(i).mailNr), mailsFromDisk.get(i).subject, mailsFromDisk.get(i).from, mailsFromDisk.get(i).date, String.valueOf(mailsFromDisk.get(i).read)});
        }

        TreeMap<Integer, String> mailUIDLs = mailReader.fetchMailUIDLs();
        for (Map.Entry<Integer, String> mapEntry : mailUIDLs.entrySet()) {
            if (searchDownloadedMails(mapEntry.getValue())) {
                System.out.println("Mail " + mapEntry.getKey() + " is already downloaded. Skipping.");
                continue;
            }
            else {
                Mail mailToDownload = new Mail();
                mailToDownload.mailUIDL = mapEntry.getValue();
                mailToDownload.mailNr = mapEntry.getKey();
                mailToDownload = mailReader.fetchMailEnvelope(mailToDownload);  //download
                mailToDownload = mailReader.fetchMailBody(mailToDownload);
                saveMail(mailToDownload);                       //save, display.
                System.out.println("Downloaded mail " + mapEntry.getKey() + ".");
                model.addRow(new String[]{mailToDownload.mailUIDL, String.valueOf(mailToDownload.mailNr), mailToDownload.subject, mailToDownload.from, mailToDownload.date, String.valueOf(mailToDownload.read)});
            }
        }

    }

    private boolean searchDownloadedMails(String mailUIDL) {
        for (Mail dmail : mailsFromDisk) {
            if (Objects.equals(dmail.mailUIDL, mailUIDL)) {
                return true;
            }
        }
        return false;
    }

    public void deleteMail(int mailID) {
        //TODO
    }

    private void saveMail(Mail myMail) {
        try {
            File parentFolder = new File( mailRootFolder );
            if (!parentFolder.exists()) {
                System.out.println("Created the folder: " + parentFolder.mkdirs());
            }

            FileOutputStream file = new FileOutputStream(mailRootFolder + myMail.mailUIDL + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(myMail);
            out.close();

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Can't save mail: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Can't save file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void loadSavedMails() {
        File downloadedMailsFolder = new File(mailRootFolder);
        File[] listOfMails = downloadedMailsFolder.listFiles();
        if (listOfMails == null) {
            return;
        }

        for (int i = 0; i < listOfMails.length; i++) {
            if (listOfMails[i].isFile()) {
                try {
                    FileInputStream readFile = new FileInputStream(listOfMails[i]);
                    ObjectInputStream readObject = new ObjectInputStream(readFile);

                    Mail m = (Mail) readObject.readObject();
                    mailsFromDisk.add(m);
                    System.out.println("Loaded mail UID" + m.mailUIDL + " ID " + m.mailNr);

                    readFile.close();
                    readObject.close();

                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(this, "Can't read file: " + e.getMessage());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Can't read object: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(this, "Can't read object, unexpected class: " + e.getMessage());
                }
            }
        }

        return;
    }

    private boolean mailIsDownloaded(Mail mail) {
        File downloadedMailsFolder = new File(mailRootFolder);
        File[] listOfMails = downloadedMailsFolder.listFiles();
        if (listOfMails == null) {
            return false;
        }

        for (int i = 0; i < listOfMails.length; i++) {
            if (listOfMails[i].getName().equals(mail.mailUIDL + ".ser")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 2) {     // to detect doble click events
            System.out.println("mouse evetn");
            int row = emailsTable.getSelectedRow(); // select a row
            //int column = emailsTable.getSelectedColumn(); // select a column
            String mailUID = String.valueOf(emailsTable.getValueAt(row, 0));
            Mail selectedMail = new Mail();

            for (Mail currentMail : mailsFromDisk) {
                if (currentMail.mailUIDL == mailUID) {
                    selectedMail = currentMail;
                }
            }
            new MailReader(this, selectedMail);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
