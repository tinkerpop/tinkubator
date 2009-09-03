package org.linkedprocess.villein.android;

import java.io.IOException;
import java.util.Properties;

import org.linkedprocess.LinkedProcess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Villein extends Activity {
	private Bundle bundle;
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String SERVER = "server";
	public static final String PORT = "port";
	protected static final String SCRIPT = "script";
	protected static final String NUMBER_OF_VMS = "number_of_vms";
	protected static final String FARM_PASSWORD = "farm_password";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
				i.putExtra(USERNAME,((EditText)findViewById(R.id.username)).getText().toString());
				i.putExtra(PASSWORD,((EditText)findViewById(R.id.password)).getText().toString());
				i.putExtra(SERVER,((EditText)findViewById(R.id.server)).getText().toString());
				i.putExtra(FARM_PASSWORD,((EditText)findViewById(R.id.farm_password)).getText().toString());
				i.putExtra(PORT,Integer.parseInt(((EditText)findViewById(R.id.port)).getText().toString()));
				
				startActivity(i);
			}
		});
	}

}