/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.linkedprocess.Error;

/**
 * ResultHolder is a "helper class" that is used to hold both succesful- and error-based results in a single data structure.
 *  
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ResultHolder<T> {
    private T result;
    private Error error;

    public ResultHolder() {
    }

    public ResultHolder(T result) {
        this.result = result;
    }

    public ResultHolder(org.linkedprocess.Error error) {
        this.error = error;
    }

    public T getResult() {
        return this.result;
    }

    public Error getLopError() {
        return this.error;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public void setLopError(Error error) {
        this.error = error;
    }

    public boolean successfulResult() {
        return this.error == null;
    }

    public boolean isEmpty() {
        return (this.error == null && this.result == null);
    }
}
