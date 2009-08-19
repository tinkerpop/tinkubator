/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.security;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 3, 2009
 * Time: 5:26:32 PM
 * To change this template use File | Settings | File Templates.
 */
public enum PermissionType {
    permission("permission", "exercise a permission"),
    createClassLoader("create_class_loader", "create a Java class loader"),
    access("access", "modify a Java thread or thread group"),
    exit("exit", "exit the farm process"),
    exec("exec", "execute a program"),
    link("link", "create a file system link"),
    read("read", "read from a file"),
    write("write", "write to a file"),
    delete("delete", "delete a file"),
    connect("connect", "open a socket connection"),
    listen("listen", "wait for a connection request"),
    accept("accept", "accept a socket connection"),
    multicast("multicast", "use IP multicast"),
    propertiesAccess("properties_access", "access or modify system properties"),
    propertyAccess("property_access", "access or modify a system property"),
    printJobAccess("print_job_access", "initiate a print job request"),
    systemClipboardAccess("system_clipboard_access", "access the system clipboard"),
    awtEventQueueAccess("awt_event_queue_access", "access the Java AWT event queue"),
    packageAccess("package_access", "access a Java package"),
    packageDefinition("package_definition", "define classes in a package"),
    setFactory("set_factory", "set a socket factory"),
    memberAccess("member_access", "access Java members"),
    securityAccess("security_access", "exercise a permission");

    private final String specName;
    private final String label;

    PermissionType(final String specName,
                   final String label) {
        this.specName = specName;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPermitted(final Properties props) {
        return Boolean.valueOf(props.getProperty(getPropertyName()));
    }

    public static Set<PermissionType> createSet(final Properties props) {
        Set<PermissionType> set = new HashSet<PermissionType>();

        for (PermissionType pt : PermissionType.values()) {
            if (pt.isPermitted(props)) {
                set.add(pt);
            }
        }

        return set;
    }

    public String getPropertyName() {
        return "org.linkedprocess.security." + this;
    }

    public String getSpecName() {
        return specName;
    }
}
