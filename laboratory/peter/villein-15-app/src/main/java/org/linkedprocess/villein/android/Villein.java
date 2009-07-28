package org.linkedprocess.villein.android;

import java.io.IOException;
import java.util.Properties;

import org.jivesoftware.smack.XMPPException;
import org.linkedprocess.xmpp.villein.XmppVillein;

import android.app.Activity;
import android.os.Bundle;

public class Villein extends Activity {
    private static final String PROPERTIES = "defaults.properties";
	private Bundle savedInstanceState;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
			connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	private void connect() throws IOException {
		Properties props = new Properties();
		props.load(getAssets().open(PROPERTIES));
		String server = props.getProperty("server");
		int port = Integer.parseInt(props.getProperty("port"));
		String username = props.getProperty("username");
		String password = props.getProperty("password");
		try {
			XmppVillein villein = new XmppVillein(server, port, username, password);
			
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}