package org.linkedprocess.villein.android;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Villein extends Activity implements ConnectionListener {
	private Bundle bundle;
	private Button connectButton;
	private TextView status;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.bundle = savedInstanceState;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		status = (TextView) findViewById(R.id.status);
		connectButton = (Button) findViewById(R.id.Connect);
		connectButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					connect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private void connect() throws IOException {
		// setup and start MyService
		{
			VilleinService.setConnectionListener(this);
			Intent svc = new Intent(this, VilleinService.class);
			startService(svc);
			
		}

	}

	public void connected() {

		//status.setText("conencted");
		Intent i = new Intent(this, VilleinOverview.class);
		startActivity(i);
		
	}
}