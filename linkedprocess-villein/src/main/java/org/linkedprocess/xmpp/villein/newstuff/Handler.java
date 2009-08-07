package org.linkedprocess.xmpp.villein.newstuff;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 6, 2009
 * Time: 4:09:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Handler<T> {
    void handle(T t);
}

