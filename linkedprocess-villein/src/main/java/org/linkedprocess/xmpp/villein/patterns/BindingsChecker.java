package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.TypedValue;

/**
 * User: marko
 * Date: Aug 6, 2009
 * Time: 12:39:07 AM
 */
public interface BindingsChecker {

    public boolean areEquivalentBindings(VMBindings checkBindings, VMBindings desiredBindings);
}
