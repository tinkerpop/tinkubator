package org.linkedprocess.villein.android;

import android.app.Activity;
import android.os.Bundle;

public class CreateJob extends Activity {
	private Bundle bundle;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        this.bundle = savedInstanceState;
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createjob);
	}
}
