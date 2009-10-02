/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.security;

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
    acceptConnection("accept_connection", "accept a socket connection"),
    accessAWTEventQueue("access_awt_event_queue", "access the Java AWT event queue"),
    accessMember("access_members", "access Java members"),
    accessPackage("access_package", "access a Java package"),
    accessPrintJob("access_print_job", "initiate a print job request"),
    accessProperties("access_properties", "read or modify system properties"),
    accessProperty("access_property", "read or modify a system property"),
    accessSystemClipboard("access_system_clipboard", "access the system clipboard"),
    createClassLoader("create_class_loader", "create a Java class loader"),
    createFileLink("create_file_link", "create a file system link"),
    defineClass("define_class", "define classes in a package"),
    deleteFile("delete_file", "delete a file"),
    executeProgram("execute_program", "execute a program"),
    exerciseNamedPermission("exercise_named_permission", "exercise a permission"),
    exercisePermission("exercise_permission", "exercise a permission"),
    listenForConnection("listen_for_connection", "wait for a connection request"),
    modifyThread("modify_thread", "modify a Java thread or thread group"),
    openConnection("open_connection", "open a socket connection"),
    performMulticast("perform_multicast", "use IP multicast"),
    readFile("read_file", "read from a file"),
    setSocketFactory("set_socket_factory", "set a socket factory"),
    shutdownFarm("shutdown_farm", "exit the farm process"),
    writeFile("write_file", "write to a file");

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
        return "org.linkedprocess.farm.security." + this;
    }

    public String getSpecName() {
        return specName;
    }
}
