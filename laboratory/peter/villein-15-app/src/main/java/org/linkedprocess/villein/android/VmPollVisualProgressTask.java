package org.linkedprocess.villein.android;

import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.patterns.BindingsChecker;
import org.linkedprocess.villein.patterns.PollBindingsPattern;
import org.linkedprocess.villein.proxies.VmProxy;

import android.widget.ProgressBar;

public class VmPollVisualProgressTask implements Runnable {
	private final VmProxy vmProxy;
	private final long pollingInterval;
	private final Double meterMax;
	private final android.os.Handler feedbackHandler;
	private final ProgressBar progressBar;

	public VmPollVisualProgressTask(final VmProxy vmProxy, Double meterMax2, long pollingInterval, final android.os.Handler feedbackHandler, ProgressBar progressBar) {
		this.vmProxy = vmProxy;
		this.meterMax = meterMax2;
		this.pollingInterval = pollingInterval;
		this.feedbackHandler = feedbackHandler;
		this.progressBar = progressBar;
	}

	public void run() {
		final Object monitor = new Object();
		BindingsChecker bc = new BindingsChecker() {
			public boolean areEquivalent(VmBindings actualBindings,
					VmBindings desiredBindings) {
				TypedValue actualValue = actualBindings.getTyped("meter");
				TypedValue desiredValue = desiredBindings.getTyped("meter");
				if (actualValue != null) {
					final Double actualDouble = Double
							.valueOf(actualValue.getValue());
					Double desiredDouble = Double.valueOf(desiredValue
							.getValue());
					System.out.println(vmProxy.getJid() + ": "
							+ actualDouble + " out of " + desiredDouble);
					feedbackHandler.post(new Runnable() {
						
						public void run() {
							progressBar.setProgress((int)(actualDouble*100));
						}
					});
					return (actualDouble != null && desiredDouble != null && actualDouble >= desiredDouble);
				} else {
					return false;
				}
			}
		};
		Handler<VmBindings> resultHandler = new Handler<VmBindings>() {

			public void handle(VmBindings vmBindings) {
				System.out.println(vmProxy.getJid()
						+ ": progress meter value has been reached: "
						+ vmBindings);
				synchronized (monitor) {
					monitor.notify();
				}
				//System.exit(0);
			}
		};
		Handler<org.linkedprocess.Error> errorHandler = new Handler<org.linkedprocess.Error>() {

			public void handle(org.linkedprocess.Error lopError) {
				System.out.println("an error has occured: " + lopError);
				synchronized (monitor) {
					monitor.notify();
				}
				
			}
		};
		PollBindingsPattern pb = new PollBindingsPattern();
		VmBindings desiredBindings = new VmBindings();
		try {
			desiredBindings.putTyped("meter", new TypedValue(
					VmBindings.XMLSchemaDatatype.DOUBLE, "" + meterMax));
			pb.startPattern(vmProxy, desiredBindings, bc, resultHandler,
					errorHandler, pollingInterval);
			synchronized (monitor) {
				monitor.wait();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
