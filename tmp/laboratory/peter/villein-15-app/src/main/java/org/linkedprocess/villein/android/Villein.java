package org.linkedprocess.villein.android;

import java.io.IOException;
import java.util.Properties;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopXmppException;
import org.linkedprocess.farm.android.AndroidFarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Villein extends Activity {
	private Bundle bundle;
	public static Prefs prefs;
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String SERVER = "server";
	public static final String PORT = "port";
	protected static final String SCRIPT = "script";
	protected static final String NUMBER_OF_VMS = "number_of_vms";
	protected static final String FARM_PASSWORD = "farm_password";
	private static final int EDIT_ID = 0;
	private static final int CLOSE_ID = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		prefs = new Prefs(this);
		Properties lopProps = new Properties();
		try {
			lopProps.load(getAssets().open("lop-default.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LinkedProcess.setConfiguration(lopProps);
		this.bundle = savedInstanceState;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// VilleinService.setConnectionListener(this);
		Button findViewById = (Button) findViewById(R.id.primefinder);
		findViewById.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(Villein.this, ConfigurePrimes.class);
				startActivity(i);
			}
		});
		Button startFarm = (Button) findViewById(R.id.start_farm);
		startFarm.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					AndroidFarm farm = new AndroidFarm(Villein.this,
							new Handler(), prefs.server, prefs.port,
							prefs.username, prefs.password, null);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LopXmppException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_ID:
			startActivity(new Intent(this, EditPreferences.class));
			return true;
		case CLOSE_ID:
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CLOSE_ID, 0, "Quit");
		menu.add(0, EDIT_ID, 0, "Preferences");
		return true;
	}

}