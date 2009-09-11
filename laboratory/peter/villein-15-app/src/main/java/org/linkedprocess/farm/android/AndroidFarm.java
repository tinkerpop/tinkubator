package org.linkedprocess.farm.android;

import org.linkedprocess.LopXmppException;
import org.linkedprocess.farm.Farm;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.farm.os.errors.UnsupportedScriptEngineException;
import org.linkedprocess.farm.os.errors.VmAlreadyExistsException;
import org.linkedprocess.farm.os.errors.VmSchedulerIsFullException;
import org.linkedprocess.villein.android.R;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class AndroidFarm extends Farm {
	private static final int HELLO_ID = 1;
	private Handler handler;
	private final Activity activity;

	public AndroidFarm(Activity activity, Handler handler, String server,
			int port, String username, String password, String farmPassword)
			throws LopXmppException {
		super(server, port, username, password, farmPassword);
		this.activity = activity;
		this.handler = handler;

	}

	@Override
	public Vm spawnVm(final String spawningVilleinJid, final String vmSpecies)
			throws VmAlreadyExistsException, VmSchedulerIsFullException,
			UnsupportedScriptEngineException {
		notify(spawningVilleinJid, vmSpecies);
		return super.spawnVm(spawningVilleinJid, vmSpecies);
	}

	private void notify(final String spawningVilleinJid, final String vmSpecies) {
		handler.post(new Runnable() {

			public void run() {
				String ns = Context.NOTIFICATION_SERVICE;
				NotificationManager mNotificationManager = (NotificationManager) activity
						.getSystemService(ns);

				int icon = R.drawable.lop;
				CharSequence tickerText = "Farm spawn";
				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, tickerText,
						when);

				Context context = activity.getApplicationContext();
				CharSequence contentTitle = "LoPFarm";
				CharSequence contentText = spawningVilleinJid + "spawning " +vmSpecies;
				Intent notificationIntent = new Intent(activity,
						AndroidFarm.class);
				PendingIntent contentIntent = PendingIntent.getActivity(
						activity, 0, notificationIntent, 0);

				notification.setLatestEventInfo(context, contentTitle,
						contentText, contentIntent);

				mNotificationManager.notify(HELLO_ID, notification);

			}
		});
	}

}
