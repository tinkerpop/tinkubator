package org.linkedprocess.villein.android;

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
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
				i.putExtra(PORT,Integer.parseInt(((EditText)findViewById(R.id.port)).getText().toString()));
				
				startActivity(i);
			}
		});
	}

}