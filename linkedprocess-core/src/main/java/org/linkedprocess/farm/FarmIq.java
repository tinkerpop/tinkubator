/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.linkedprocess.LopIq;

/**
 * User: marko
 * Date: Jun 30, 2009
 * Time: 10:02:49 AM
 */
public abstract class FarmIq extends LopIq {

    protected String farmPassword;

    public void setFarmPassword(String farmPassword) {
        this.farmPassword = farmPassword;
    }

    public String getFarmPassword() {
        return this.farmPassword;
    }
}
