package org.linkedprocess.villein.android;

import org.jivesoftware.smack.RosterEntry;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

public class VilleinOverview extends ListActivity {

	private Bundle bundle;
	private Button newJobButton;
	private ListView farmList;
	//private XmppVillein villein;
	private RosterEntry[] entries;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.bundle = savedInstanceState;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overview);
		//villein = VilleinService.villein;
		//Collection<RosterEntry> items = villein.getRoster().getEntries();
//		entries = new RosterEntry[items.size()];
//		items.toArray(entries);
//		setListAdapter(new IconicAdapter(this));
//		newJobButton = (Button) findViewById(R.id.newJob);
//		newJobButton.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				Intent i = new Intent(VilleinOverview.this, CreateJob.class);
//				startActivity(i);
//			}
//		});
//		
//		Roster roster = villein.getConnection().getRoster();
//		roster.setSubscriptionMode(SubscriptionMode.accept_all);

	}

//	class IconicAdapter extends ArrayAdapter {
//		Activity context;
//
//		IconicAdapter(Activity context) {
//			super(context, R.layout.row, entries);
//
//			this.context = context;
//		}
//		// adapter is a reference to your BaseAdapter or ListAdapter
//        private Runnable updateAdapter = new Runnable() {
//
//                public void run() {
//                        IconicAdapter.this.notifyDataSetChanged();
//                }
//
//        }; 
//		public View getView(int position, View convertView, ViewGroup parent) {
//			villein.getConnection().getRoster().addRosterListener(new RosterListener() {
//				// Ignored events public void entriesAdded(Collection<String> addresses) {}
//				public void entriesDeleted(Collection<String> addresses) {}
//				public void entriesUpdated(Collection<String> addresses) {}
//				public void presenceChanged(Presence presence) {
//					Log.i("Presence","Presence changed: " + presence.getFrom() + " " + presence);
//					runOnUiThread(updateAdapter);
//				}
//				public void entriesAdded(Collection<String> arg0) {
//					// TODO Auto-generated method stub
//					
//				}
//			});
//			View row = context.getLayoutInflater().inflate(R.layout.row,
//					parent, false);
//			TextView label = (TextView) row.findViewById(R.id.rowlabel);
//			RosterEntry rosterEntry = entries[position];
//			Presence presence = villein.getConnection().getRoster()
//			.getPresence(rosterEntry.getUser());
//
//			label.setText(rosterEntry.getUser());
//			ImageView icon = (ImageView) row.findViewById(R.id.rowicon);
//			Log.i(getClass().getName(), rosterEntry.getUser() + " Status: "
//					+ presence.toString());
//			if (presence.isAvailable() ) {
//				icon.setImageResource(android.R.drawable.presence_online);
//			} else {
//				icon.setImageResource(android.R.drawable.presence_offline);
//			}
//			return (row);
//		}
//	}
}
