package gov.lanl.cnls.linkedprocess.security;

import org.jdom.Element;

import java.util.Collection;

/**
 * Author: josh
 * Date: Jul 13, 2009
 * Time: 1:39:00 PM
 */
public class ServiceDiscoveryConfiguration {
    private static final String
            FIELD = "field",
            VALUE = "value",
            VAR = "var",
            X = "x";

    private PathPermissions
            readPermissions,
            writePermissions,
            execPermissions,
            linkPermissions,
            httpGetPermissions,
            httpPutPermissions,
            httpPostPermissions;

    // TODO: namespace logic
    public ServiceDiscoveryConfiguration(final Element x) {
        if (!x.getName().equals(X)) {
            throw new IllegalArgumentException("expected a jabber:x:data element named 'x'");
        }

        for (Element field : (Collection<Element>) x.getChildren(FIELD)) {
            String varValue = field.getAttributeValue(VAR);
            VMSecurityManager.PermissionType type = VMSecurityManager.PermissionType.valueOf(varValue);
            if (null == type) {
                throw new IllegalArgumentException("missing '" + VAR + "' attribute");
            }

            switch (type) {
                case read:
                    readPermissions = createPathPermissions(field);
                    break;
                case write:
                    writePermissions = createPathPermissions(field);
                    break;
                case exec:
                    execPermissions = createPathPermissions(field);
                    break;
                case link:
                    linkPermissions = createPathPermissions(field);
                    break;
                default:
                    // Ignore.
            }
        }
    }

    // TODO: formatting for "negative" permissions
    private PathPermissions createPathPermissions(final Element field) {
        PathPermissions p = new PathPermissions();

        for (Element value : (Collection<Element>) field.getChildren(VALUE)) {
            String s = value.getText();
            if (0 == s.length()) {
                throw new IllegalArgumentException("empty '" + VALUE + "' text");
            }

            p.isPermitted(s);
        }

        return p;
    }

    public void modifySecurityManager(final VMSecurityManager manager) {
        manager.setReadPermissions(readPermissions);
        manager.setWritePermissions(writePermissions);
        manager.setExecPermissions(execPermissions);
        manager.setLinkPermissions(linkPermissions);
    }
}
