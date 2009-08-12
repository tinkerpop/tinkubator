package org.linkedprocess.gui.farm;

import org.jivesoftware.smack.XMPPException;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.gui.LoginArea;
import org.linkedprocess.xmpp.farm.XmppFarm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Properties;
import java.io.FileInputStream;

/**
 * User: marko
 * Date: Jul 6, 2009
 * Time: 10:32:32 AM
 */
public class FarmLoginArea extends LoginArea {

    protected FarmGui farmGui;
    protected final static String PROPERTIES_FILE = "farm_manager.properties";

    public FarmLoginArea(FarmGui farmGui) {
        super(new BorderLayout());
        this.backgroundImage = ImageHolder.farmBackground.getImage();
        this.propertiesFile = PROPERTIES_FILE;
        this.farmGui = farmGui;

        this.setOpaque(false);

        this.usernameField = new JTextField("", 15);
        this.passwordField = new JPasswordField("", 15);
        this.serverField = new JTextField("", 15);
        this.portField = new JTextField("5222", 15);
        this.farmPasswordField = new JPasswordField("", 15);

        this.rememberBox = new JCheckBox("remember");
        this.rememberBox.setSelected(true);

        try {
            Properties props = new Properties();
            props.load(new FileInputStream(this.propertiesFile));
            this.usernameField.setText(props.getProperty("username"));
            this.passwordField.setText(props.getProperty("password"));
            this.serverField.setText(props.getProperty("server"));
            this.portField.setText(props.getProperty("port"));
            this.farmPasswordField.setText(props.getProperty("farmPassword"));
        } catch (Exception e) {
            System.out.println("Could not load " + this.propertiesFile + " file.");

        }

        JPanel mainPanel = new JPanel(new GridLayout(7, 2, 0, 0));

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

        mainPanel.add(new JLabel(BORDER_SPACE + "farm password (optional):"));
        mainPanel.add(this.farmPasswordField);

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
                String farmPassword = this.farmPasswordField.getText().trim();
                if(farmPassword.equals(""))
                    farmPassword = null;
                XmppFarm farm = new XmppFarm(serverField.getText(), new Integer(this.portField.getText()), this.usernameField.getText(), this.passwordField.getText(), farmPassword);
                this.farmGui.loadMainFrame(farm);
            } else if (event.getActionCommand().equals(QUIT)) {
                System.exit(0);
            }
        } catch (XMPPException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "could not login", JOptionPane.ERROR_MESSAGE);
        }
    }
}
