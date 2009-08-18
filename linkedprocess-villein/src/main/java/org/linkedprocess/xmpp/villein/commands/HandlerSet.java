/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.commands;

import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A HandlerSet maintains a collection of HandlerRecords which each maintain a Handler for a command.
 * <p/>
 * User: josh
 * Date: Aug 6, 2009
 * Time: 5:53:06 PM
 */
public class HandlerSet<T> {
    private final Map<String, HandlerRecord> handlerRecords;

    public HandlerSet() {
        handlerRecords = new HashMap<String, HandlerRecord>();
    }

    public Handler<T> addHandler(final String id,
                                 final Handler<T> handler) {
        if (null != handler) {
            HandlerRecord r = new HandlerRecord(handler);
            handlerRecords.put(id, r);
        }
        return handler;

    }

    public Handler<T> removeHandler(final String id) {
        HandlerRecord r = handlerRecords.remove(id);
        return null == r
                ? null
                : r.getHandler();
    }

    public void handle(final String id, final T t) {
        HandlerRecord r = handlerRecords.get(id);
        if (null != r) {
            r.getHandler().handle(t);
        } else {
            XmppVillein.LOGGER.warning("No handler found for packet " + id);
        }
    }

    public void removeOldHandlers(final long sinceTimestamp) {
        List<String> toRemove = new LinkedList<String>();

        for (String id : handlerRecords.keySet()) {
            HandlerRecord r = handlerRecords.get(id);
            if (r.getTimestamp() < sinceTimestamp) {
                toRemove.add(id);
            }
        }

        for (String id : toRemove) {
            handlerRecords.remove(id);
        }
    }

    private class HandlerRecord {
        private final Handler<T> handler;
        private final long timestamp;

        public HandlerRecord(final Handler<T> handler) {
            this.handler = handler;
            this.timestamp = System.currentTimeMillis();
        }

        public Handler<T> getHandler() {
            return handler;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
