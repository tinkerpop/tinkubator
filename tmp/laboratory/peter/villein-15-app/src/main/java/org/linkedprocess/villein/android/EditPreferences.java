package org.linkedprocess.villein.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class EditPreferences extends PreferenceActivity {

	public static final String PREFS = "prefs";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onStop() {
		super.onStop();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(Villein.USERNAME,
				((EditTextPreference) findPreference(Villein.USERNAME))
						.getText());
		editor.putString(Villein.PASSWORD,
				((EditTextPreference) findPreference(Villein.PASSWORD))
						.getText());
		editor
				.putString(Villein.SERVER,
						((EditTextPreference) findPreference(Villein.SERVER))
								.getText());
		editor.putString(Villein.PORT, ((EditTextPreference) findPreference(Villein.PORT))
						.getText());
		
		editor.commit();
		Toast.makeText(this, "preferences saved", Toast.LENGTH_SHORT).show();
		Villein.prefs.refresh();
		
	}
}
