package org.linkedprocess.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
    protected JTextField farmPasswordField;
    protected JCheckBox rememberBox;
    protected Image backgroundImage;
    protected String BORDER_SPACE = "    ";
    protected String propertiesFile;
    protected final String QUIT = "quit";
    protected final String LOGIN = "login";

    public LoginArea(BorderLayout borderLayout) {
        super(borderLayout);
    }

    public void doRememberedProperties() {
        if (this.rememberBox.isSelected()) {
            Properties props = new Properties();
            props.put("username", this.usernameField.getText());
            props.put("password", this.passwordField.getText());
            props.put("server", this.serverField.getText());
            props.put("port", this.portField.getText());
            if(this.farmPasswordField != null)
                props.put("farmPassword", this.farmPasswordField.getText());

            try {
                props.store(new FileOutputStream(this.propertiesFile), "remembered properties.");
            } catch (IOException e) {
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
