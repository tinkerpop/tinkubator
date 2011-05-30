package org.linkedprocess.villein.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

	private Context activity;
	public String username;
	public String password;
	public String server;
	public int port;

	public Prefs(Context activity) {
		this.activity = activity;
		refresh();
	}

	void refresh() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);
		username = prefs.getString(Villein.USERNAME, "linked.process.1");
		password = prefs.getString(Villein.PASSWORD, "linked12");
		server = prefs.getString(Villein.SERVER, "talk.l.google.com");
		port = Integer.parseInt(prefs.getString(Villein.PORT, "5222"));
	}

}
