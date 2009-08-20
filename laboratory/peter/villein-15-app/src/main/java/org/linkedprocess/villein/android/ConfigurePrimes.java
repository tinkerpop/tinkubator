package org.linkedprocess.villein.android;

import java.io.IOException;
import java.io.InputStream;

import org.linkedprocess.smack.AndroidProviderManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfigurePrimes extends Activity {
	private static final String[] machines = { "1", "2", "3" };
	private static final String[] language = { "groovy", "ruby", "LUA" };
	private Button submitButton;
	private TextView script;
	private Spinner numberOfMachines;
	private Spinner language2;

	@Override
	protected void onCreate(Bundle context) {
		super.onCreate(context);
		setContentView(R.layout.config_primes);
		numberOfMachines = (Spinner) findViewById(R.id.numberofmachines);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, machines);
		numberOfMachines.setAdapter(adapter);
		language2 = (Spinner) findViewById(R.id.language);
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, ConfigurePrimes.language);
		language2.setAdapter(adapter2);
		submitButton = (Button) findViewById(R.id.submit);
		submitButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				findPrimes();
			}
		});
		InputStream is;
		try {
			is = getAssets().open("findPrimes.groovy");
			byte[] bytes = new byte[4000];
			is.read(bytes, 0, 4000);
			script = (TextView) findViewById(R.id.jobScript);
			script.setText(new String(bytes).trim());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void findPrimes() {
		try {
			AndroidProviderManager.init(this);

			String scriptString = script.getText().toString();
			Bundle extras = getIntent().getExtras();
			int nrOfFarms = Integer.parseInt((String) numberOfMachines
					.getSelectedItem());
			org.linkedprocess.demos.primes.PrimeFinderAsynchWithProgress.findAsynch(1, 10000, 1, 2, extras
					.getString(Villein.USERNAME), extras
					.getString(Villein.PASSWORD), extras
					.getString(Villein.SERVER), extras.getInt(Villein.PORT), 1.0, 500, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
