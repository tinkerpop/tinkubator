package org.linkedprocess.xmpp.villein.operations;

import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 6, 2009
 * Time: 5:53:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class HandlerSet<T> {
    private final Map<String, HandlerRecord> handlerRecords;

    public HandlerSet() {
        handlerRecords = new HashMap<String, HandlerRecord>();
    }

    public Handler<T> addHandler(final String id,
                                 final Handler<T> handler) {
        HandlerRecord r = new HandlerRecord(handler);
        return handlerRecords.put(id, r).getHandler();
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
            XmppVillein.LOGGER.warning("No handler found for " + t + "--" + id);
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
