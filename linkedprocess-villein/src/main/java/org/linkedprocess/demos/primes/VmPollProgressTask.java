package org.linkedprocess.demos.primes;

import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.patterns.BindingsChecker;
import org.linkedprocess.xmpp.villein.patterns.PollBindingsPattern;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;

public class VmPollProgressTask implements Runnable {
	private final VmProxy vmProxy;
	private final long pollingInterval;
	private final Double meterMax;

	public VmPollProgressTask(final VmProxy vmProxy, Double meterMax2, long pollingInterval) {
		this.vmProxy = vmProxy;
		this.meterMax = meterMax2;
		this.pollingInterval = pollingInterval;
	}

	public void run() {
		final Object monitor = new Object();
		BindingsChecker bc = new BindingsChecker() {
			public boolean areEquivalent(VmBindings actualBindings,
					VmBindings desiredBindings) {
				TypedValue actualValue = actualBindings.getTyped("meter");
				TypedValue desiredValue = desiredBindings.getTyped("meter");
				if (actualValue != null) {
					Double actualDouble = Double
							.valueOf(actualValue.getValue());
					Double desiredDouble = Double.valueOf(desiredValue
							.getValue());
					System.out.println(vmProxy.getFullJid() + ": "
							+ actualDouble + " out of " + desiredDouble);
					return (actualDouble != null && desiredDouble != null && actualDouble >= desiredDouble);
				} else {
					return false;
				}
			}
		};
		Handler<VmBindings> resultHandler = new Handler<VmBindings>() {

			public void handle(VmBindings vmBindings) {
				System.out.println(vmProxy.getFullJid()
						+ ": progress meter value has been reached: "
						+ vmBindings);
				synchronized (monitor) {
					monitor.notify();
				}
				//System.exit(0);
			}
		};
		Handler<LopError> errorHandler = new Handler<LopError>() {
			public void handle(LopError lopError) {
				System.out.println("an error has occured: " + lopError);
				synchronized (monitor) {
					monitor.notify();
				}
				//	System.exit(1);
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
