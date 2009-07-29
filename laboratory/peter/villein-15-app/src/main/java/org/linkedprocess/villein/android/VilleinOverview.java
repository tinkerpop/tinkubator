package org.linkedprocess.villein.android;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;
import org.linkedprocess.xmpp.villein.XmppVillein;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class VilleinOverview extends ListActivity {

	private Bundle bundle;
	private Button newJobButton;
	private ListView farmList;
	private XmppVillein villein;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        this.bundle = savedInstanceState;
		super.onCreate(savedInstanceState);
        setContentView(R.layout.overview);
        villein = VilleinService.villein;
        Collection<RosterEntry> items = villein.getRoster().getEntries();
        RosterEntry[] entries = new RosterEntry[items.size()];
        items.toArray(entries);
        setListAdapter(new ArrayAdapter(this,R.layout.row, R.id.rowlabel, entries));  
        newJobButton = (Button) findViewById(R.id.newJob);
        newJobButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(VilleinOverview.this, CreateJob.class);
				startActivity(i);
			}
		});
    }
}
