/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.proxies;

import org.jdom.Document;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Dispatcher;
import org.linkedprocess.xmpp.villein.Handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A FarmProxy is a proxy to a farm. A farm is an XMPP client as is identified by a fully-qualified JID.
 * The bare JID component of the farm is the farm's countryside. A farm can contain any number of virtual machines.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class FarmProxy extends Proxy {

    protected Map<String, VmProxy> vmProxies = new HashMap<String, VmProxy>();
    protected String farmPassword;


    public FarmProxy(final String fullJid, final Dispatcher dispatcher) {
        super(fullJid, dispatcher);
    }

    public FarmProxy(final String fullJid, final Dispatcher dispatcher, final Document discoInfoDocument) {
        super(fullJid, dispatcher, discoInfoDocument);
    }

    public VmProxy getVmProxy(String vmJid) {
        return vmProxies.get(vmJid);
    }

    public void addVmProxy(VmProxy vmProxy) {
        this.vmProxies.put(vmProxy.getFullJid(), vmProxy);
    }

    public Collection<VmProxy> getVmProxies() {
        return this.vmProxies.values();
    }

    public void removeVmProxy(String vmJid) {
        this.vmProxies.remove(vmJid);
    }

    public Collection<String> getSupportedVmSpecies() {
        Field field = this.getField(LinkedProcess.VM_SPECIES_ATTRIBUTE);
        if (null != field)
            return field.getValues();
        else
            return new HashSet<String>();
    }

    public String getFarmPassword() {
        return this.farmPassword;
    }

    public void setFarmPassword(String farmPassword) {
        this.farmPassword = farmPassword;
    }

    public boolean requiresPassword() {
        Field field = this.getField(LinkedProcess.FARM_PASSWORD_REQUIRED);
        if (null != field) {
            return field.getBooleanValue();
        } else {
            return false;
        }
    }

    public long getVmTimeToLive() {
        Field field = this.getField(LinkedProcess.VM_TIME_TO_LIVE);
        if (null != field) {
            return field.getLongValue();
        } else {
            return -1;
        }
    }

    public long getJobTimeout() {
        Field field = this.getField(LinkedProcess.JOB_TIMEOUT);
        if (null != field) {
            return field.getLongValue();
        } else {
            return -1;
        }
    }

    public boolean supportsSpecies(String vmSpecies) {
        return this.getSupportedVmSpecies().contains(vmSpecies);
    }

    public void spawnVm(final String vmSpecies, final Handler<VmProxy> resultHandler, final Handler<LopError> errorHandler) {
        this.dispatcher.getSpawnVmCommand().send(this, vmSpecies, resultHandler, errorHandler);
    }
}
