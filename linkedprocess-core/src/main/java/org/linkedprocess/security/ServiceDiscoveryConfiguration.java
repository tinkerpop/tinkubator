/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.security;

import org.jdom.Element;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;

import java.util.HashSet;
import java.util.Set;

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
            deletePermissions,
            execPermissions,
            linkPermissions;
            /*httpGetPermissions = null,
            httpPutPermissions = null,
            httpPostPermissions = null*/

    private final Set<PermissionType> permittedTypes;

    public ServiceDiscoveryConfiguration(final VmSecurityManager m) {
        permittedTypes = m.getPermittedTypes();
        readPermissions = m.getReadPermissions();
        writePermissions = m.getWritePermissions();
        deletePermissions = m.getDeletePermittedPaths();
        execPermissions = m.getExecPermissions();
        linkPermissions = m.getLinkPermissions();
    }

    // TODO: namespace logic
    public ServiceDiscoveryConfiguration(final Element x) {
        permittedTypes = new HashSet<PermissionType>();

        if (!x.getName().equals(X)) {
            throw new IllegalArgumentException("expected a jabber:x:data element named 'x'");
        }

        for (Object field : x.getChildren(FIELD)) {
            if (field instanceof Element) {
                String varValue = ((Element) field).getAttributeValue(VAR);
                PermissionType type = PermissionType.valueOf(varValue);
                if (null == type) {
                    throw new IllegalArgumentException("missing '" + VAR + "' attribute");
                }

                permittedTypes.add(type);

                switch (type) {
                    case read:
                        readPermissions = createPathPermissions((Element) field);
                        break;
                    case write:
                        writePermissions = createPathPermissions((Element) field);
                        break;
                    case delete:
                        deletePermissions = createPathPermissions((Element) field);
                    case exec:
                        execPermissions = createPathPermissions((Element) field);
                        break;
                    case link:
                        linkPermissions = createPathPermissions((Element) field);
                        break;
                    default:
                        // Other types have no special formatting.
                }
            }
        }
    }

    // TODO: formatting for "negative" permissions
    private PathPermissions createPathPermissions(final Element field) {
        PathPermissions p = new PathPermissions();

        for (Object value : field.getChildren(VALUE)) {
            if (value instanceof Element) {
                String s = ((Element) value).getText();
                if (0 == s.length()) {
                    throw new IllegalArgumentException("empty '" + VALUE + "' text");
                }
                p.isPermitted(s);
            }
        }

        return p;
    }

    public void modifySecurityManager(final VmSecurityManager manager) {
        manager.setReadPermissions(readPermissions);
        manager.setWritePermissions(writePermissions);
        manager.setDeletePermissions(deletePermissions);
        manager.setExecPermissions(execPermissions);
        manager.setLinkPermissions(linkPermissions);
    }

    public Element toElement() {
        Element x = new Element(X);

        for (PermissionType type : permittedTypes) {
            Element field = new Element(FIELD);
            field.setAttribute(VAR, type.getSpecName());
            x.addContent(field);

            switch (type) {
                case read:
                    if (null != readPermissions) {
                        addPermittedPaths(field, readPermissions);
                    }
                    break;
                case write:
                    if (null != writePermissions) {
                        addPermittedPaths(field, writePermissions);
                    }
                    break;
                case delete:
                    if (null != deletePermissions) {
                        addPermittedPaths(field, deletePermissions);
                    }
                case exec:
                    if (null != execPermissions) {
                        addPermittedPaths(field, execPermissions);
                    }
                    break;
                case link:
                    if (null != linkPermissions) {
                        addPermittedPaths(field, linkPermissions);
                    }
                    break;
                default:
                    // Other types have no special formatting.
            }
        }

        return x;
    }

    public void addFields(final DataForm serviceExtension) {
        for (PermissionType type : PermissionType.values()) {
            FormField field = new FormField(type.getSpecName());
            field.setLabel(type.getLabel());

            switch (type) {
                case read:
                    field.setType(FormField.TYPE_LIST_MULTI);
                    if (null != readPermissions) {
                        addPermittedPaths(field, readPermissions);
                    }
                    break;
                case write:
                    field.setType(FormField.TYPE_LIST_MULTI);
                    if (null != writePermissions) {
                        addPermittedPaths(field, writePermissions);
                    }
                    break;
                case delete:
                    field.setType(FormField.TYPE_LIST_MULTI);
                    if (null != deletePermissions) {
                        addPermittedPaths(field, deletePermissions);
                    }
                    break;
                case exec:
                    field.setType(FormField.TYPE_LIST_MULTI);
                    if (null != execPermissions) {
                        addPermittedPaths(field, execPermissions);
                    }
                    break;
                case link:
                    field.setType(FormField.TYPE_LIST_MULTI);
                    if (null != linkPermissions) {
                        addPermittedPaths(field, linkPermissions);
                    }
                    break;
                default:
                    // Other types have no special formatting.
                    field.setType(FormField.TYPE_BOOLEAN);
                    field.addValue(permittedTypes.contains(type) ? "true" : "false");
            }

            serviceExtension.addField(field);
        }
    }

    private void addPermittedPaths(final Element field,
                                   final PathPermissions p) {
        for (String path : p.getPositiveRules()) {
            Element value = new Element(VALUE);
            value.setText(path);
            field.addContent(value);
        }
    }

    private void addPermittedPaths(final FormField field,
                                   final PathPermissions p) {
        for (String path : p.getPositiveRules()) {
            field.addValue(path);
        }
    }
}
