package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.structs.VmProxy;
import org.linkedprocess.os.VMBindings;

import java.util.Set;
import java.util.HashSet;

/**
 * User: marko
 * Date: Aug 5, 2009
 * Time: 12:49:24 AM
 */
public class PollBindingsPattern extends VilleinPattern implements Runnable {

    protected VmProxy vmStruct;
    protected VMBindings desiredBindings;
    protected long pollingSleepTime;
    protected BindingsChecker bindingsChecker;
    protected Set<PollBindingsHandler> pollBindingsHandlers = new HashSet<PollBindingsHandler>();

    public PollBindingsPattern(XmppVillein xmppVillein, VmProxy vmStruct, VMBindings desiredBindings, BindingsChecker bindingsChecker, long pollingSleepTime) {
        super(xmppVillein);
        this.vmStruct = vmStruct;
        this.desiredBindings = desiredBindings;
        this.bindingsChecker = bindingsChecker;
        this.pollingSleepTime = pollingSleepTime;
    }

    public void addPollBindingsHandler(PollBindingsHandler pollBindingsHandler) {
        this.pollBindingsHandlers.add(pollBindingsHandler);
    }

    private void pollingSleep() {
        if (this.pollingSleepTime > 0) {
            try {
                Thread.sleep(this.pollingSleepTime);
            } catch (InterruptedException e) {
                XmppVillein.LOGGER.warning(e.getMessage());
            }
        }
    }

    public void run() {
        boolean done = false;
        while(!done) {
            VMBindings vmBindings = vmStruct.getVmBindings();
            if(null != vmBindings) {
                if(this.bindingsChecker.areEquivalentBindings(vmBindings, desiredBindings)) {
                    for(PollBindingsHandler pollBindingsHandler : this.pollBindingsHandlers) {
                        pollBindingsHandler.handleBindingsAchieved();
                    }
                    done = true;
                }
            }
            this.pollingSleep();
        }
    }
}
