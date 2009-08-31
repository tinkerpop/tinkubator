/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.linkedprocess.LopError;

/**
 * ResultHolder is a "helper class" that is used to hold both succesful- and error-based results in a single data structure.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ResultHolder<T> {
    private T success;
    private LopError error;

    public ResultHolder() {
    }

    public ResultHolder(T success) {
        this.success = success;
    }

    public ResultHolder(LopError error) {
        this.error = error;
    }

    public T getSuccess() {
        return this.success;
    }

    public LopError getLopError() {
        return this.error;
    }

    public void setSuccess(T success) {
        this.success = success;
    }

    public void setLopError(LopError error) {
        this.error = error;
    }

    public boolean isSuccessful() {
        return this.error == null;
    }

    public boolean isEmpty() {
        return (this.error == null && this.success == null);
    }
}
