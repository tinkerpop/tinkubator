package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.XMPPException;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.gui.LoginArea;
import org.linkedprocess.villein.LopVillein;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class VilleinLoginArea extends LoginArea {

    protected VilleinGui villeinGui;
    protected final static String PROPERTIES_FILE = "villein_gui.properties";

    public VilleinLoginArea(VilleinGui villeinGui) {
        super(new BorderLayout());
        this.backgroundImage = ImageHolder.cowBackground.getImage();
        this.propertiesFile = PROPERTIES_FILE;
        this.villeinGui = villeinGui;
        this.setOpaque(false);

        this.usernameField = new JTextField("", 15);
        this.passwordField = new JPasswordField("", 15);
        this.serverField = new JTextField("", 15);
        this.portField = new JTextField("5222", 15);
        this.rememberBox = new JCheckBox("remember");
        this.rememberBox.setSelected(true);

        try {
            Properties props = new Properties();
            props.load(new FileInputStream(this.propertiesFile));
            this.usernameField.setText(props.getProperty("username"));
            this.passwordField.setText(props.getProperty("password"));
            this.serverField.setText(props.getProperty("server"));
            this.portField.setText(props.getProperty("port"));
        } catch (Exception e) {
            System.out.println("Could not load " + this.propertiesFile + " file.");

        }

        JPanel mainPanel = new JPanel(new GridLayout(6, 2, 0, 0));

        mainPanel.add(new JLabel());
        mainPanel.add(new JLabel());

        mainPanel.add(new JLabel(BORDER_SPACE + "username:"));
        mainPanel.add(this.usernameField);

        mainPanel.add(new JLabel(BORDER_SPACE + "password:"));
        mainPanel.add(this.passwordField);

        mainPanel.add(new JLabel(BORDER_SPACE + "server:"));
        mainPanel.add(this.serverField);

        mainPanel.add(new JLabel(BORDER_SPACE + "port:"));
        mainPanel.add(this.portField);

        mainPanel.add(new JLabel());
        mainPanel.add(this.rememberBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loginButton = new JButton(LOGIN);
        JButton quitButton = new JButton(QUIT);

        buttonPanel.add(loginButton);
        buttonPanel.add(quitButton);
        JLabel copyright = new JLabel("developed by linkedprocess.org" + BORDER_SPACE + BORDER_SPACE);
        copyright.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
        buttonPanel.add(copyright);
        buttonPanel.add(new JLabel(ImageHolder.lopIcon));

        mainPanel.setOpaque(false);
        buttonPanel.setOpaque(false);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(this);
        quitButton.addActionListener(this);

        this.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));
    }

    public void actionPerformed(ActionEvent event) {

        this.doRememberedProperties();

        try {
            if (event.getActionCommand().equals(LOGIN)) {
                LopVillein villein = new LopVillein(serverField.getText(), new Integer(this.portField.getText()), this.usernameField.getText(), this.passwordField.getText());
                this.villeinGui.loadLopCloudArea(villein);
            } else if (event.getActionCommand().equals(QUIT)) {
                System.exit(0);
            }
        } catch (XMPPException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "could not login", JOptionPane.ERROR_MESSAGE);
        }
    }
}
