package org.linkedprocess.villein.android;

import java.io.IOException;
import java.util.Properties;

import org.jivesoftware.smack.XMPPException;
import org.linkedprocess.xmpp.villein.XmppVillein;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class VilleinService extends Service {

	private static final String PROPERTIES = "defaults.properties";
	public static XmppVillein villein;
	private static ConnectionListener connectionListener;

	// XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	// lifecycle methods
	// XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

	/** not using ipc... dont care about this method */
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// init the service here
		_startService();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		_shutdownService();

	}

	// XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	// service business logic
	// XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	private void _startService() {
		Properties props = new Properties();
		try {
			props.load(getAssets().open(PROPERTIES));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String server = props.getProperty("server");
		int port = Integer.parseInt(props.getProperty("port"));
		String username = props.getProperty("username");
		String password = props.getProperty("password");
		try {
			villein = new XmppVillein(server, port, username, password);
			if (villein.getConnection().isConnected()) {
				connectionListener.connected();
			} else {
				
			}
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.i(getClass().getSimpleName(), "Villein service started!!!");
	}

	private void _shutdownService() {
		Log.i(getClass().getSimpleName(), "Villein service stopped!!!");
	}

	public static void setConnectionListener(ConnectionListener listener) {
		connectionListener = listener;
		
	}
}
