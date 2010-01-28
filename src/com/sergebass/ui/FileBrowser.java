/*
 * FileBrowser.java
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * Portions Copyrighted 2008 by Serge Perinsky.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package com.sergebass.ui;

import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;

/**
 * The <code>FileBrowser</code> custom component lets the user list files and
 * directories. It's uses FileConnection Optional Package (JSR 75). The FileConnection
 * Optional Package APIs give J2ME devices access to file systems residing on mobile devices,
 * primarily access to removable storage media such as external memory cards.
 *
 * @author breh
 * @author Serge Perinsky
 */
public class FileBrowser
        extends List
        implements CommandListener {

    /**
     * Command fired on file selection.
     */
    public static final Command OPEN_ITEM_COMMAND = new Command("Open", Command.OK, 1);
    public static final Command SELECT_ITEM_COMMAND = new Command("Select", Command.OK, 2);

    private String currentDirectoryName = MEGA_ROOT;
    private String currentFileName;
    private Image directoryIcon;
    private Image fileIcon;
    private Image[] iconList;
    private CommandListener commandListener;

    boolean areFoldersSelectable = true;
    boolean areFilesSelectable = true;

    /* special string denotes upper directory */
    private static final String UP_DIRECTORY = "..";

    /* special string that denotes upper directory accessible by this browser.
     * this virtual directory contains all roots.
     */
    private static final String MEGA_ROOT = "/";

    /* separator string as defined by FC specification */
    private static final String SEPARATOR_STRING = "/";

    /* separator character as defined by FC specification */
    private static final char SEPARATOR_CHARACTER = '/';

    private Display display;

    private String selectedURL;

    private String filter = null;

    private String title;

    /**
     * Creates a new instance of FileBrowser for given <code>Display</code> object.
     * @param display non null display object.
     */
    public FileBrowser(Display display) {
        this(display, null, true, true);
    }
    /**
     * Creates a new instance of FileBrowser for given <code>Display</code> object.
     * @param display non null display object.
     * @param initialURL 
     * @param areFoldersSelectable
     * @param areFilesSelectable
     */
    public FileBrowser(Display display,
                       String initialURL,
                       boolean areFoldersSelectable,
                       boolean areFilesSelectable) {
        super("", IMPLICIT);
        
        this.display = display;
        this.areFilesSelectable = areFilesSelectable;
        this.areFoldersSelectable = areFoldersSelectable;

        super.addCommand(OPEN_ITEM_COMMAND);
        super.setCommandListener(this);
        setSelectCommand(OPEN_ITEM_COMMAND);

        try {
            directoryIcon = Image.createImage("/images/dir.png");
        } catch (IOException e) {
            directoryIcon = null;
        }
        try {
            fileIcon = Image.createImage("/images/file.png");
        } catch (IOException e) {
            fileIcon = null;
        }
        iconList = new Image[]{fileIcon, directoryIcon};

        setCurrentURL(initialURL);
    }

    public void setCurrentURL(String newCurrentURL) {
        if (newCurrentURL == null) {
            currentDirectoryName = MEGA_ROOT;
        } else { // something was specified
            if (newCurrentURL.startsWith("file:///")) {
                // strip the initial URL of the "file:///" prefix
                int lastDirectorySeparatorIndex = newCurrentURL.lastIndexOf(SEPARATOR_CHARACTER);
                currentDirectoryName = newCurrentURL.substring(8, lastDirectorySeparatorIndex + 1);
                
/// where should we handle errors from incorrect initial URLs?
System.out.println("Setting default to " + currentDirectoryName);
            }
        }
        showDirectory();
    }

    private void showDirectory() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    showCurrentDirectory();
                } catch (SecurityException e) {
                    Alert alert = new Alert("Error", "You are not authorized to access the restricted API", null, AlertType.ERROR);
                    alert.setTimeout(2000);
                    display.setCurrent(alert, FileBrowser.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Indicates that a command event has occurred on Displayable d.
     * @param command a <code>Command</code> object identifying the command. This is either
     * one of the applications have been added to <code>Displayable</code> with <code>addCommand(Command)</code>
     * or is the implicit <code>SELECT_COMMAND</code> of List.
     * @param screen the <code>Displayable</code> on which this event has occurred
     */
    public void commandAction(final Command command, Displayable screen) {
        if (command.equals(SELECT_ITEM_COMMAND) || command.equals(OPEN_ITEM_COMMAND)) {
            List list = (List) screen;
            currentFileName = list.getString(list.getSelectedIndex());
            new Thread(new Runnable() {
                public void run() {
                    // is this a directory?
                    if ((currentFileName.endsWith(SEPARATOR_STRING) || currentFileName.equals(UP_DIRECTORY))) {
                        if (command.equals(OPEN_ITEM_COMMAND)) {
                            openDirectory(currentFileName);
                        } else if (command.equals(SELECT_ITEM_COMMAND)) {
                            if (areFoldersSelectable) {
                                performSelection();
                            }
                        }
                    } else { // just a file, clicking on files always selects them (open/select is the same)
                        if (areFilesSelectable) {
                            performSelection();
                        }
                    }
                }
            }).start();
        } else {
            commandListener.commandAction(command, screen);
        }
    }

    /**
     * Sets component's title.
     *  @param title component's title.
     */
    public void setTitle(String title) {
        this.title = title;
        super.setTitle(title);
    }

    /**
     * Show file list in the current directory .
     */
    private void showCurrentDirectory() {
        if (title == null) {
            super.setTitle(currentDirectoryName);
        }
        Enumeration fsRootsEnumeration = null;
        FileConnection currentDirectory = null;

///
System.out.println("currentDirectoryName=" + currentDirectoryName);
        
        deleteAll();
        if (MEGA_ROOT.equals(currentDirectoryName)) {
            append(UP_DIRECTORY, directoryIcon);
            fsRootsEnumeration = FileSystemRegistry.listRoots();
        } else {
            try {
                currentDirectory = (FileConnection) Connector.open
                        ("file:///" + currentDirectoryName, Connector.READ);
                fsRootsEnumeration = currentDirectory.list();
            } catch (IOException ioe) {
            }
            append(UP_DIRECTORY, directoryIcon);
        }

        if (fsRootsEnumeration == null) {
            try {
                currentDirectory.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return;
        }

        while (fsRootsEnumeration.hasMoreElements()) {
            String fileName = (String) fsRootsEnumeration.nextElement();
            if (fileName.charAt(fileName.length() - 1) == SEPARATOR_CHARACTER) {
                // This is directory
                append(fileName, directoryIcon);
            } else {
                // this is regular file
                if (filter == null || fileName.indexOf(filter) > -1) {
                    append(fileName, fileIcon);
                }
            }
        }

        if (currentDirectory != null) {
            try {
                currentDirectory.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void openDirectory(String fileName) {
        /* In case of directory just change the current directory
         * and show it
         */
        if (currentDirectoryName.equals(MEGA_ROOT)) {
            if (fileName.equals(UP_DIRECTORY)) {
                // can not go up from MEGA_ROOT
                return;
            }
            currentDirectoryName = fileName;
        } else if (fileName.equals(UP_DIRECTORY)) {
            // Go up one directory
            // TODO use setFileConnection when implemented
            int i = currentDirectoryName.lastIndexOf(SEPARATOR_CHARACTER, currentDirectoryName.length() - 2);
            if (i != -1) {
                currentDirectoryName = currentDirectoryName.substring(0, i + 1);
            } else {
                currentDirectoryName = MEGA_ROOT;
            }
        } else {
            currentDirectoryName = currentDirectoryName + fileName;
        }
        showDirectory();
    }

    /**
     * Returns selected file as a <code>FileConnection</code> object.
     * @return non null <code>FileConection</code> object
     * @throws IOException
     */
    public FileConnection getSelectedFile() throws IOException {
        FileConnection fileConnection = (FileConnection) Connector.open(selectedURL);
        return fileConnection;
    }

    /**
     * Returns selected file as a <code>FileConnection</code> object.
     * @param mode Connector.READ etc.
     * @return non null <code>FileConection</code> object
     * @throws IOException
     */
    public FileConnection getSelectedFile(int mode) throws IOException {
        FileConnection fileConnection = (FileConnection) Connector.open(selectedURL, mode);
        return fileConnection;
    }

    /**
     * Returns selected <code>FileURL</code> object.
     * @return non null <code>FileURL</code> object
     */
    public String getSelectedFileURL() {
        return selectedURL;
    }

    /**
     * Sets the file filter.
     * @param filter file filter String object
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Returns command listener.
     * @return non null <code>CommandListener</code> object
     */
    protected CommandListener getCommandListener() {
        return commandListener;
    }

    /**
     * Sets command listener to this component.
     * @param commandListener <code>CommandListener</code> to be used
     */
    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    private void performSelection() {
        selectedURL = "file:///" + currentDirectoryName + currentFileName;
        CommandListener aCommandListener = getCommandListener();
        if (aCommandListener != null) {
            aCommandListener.commandAction(SELECT_ITEM_COMMAND, this);
        }
    }
}
