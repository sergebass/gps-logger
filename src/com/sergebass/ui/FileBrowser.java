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

    private String currDirName;
    private String currFile;
    private Image dirIcon;
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
    private static final String SEP_STR = "/";

    /* separator character as defined by FC specification */
    private static final char SEP = '/';

    private Display display;

    private String selectedURL;

    private String filter = null;

    private String title;

    /**
     * Creates a new instance of FileBrowser for given <code>Display</code> object.
     * @param display non null display object.
     */
    public FileBrowser(Display display) {
        this(display, true, true);
    }
    /**
     * Creates a new instance of FileBrowser for given <code>Display</code> object.
     * @param display non null display object.
     * @param areFoldersSelectable
     * @param areFilesSelectable
     */
    public FileBrowser(Display display,
                       boolean areFoldersSelectable,
                       boolean areFilesSelectable) {
        super("", IMPLICIT);
        currDirName = MEGA_ROOT;

        this.display = display;
        this.areFilesSelectable = areFilesSelectable;
        this.areFoldersSelectable = areFoldersSelectable;

        super.addCommand(OPEN_ITEM_COMMAND);
        super.setCommandListener(this);
        setSelectCommand(OPEN_ITEM_COMMAND);

        try {
            dirIcon = Image.createImage("/images/dir.png");
        } catch (IOException e) {
            dirIcon = null;
        }
        try {
            fileIcon = Image.createImage("/images/file.png");
        } catch (IOException e) {
            fileIcon = null;
        }
        iconList = new Image[]{fileIcon, dirIcon};

        showDir();
    }

    private void showDir() {
        new Thread(new Runnable() {

            public void run() {
                try {
                    showCurrDir();
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
     * @param c a <code>Command</code> object identifying the command. This is either
     * one of the applications have been added to <code>Displayable</code> with <code>addCommand(Command)</code>
     * or is the implicit <code>SELECT_COMMAND</code> of List.
     * @param d the <code>Displayable</code> on which this event has occurred
     */
    public void commandAction(final Command c, Displayable d) {
        if (c.equals(SELECT_ITEM_COMMAND) || c.equals(OPEN_ITEM_COMMAND)) {
            List curr = (List) d;
            currFile = curr.getString(curr.getSelectedIndex());
            new Thread(new Runnable() {

                public void run() {

                    if (c.equals(OPEN_ITEM_COMMAND)) {
                        if ((currFile.endsWith(SEP_STR) || currFile.equals(UP_DIRECTORY))) {
                            openDir(currFile);
                        }
                    } else if (c.equals(SELECT_ITEM_COMMAND)) {
                        // is this a folder?
                        if ((currFile.endsWith(SEP_STR) || currFile.equals(UP_DIRECTORY))) {
                            if (areFoldersSelectable) {
                                performSelection();
                            }
                        } else { // this is a simple file
                            if (areFilesSelectable) {
                                performSelection();
                            }
                        }
                    }
                }
            }).start();
        } else {
            commandListener.commandAction(c, d);
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
    private void showCurrDir() {
        if (title == null) {
            super.setTitle(currDirName);
        }
        Enumeration e = null;
        FileConnection currDir = null;

        deleteAll();
        if (MEGA_ROOT.equals(currDirName)) {
            append(UP_DIRECTORY, dirIcon);
            e = FileSystemRegistry.listRoots();
        } else {
            try {
                currDir = (FileConnection) Connector.open("file:///" + currDirName);
                e = currDir.list();
            } catch (IOException ioe) {
            }
            append(UP_DIRECTORY, dirIcon);
        }

        if (e == null) {
            try {
                currDir.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return;
        }

        while (e.hasMoreElements()) {
            String fileName = (String) e.nextElement();
            if (fileName.charAt(fileName.length() - 1) == SEP) {
                // This is directory
                append(fileName, dirIcon);
            } else {
                // this is regular file
                if (filter == null || fileName.indexOf(filter) > -1) {
                    append(fileName, fileIcon);
                }
            }
        }

        if (currDir != null) {
            try {
                currDir.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void openDir(String fileName) {
        /* In case of directory just change the current directory
         * and show it
         */
        if (currDirName.equals(MEGA_ROOT)) {
            if (fileName.equals(UP_DIRECTORY)) {
                // can not go up from MEGA_ROOT
                return;
            }
            currDirName = fileName;
        } else if (fileName.equals(UP_DIRECTORY)) {
            // Go up one directory
            // TODO use setFileConnection when implemented
            int i = currDirName.lastIndexOf(SEP, currDirName.length() - 2);
            if (i != -1) {
                currDirName = currDirName.substring(0, i + 1);
            } else {
                currDirName = MEGA_ROOT;
            }
        } else {
            currDirName = currDirName + fileName;
        }
        showDir();
    }

    /**
     * Returns selected file as a <code>FileConnection</code> object.
     * @return non null <code>FileConection</code> object
     */
    public FileConnection getSelectedFile() throws IOException {
        FileConnection fileConnection = (FileConnection) Connector.open(selectedURL);
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
        selectedURL = "file:///" + currDirName + currFile;
        CommandListener commandListener = getCommandListener();
        if (commandListener != null) {
            commandListener.commandAction(SELECT_ITEM_COMMAND, this);
        }
    }
}
