package mailclient.frontend;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class Options extends JFrame implements ChangeListener {
    private Font myFont;
    private JSlider fontSize;
    MainWindow parent;

    Options(MainWindow parent, Font myFont) {
        this.parent = parent;
        this.myFont = myFont;
        this.setTitle("Options");
        ImageIcon titleIcon = new ImageIcon(getClass().getResource("/icons/email.png"));
        this.setIconImage(titleIcon.getImage());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel settings = new JPanel();
        fontSize = new JSlider(15,30);
        fontSize.setMinorTickSpacing(1);
        fontSize.setMajorTickSpacing(3);
        fontSize.setPaintLabels(true);
        fontSize.addChangeListener(this);
        JLabel fontLabel = new JLabel("Font size of the mails screen: ");

        settings.add(fontSize);
        settings.add(fontLabel);
        this.add(settings, BorderLayout.CENTER);

        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        myFont = new Font("Serif", Font.PLAIN, fontSize.getValue());
        parent.updateTableFont(myFont);
    }
}
