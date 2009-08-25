package org.linkedprocess.demos.primes;

import org.linkedprocess.farm.os.TypedValue;
import org.linkedprocess.farm.os.VmBindings;
import org.linkedprocess.Error;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.patterns.BindingsChecker;
import org.linkedprocess.villein.patterns.PollBindingsPattern;
import org.linkedprocess.villein.proxies.VmProxy;

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
					System.out.println(vmProxy.getVmId() + ": "
							+ actualDouble + " out of " + desiredDouble);
					return (actualDouble != null && desiredDouble != null && actualDouble >= desiredDouble);
				} else {
					return false;
				}
			}
		};
		Handler<VmBindings> resultHandler = new Handler<VmBindings>() {

			public void handle(VmBindings vmBindings) {
				System.out.println(vmProxy.getVmId()
						+ ": progress meter value has been reached: "
						+ vmBindings);
				synchronized (monitor) {
					monitor.notify();
				}
				//System.exit(0);
			}
		};
		Handler<Error> errorHandler = new Handler<Error>() {
			public void handle(Error lopError) {
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
