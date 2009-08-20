/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.jdom.Document;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopError;
import org.linkedprocess.villein.Dispatcher;
import org.linkedprocess.villein.Handler;

import java.util.*;

/**
 * A FarmProxy is a proxy to a farm. A farm is an XMPP client as is identified by a fully-qualified JID.
 * The bare JID component of the farm is the farm's countryside. A farm can contain any number of virtual machines.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class FarmProxy extends Proxy {

    /**
     * The virtual machine proxies maintained by this farm proxy.
     */
    protected Map<String, VmProxy> vmProxies = new HashMap<String, VmProxy>();
    /**
     * The password of the farm.
     */
    protected String farmPassword;


    /**
     * Create a new farm proxy with a provided fully-qualified jid.
     *
     * @param fullJid    the fully-qualified farm jid
     * @param dispatcher
     */
    public FarmProxy(final String fullJid, final Dispatcher dispatcher) {
        super(fullJid, dispatcher);
    }

    /**
     * Create a new farm proxy with a provided fully-qualified jid.
     *
     * @param fullJid           the fully-qualified farm jid
     * @param dispatcher
     * @param discoInfoDocument
     */
    public FarmProxy(final String fullJid, final Dispatcher dispatcher, final Document discoInfoDocument) {
        super(fullJid, dispatcher, discoInfoDocument);
    }

    /**
     * Given a fully-qualified virtual machine jid, get its virtual machine proxy representation
     *
     * @param vmJid a fully-qualified virtual machine jid
     * @return the virtual machine proxy with the associated jid (can be null)
     */
    public VmProxy getVmProxy(String vmJid) {
        return vmProxies.get(vmJid);
    }

    /**
     * Add a virtual machine proxy to this farm proxy.
     *
     * @param vmProxy the virtual maching proxy to add
     */
    public void addVmProxy(VmProxy vmProxy) {
        this.vmProxies.put(vmProxy.getFullJid(), vmProxy);
    }

    /**
     * The set of all virtual machine proxies indexed by this farm proxy.
     * Note that this collection does not contain proxies to all virtual machines that exist at this farm.
     * Instead it only contains those proxies to those virtual machines that have been spawed by the villein.
     * It is possible to add more proxies through addVmProxy().
     * However, be sure that these added proxies have associated virtual machine passwords.
     *
     * @return a collection of virtual machine proxies that are maintained by this farm proxy
     */
    public Collection<VmProxy> getVmProxies() {
        return this.vmProxies.values();
    }

    /**
     * Given a fully-qualified virtual machine jid, remove the corresponding virtual machine proxy from this farm proxy's collection.
     *
     * @param vmJid a fully-qualified virtual machine jid
     */
    public void removeVmProxy(String vmJid) {
        this.vmProxies.remove(vmJid);
    }

    /**
     * The virtual machines species that are supported by the farm.
     * This information is lifted from the disco#info of the farm.
     *
     * @return the set of species supported by this farm
     */
    public Set<String> getSupportedVmSpecies() {
        Field field = this.getField(LinkedProcess.VM_SPECIES_ATTRIBUTE);
        if (null != field)
            return field.getValues();
        else
            return new HashSet<String>();
    }

    /**
     * Determines wherther the provided virtual machine species is supported by this farm.
     * This information is lifted from the disco#info of the farm.
     *
     * @param vmSpecies the species to check against the disco#info of the farm
     * @return if the provided species is supported by this farm
     */
    public boolean supportsSpecies(String vmSpecies) {
        return this.getSupportedVmSpecies().contains(vmSpecies);
    }

    /**
     * The supplied farm password.
     *
     * @return the farm password supplied
     */
    public String getFarmPassword() {
        return this.farmPassword;
    }

    /**
     * If a farm requires a password, then a password should be provided to the farm proxy
     * so that all commands issued by the farm proxy have the required password.
     *
     * @param farmPassword the password of the farm
     */
    public void setFarmPassword(String farmPassword) {
        this.farmPassword = farmPassword;
    }

    /**
     * Whether a password is required by the farm.
     * This information is lifted from the disco#info of the farm.
     *
     * @return if a password is required
     */
    public boolean requiresFarmPassword() {
        Field field = this.getField(LinkedProcess.FARM_PASSWORD_REQUIRED);
        if (null != field) {
            return field.getBooleanValue();
        } else {
            return false;
        }
    }

    /**
     * The number of milliseconds before a virtual machine is shutdown by a farm.
     * This information is lifted from the disco#info of the farm.
     *
     * @return the number of milliseconds before a virtual machine is shutdown
     */
    public long getVmTimeToLive() {
        Field field = this.getField(LinkedProcess.VM_TIME_TO_LIVE);
        if (null != field) {
            return field.getLongValue();
        } else {
            return -1;
        }
    }

    /**
     * The number of milliseconds before a job that is submitted to a virtual machine times out.
     * This information is lifted from the disco#info of the farm.
     *
     * @return the number of milliseconds before a job times out
     */
    public long getJobTimeout() {
        Field field = this.getField(LinkedProcess.JOB_TIMEOUT);
        if (null != field) {
            return field.getLongValue();
        } else {
            return -1;
        }
    }

    /**
     * Spawn a virtual machine off of this farm.
     *
     * @param vmSpecies      the species of the virtual machine to spawn (make sure its a supported species)
     * @param successHandler the handler called when a sucessful result has occurred
     * @param errorHandler   the handler called when an error result has occurred
     */
    public void spawnVm(final String vmSpecies, final Handler<VmProxy> successHandler, final Handler<LopError> errorHandler) {
        this.dispatcher.getSpawnVmCommand().send(this, vmSpecies, successHandler, errorHandler);
    }
}
