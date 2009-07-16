package org.linkedprocess.gui;

import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.xmpp.villein.XmppVillein;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

import org.jivesoftware.smack.XMPPException;

/**
 * User: marko
 * Date: Jul 6, 2009
 * Time: 10:32:32 AM
 */
public abstract class LoginArea extends JPanel implements ActionListener {

    protected JTextField usernameField;
    protected JTextField passwordField;
    protected JTextField serverField;
    protected JTextField portField;
    protected JLabel statusLabel;
    protected JCheckBox rememberBox;
    protected Image backgroundImage;
    protected String BORDER_SPACE = "    ";
    protected String propertiesFile;

    public LoginArea(Image backgroundImage, String propertiesFile) {
        super(new BorderLayout());
        this.backgroundImage = backgroundImage;
        this.propertiesFile = propertiesFile;
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
        } catch(Exception e) {
            System.out.println("Could not load " + this.propertiesFile + " file.");

        }

        JPanel mainPanel = new JPanel(new GridLayout(6,2,0,0));

        mainPanel.add(new JLabel());
        this.statusLabel = new JLabel();
        this.statusLabel.setForeground(Color.RED);
        mainPanel.add(this.statusLabel);

        mainPanel.add(new JLabel(BORDER_SPACE + "username:"));
        mainPanel.add(usernameField);

        mainPanel.add(new JLabel(BORDER_SPACE + "password:"));
        mainPanel.add(passwordField);

        mainPanel.add(new JLabel(BORDER_SPACE + "server:"));
        mainPanel.add(serverField);

        mainPanel.add(new JLabel(BORDER_SPACE + "port:"));
        mainPanel.add(portField);

        mainPanel.add(new JLabel());
        mainPanel.add(this.rememberBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loginButton = new JButton("login");
        JButton quitButton = new JButton ("quit");

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

    public void doRememberedProperties() {
         if(this.rememberBox.isSelected()) {
            Properties props = new Properties();
            props.put("username", this.usernameField.getText());
            props.put("password", this.passwordField.getText());
            props.put("server", this.serverField.getText());
            props.put("port", this.portField.getText());

            try {
                props.store(new FileOutputStream(this.propertiesFile), "remembered properties.");
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            new File(this.propertiesFile).delete();
        }
    }
    
    public void paintComponent(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, null);
        super.paintComponent(g);
    }
}
