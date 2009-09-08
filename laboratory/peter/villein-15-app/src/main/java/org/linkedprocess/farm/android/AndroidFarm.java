package org.linkedprocess.farm.android;

import org.linkedprocess.LopXmppException;
import org.linkedprocess.farm.Farm;

public class AndroidFarm {
	private Farm farm;

	public AndroidFarm() {
		try {
			farm = new Farm("talk.l.google.com", 5222, "linked.process.1", "linked12", null);
		} catch (LopXmppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
