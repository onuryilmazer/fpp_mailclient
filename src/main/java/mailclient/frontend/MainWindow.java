package mailclient.frontend;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.Main;
import mailclient.backend.Mail;
import mailclient.backend.MailServer;
import mailclient.backend.Pop3Client;
import mailclient.backend.SmtpClient;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
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
    private DefaultTableModel model;
    private ImageIcon titleIcon, newMailIcon, syncMailsIcon, optionsIcon, logoutIcon;

    private String[] tableHeaders;
    private String mailRootFolder = "applicationData/savedMails/";
    private ArrayList<Mail> mailsFromDisk = new ArrayList<>();
    private Font tableFont = new Font("Serif", Font.PLAIN, 20);
    private int mailsToDelete = 0;

    public static void main(String[] args) {
        //for testing purposes.
        new MainWindow(null, null, null);
    }

    MainWindow(Pop3Client mailReader, SmtpClient mailSender, String username) {
        mailRootFolder += username + "/";
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
        model = new DefaultTableModel( tableHeaders, 0 ){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        emailsTable = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                String status = (String)getValueAt(row, 5);
                if ("true".equals(status)) {
                    c.setBackground(Color.LIGHT_GRAY);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(super.getBackground());
                    c.setForeground(super.getForeground());
                }
                return c;
            }
        };

        JScrollPane emailTableContainer = new JScrollPane(emailsTable);
        emailsTable.setFont(tableFont);
        emailsTable.setFocusable(false);
        emailsTable.setRowHeight(30);
        emailsTable.setAutoCreateRowSorter(true);
        emailsTable.getRowSorter().toggleSortOrder(1);
        emailsTable.getRowSorter().toggleSortOrder(1); //we call it twice so it sorts descending
        emailsTable.addMouseListener(this);



        TableColumnModel columnModel = emailsTable.getColumnModel();
        columnModel.getColumn(0).setMinWidth(150);
        columnModel.getColumn(0).setMaxWidth(150);
        columnModel.getColumn(1).setMinWidth(70);
        columnModel.getColumn(1).setMaxWidth(70);
        columnModel.getColumn(2).setMinWidth(350);
        columnModel.getColumn(3).setMinWidth(200);
        columnModel.getColumn(4).setMinWidth(160);
        columnModel.getColumn(4).setMaxWidth(160);
        columnModel.getColumn(5).setMinWidth(70);
        columnModel.getColumn(5).setMaxWidth(70);

        this.add(controlsToolbar, BorderLayout.NORTH);
        this.add(emailTableContainer, BorderLayout.CENTER);

        this.setMinimumSize(new Dimension(1000, 750));
        //this.pack();                       //Resizes the frame automatically.
        this.setLocationRelativeTo(null);  //Sets the initial position of the frame as the center of the screen.
        this.setVisible(true);

        syncMails();

        this.pack();

    }

    private void syncMails() {
        model.getDataVector().removeAllElements();
        model.fireTableDataChanged();
        mailsFromDisk = new ArrayList<>();
        loadSavedMails();

        for (int i = mailsFromDisk.size()-1; i >= 0; i--) {
            if (mailsFromDisk.get(i) == null) continue;
            model.addRow(new Object[]{mailsFromDisk.get(i).mailUIDL, mailsFromDisk.get(i).mailNr, mailsFromDisk.get(i).subject, mailsFromDisk.get(i).from, mailsFromDisk.get(i).date, String.valueOf(mailsFromDisk.get(i).read)});
        }

        int numberOfNewMails = 0;

        TreeMap<Integer, String> mailUIDLs = mailReader.fetchMailUIDLs();
        for (Map.Entry<Integer, String> mapEntry : mailUIDLs.entrySet()) {
            if (searchDownloadedMails(mapEntry.getValue())) {
                System.out.println("Mail " + mapEntry.getKey() + " is already downloaded. Skipping.");
                continue;
            }
            else {
                numberOfNewMails++;
                Mail mailToDownload = new Mail();
                mailToDownload.mailUIDL = mapEntry.getValue();
                mailToDownload.mailNr = mapEntry.getKey();
                mailToDownload = mailReader.fetchMailEnvelope(mailToDownload);  //download
                mailToDownload = mailReader.fetchMailBody(mailToDownload);
                saveMail(mailToDownload);                       //save, display.
                mailsFromDisk.add(mailToDownload);
                System.out.println("Downloaded mail " + mapEntry.getKey() + ".");
                model.addRow(new Object[]{mailToDownload.mailUIDL, mailToDownload.mailNr, mailToDownload.subject, mailToDownload.from, mailToDownload.date, String.valueOf(mailToDownload.read)});
            }
        }

        if (numberOfNewMails == 0) {
            JOptionPane.showMessageDialog(this, "You have no new mails", "Mailbox up-to-date!", JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(this, "Downloaded " + numberOfNewMails + " new mails from the server.", "Mailbox updated!", JOptionPane.INFORMATION_MESSAGE);
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

    public void deleteMail(int mailID, String mailUIDL) {
        mailReader.markMailForDeletionFromServer(mailID);

        File mailFile = new File(mailRootFolder + mailUIDL + ".ser");
        if (!mailFile.delete()) {
            JOptionPane.showMessageDialog(this, "Error: Couldn't delete mail from disk.");
        }

        mailsToDelete++;
    }

    public boolean sendMail(Mail mail) {
        return mailSender.sendMail(mail.from, new String[]{mail.to}, mail.subject, mail.mailBody);
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
        if (e.getSource() == optionsButton) {
            new Options(this, tableFont);
        }
        else if (e.getSource() == logoutButton) {
            this.dispose();
        }
        else if (e.getSource() == syncMailsButton) {
            try {
                mailReader.closeConnection();
                mailReader.reconnect(1);
                if (mailsToDelete > 0 ) {
                    JOptionPane.showMessageDialog(this, mailsToDelete + " mails were deleted from the server.", "Mails deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Can't reconnect.");
            }
            syncMails();
        }
        else if (e.getSource() == newMailButton) {
            new MailSender(this, tableFont);
        }
    }

    public void updateTableFont(Font font) {
        tableFont = font;
        emailsTable.setFont(tableFont);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 2 && me.getSource() == emailsTable) {     // to detect doble click events
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
            selectedMail.read = true;
            emailsTable.setValueAt("true" ,row, 5);
            emailsTable.repaint();
            saveMail(selectedMail);
            new MailReader(this, selectedMail, tableFont);
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
