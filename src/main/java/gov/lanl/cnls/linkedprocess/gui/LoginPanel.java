package gov.lanl.cnls.linkedprocess.gui;

import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.jivesoftware.smack.XMPPException;

/**
 * User: marko
 * Date: Jul 6, 2009
 * Time: 10:32:32 AM
 */
public class LoginPanel extends JPanel implements ActionListener {

    protected FarmGui farmGui;
    protected JTextField usernameField;
    protected JTextField passwordField;
    protected JTextField serverField;
    protected JTextField portField;
    protected JLabel statusLabel;
    protected Image backgroundImage;

    public LoginPanel(FarmGui farmGui) {
        super(new BorderLayout());
        this.farmGui = farmGui;
        this.backgroundImage = new ImageIcon(FarmGui.class.getResource("farm-background.png")).getImage();
        this.setOpaque(false);


        JPanel mainPanel = new JPanel(new GridLayout(4,2,0,0));

        this.usernameField = new JTextField("linked.process.1", 15);
        this.passwordField = new JPasswordField("linked12", 15);
        this.serverField = new JTextField("xmpp.linkedprocess.org", 15);
        this.portField = new JTextField("5222", 15);

        mainPanel.add(new JLabel("username:"));
        mainPanel.add(usernameField);

        mainPanel.add(new JLabel("password:"));
        mainPanel.add(passwordField);

        mainPanel.add(new JLabel("server:"));
        mainPanel.add(serverField);

        mainPanel.add(new JLabel("port:"));
        mainPanel.add(portField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loginButton = new JButton("login");
        this.statusLabel = new JLabel();
        buttonPanel.add(loginButton);
        buttonPanel.add(statusLabel);

        mainPanel.setOpaque(false);
        buttonPanel.setOpaque(false);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(this);

        Dimension size = new Dimension(backgroundImage.getWidth(null), backgroundImage.getHeight(null));
        //setPreferredSize(size);
        //setMinimumSize(size);
        //setMaximumSize(size);
        //setSize(size);
        //setLayout(null);

    }

    public void actionPerformed(ActionEvent event) {
        try {
            XmppFarm farm = new XmppFarm(serverField.getText(), new Integer(this.portField.getText()), this.usernameField.getText(), this.passwordField.getText());
            this.farmGui.loadMainFrame(farm);
        } catch(XMPPException e) {
            this.statusLabel.setText("Login information is incorrect.");    
        }


    }

    public void paintComponent(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, null);
        super.paintComponent(g);
    }



}
