/*
 * (C) Serge Perinsky, 2007-2010
 */

import java.util.Vector;
import java.io.*;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.media.*;
import javax.bluetooth.*;
import javax.microedition.media.control.VideoControl;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import com.sergebass.geo.*;
import com.sergebass.geo.map.MapConfiguration;
import com.sergebass.video.*;
import com.sergebass.ui.FileBrowser;
import com.sergebass.util.*;

/**
 * @author Serge Perinsky
 */
public class GPSLogger
        extends MIDlet
        implements CommandListener,
                   ItemCommandListener,
                   GeoLocationListener,
                   SettingsListener {

    static final String ERROR_AUDIO_FILE = "sounds/error.wav";
    
    private boolean mustBeTerminated = false;
    private boolean midletPaused = false;
    static GPSLogger midlet = null;

    static GPSLoggerSettings settings = null;

    GPSLoggerBTGPSEnumerator gpsDeviceEnumerator = null;

    final Object waitingLock = new Object();
            
    int number = 0;

    GeoLocator geoLocator = null;
    final Object geoLocatorLock = new Object();
    
    GPSLogFile trackLogFile = null;
    GPXWriter trackLogWriter = null;
    final Object trackLogLock = new Object();
    
    boolean isTrackStarted = false;
    boolean isTrackSegmentStarted = false;
    final Object trackLock = new Object();

    GPSLogFile waypointLogFile = null;
    GPXWriter waypointLogWriter = null;
    final Object waypointLogLock = new Object();
    
    Vector waypoints = null;

    FileBrowser logFolderBrowser = null;
    FileBrowser mapDescriptorFileBrowser = null;

    Player errorPlayer = null;

    GPSScreen mainScreen = null;
    MapConfiguration mapConfiguration = null;

    long startTimeMillis = 0L;

    int coordinatesMode = 0;
    int altitudeUnits = 0;
    int speedUnits = 0;

    Canvas cameraCanvas = null;

    private java.util.Hashtable __previousDisplayables = new java.util.Hashtable();
    
    private Command saveWaypointCommand;
    private Command cancelWaypointCommand;
    private Command exitCommand;
    private Command takePhotoCommand;
    private Command stopCommand;
    private Command sendSMSCommand;
    private Command minimizeCommand;
    private Command okSendSMSCommand;
    private Command cancelSMSEditCommand;
    private Command okSMSEditCommand;
    private Command cancelSendSMSCommand;
    private Command searchCommand;
    private Command browseMapDescriptorFileCommand;
    private Command startCommand;
    private Command settingsCommand;
    private Command helpCommand;
    private Command backCommand;
    private Command saveSettingsCommand;
    private Command browseLogFolderCommand;
    private Command markWaypointCommand;
    private Command cancelCommand;
    private Command resetCommand;
    private Command okCommand;
    
    private Form waypointScreen;
    private TextField waypointNameTextField;
    private Form smsScreen;
    private TextField phoneNumberTextField;
    private StringItem smsLengthStringItem;
    private StringItem smsTextStringItem;
    private TextBox smsTextBox;
    private List gpsDeviceList;
    private Form startScreen;
    private StringItem gpsDeviceURLStartScreenStringItem;
    private StringItem mapDescriptorFileStartScreenStringItem;
    private StringItem gpsDeviceNameStartScreenStringItem;
    private StringItem freeSpaceStartScreenStringItem;
    private StringItem logPathStartScreenStringItem;
    private Form settingsScreen;
    private Spacer spacer1;
    private StringItem browseLogFolderStringItem;
    private Spacer spacer;
    private StringItem searchGPSStringItem;
    private ChoiceGroup logFormatChoiceGroup;
    private ChoiceGroup logSettingsChoiceGroup;
    private TextField defaultSmsPhoneNumber;
    private TextField logUpdateFrequencyTextField;
    private TextField logFileNamePrefixTextField;
    private TextField mapDescriptorFileTextField;
    private Spacer spacer3;
    private StringItem gpsDeviceNameStringItem;
    private StringItem browseMapDescriptorFileStringItem;
    private ChoiceGroup coordinatesModeChoiceGroup;
    private ChoiceGroup speedUnitsChoiceGroup;
    private TextField gpsDeviceURLTextField;
    private TextField logFolderTextField;
    private ChoiceGroup altitudeUnitsChoiceGroup;
    private ChoiceGroup languageChoiceGroup;
    private Form helpScreen;
    private StringItem freeMemoryStringItem;
    private StringItem totalMemoryStringItem;
    private Spacer spacer2;
    private StringItem copyrightStringItem1;
    private Alert errorAlert;
    private Font font;
    private Font boldFont;

    /**
     * Switches a display to previous displayable of the current displayable.
     * The <code>display</code> instance is obtain from the <code>getDisplay</code> method.
     */
    private void switchToPreviousDisplayable() {
        Displayable __currentDisplayable = getDisplay().getCurrent();
        if (__currentDisplayable != null) {
            Displayable __nextDisplayable = (Displayable) __previousDisplayables.get(__currentDisplayable);
            if (__nextDisplayable != null) {
                switchDisplayable(null, __nextDisplayable);
            }
        }
    }

    /**
     * Initilizes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {
        try {
            errorPlayer = Manager.createPlayer(getClass().getResourceAsStream
            				(ERROR_AUDIO_FILE), "audio/x-wav");
            errorPlayer.prefetch();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore?
        }
    }
    
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {
        loadSettings();
        switchDisplayable(null, getStartScreen());
        prepareStartScreen();
    }
    
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {
    }

    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {
        Display display = getDisplay();
        Displayable __currentDisplayable = display.getCurrent();
        if (__currentDisplayable != null  &&  nextDisplayable != null) {
            __previousDisplayables.put(nextDisplayable, __currentDisplayable);
        }
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }
    }

    public static GPSLogger getMidlet() {
        return midlet;
    }

    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {
        if (displayable == gpsDeviceList) {
            if (command == List.SELECT_COMMAND) {
                stopGPSDeviceSearch();
                gpsDeviceListAction();
            } else if (command == cancelCommand) {
                stopGPSDeviceSearch();
                getDisplay().setCurrent(getSettingsScreen()); // go back without history
            } else if (command == okCommand) {
                stopGPSDeviceSearch();
                gpsDeviceListAction();
            }
        } else if (displayable == helpScreen) {
            if (command == backCommand) {
                switchToPreviousDisplayable();
            }
        } else if (displayable == settingsScreen) {
            if (command == cancelCommand) {
                switchToPreviousDisplayable();
            } else if (command == saveSettingsCommand) {
                saveSettings();
                // only show summary if the last (next) displayable is introForm
                if (__previousDisplayables.get(getDisplay().getCurrent()) == startScreen) {
                    prepareStartScreen();
                }
                switchToPreviousDisplayable();
            }
        } else if (displayable == smsScreen) {
            if (command == cancelSendSMSCommand) {
                switchDisplayable(null, getSmsTextBox());
            } else if (command == okSendSMSCommand) {
                doSendSMS();
            }
        } else if (displayable == smsTextBox) {
            if (command == cancelSMSEditCommand) {
                switchDisplayable(null, getWaypointScreen());
            } else if (command == okSMSEditCommand) {
                String messageText = getSmsTextBox().getString();
                getSmsLengthStringItem().setText(messageText.length() + " characters");
                getSmsTextStringItem().setText(messageText);
                // copy phone number from settings (if the field is (still) empty)
                if (getPhoneNumberTextField().getString().trim().equals("")) {
                    getPhoneNumberTextField().setString(settings.getDefaultSmsPhoneNumber());
                }
                switchDisplayable(null, getSmsScreen());
            }
        } else if (displayable == startScreen) {
            if (command == exitCommand) {
                new Thread() {
                    public void run() {
                        exitMIDlet();
                    }
                }.start();
            } else if (command == helpCommand) {
                switchDisplayable(null, getHelpScreen());
            } else if (command == minimizeCommand) {
            	///...
            } else if (command == settingsCommand) {
                switchDisplayable(null, getSettingsScreen());
            } else if (command == startCommand) {
                startTrack();
            }
        } else if (displayable == waypointScreen) {
            if (command == cancelWaypointCommand) {
                // remove the last added waypoint: it was cancelled
                if (waypoints.size() > 1) {
                    waypoints.removeElementAt(waypoints.size() - 1);
                }
                switchToMainScreen();
            } else if (command == saveWaypointCommand) {
                new Thread() {
                    public void run() {
                        saveWaypoint();
                    }
                }.start();
            } else if (command == sendSMSCommand) {
                new Thread() {
                    public void run() {
                        sendSMS();
                    }
                }.start();
            } else if (command == takePhotoCommand) {
                new Thread() {
                    public void run() {
                        takePhoto();
                    }
                }.start();
            }
        } else { // all other displayables
            if (command.getCommandType() == Command.EXIT) {
                new Thread() {
                    public void run() {
                        exitMIDlet();
                    }
                }.start();
            } else if (command.getCommandType() == Command.BACK) {
                switchToPreviousDisplayable();
            } else if (command == stopCommand) {
                // do this in a separate thread - we have I/O there
                new Thread() {
                    public void run() {
                        stopTrack();
                    }
                }.start();
            } else if (command == markWaypointCommand) {
                new Thread() {
                    public void run() {
                        markWaypoint();
                    }
                }.start();
            } else if (command == settingsCommand) {
                switchDisplayable(null, getSettingsScreen());
            } else if (command == minimizeCommand) {
                getDisplay().setCurrent(null); // null screen = minimization
            } else if (command == resetCommand) {
                resetOdometer();
            }
        }
    }

    /**
     * Returns an initiliazed instance of exitCommand component.
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {
            exitCommand = new Command(GPSLoggerLocalization.getMessage("exitCommand"), Command.EXIT, 4);
        }
        return exitCommand;
    }

    /**
     * Performs an action assigned to the searchDevices entry-point.
     */
    public void searchDevices() {
        getGpsDeviceList().deleteAll();
        gpsDeviceEnumerator = new GPSLoggerBTGPSEnumerator(this, getGpsDeviceList());
        gpsDeviceEnumerator.start();
        // jump to the discovered bluetooth device list (without history)
        getDisplay().setCurrent(getGpsDeviceList());
    }

    void stopGPSDeviceSearch() {
        if (gpsDeviceEnumerator != null) {
            gpsDeviceEnumerator.cancel();
        }
    }
    
    /**
     * Returns an initiliazed instance of gpsDeviceList component.
     * @return the initialized component instance
     */
    public List getGpsDeviceList() {
        if (gpsDeviceList == null) {
            gpsDeviceList = new List("GPS devices", Choice.IMPLICIT);
            gpsDeviceList.addCommand(getCancelCommand());
            gpsDeviceList.addCommand(getOkCommand());
            gpsDeviceList.setCommandListener(this);
            gpsDeviceList.setFitPolicy(Choice.TEXT_WRAP_ON);
            gpsDeviceList.setSelectCommand(getOkCommand());
        }
        return gpsDeviceList;
    }

    /**
     * Performs an action assigned to the selected list element in the gpsDeviceList component.
     */
    public void gpsDeviceListAction() {
        String __selectedString = getGpsDeviceList().getString(getGpsDeviceList().getSelectedIndex());
        Vector deviceServiceRecords = gpsDeviceEnumerator.getDeviceServiceRecords();

        if (deviceServiceRecords == null) {
            handleException(new Exception("No bluetooth services were found :("),
                            getSettingsScreen());
            return;
        }

        if (deviceServiceRecords.size() == 0) {
            handleException(new Exception("No bluetooth services were found :("),
                            getSettingsScreen());
            return;
        }

        int selectedDeviceIndex = getGpsDeviceList().getSelectedIndex();
        ServiceRecord service
                = (ServiceRecord)(deviceServiceRecords.elementAt(selectedDeviceIndex));

        System.out.println("Chosen Bluetooth device: " + service.getHostDevice().getBluetoothAddress());
        System.out.println("Chosen Bluetooth service: " + service.toString());

        getGpsDeviceList().setTitle("Connecting to GPS...");

        String connectionURLString = service.getConnectionURL(0, // security
                                                              true); // master mode
        if (connectionURLString == null) {
            handleException(new Exception("This Bluetooth device does not support Simple SPP Service."),
                            getSettingsScreen());
            return;
        }

        settings.setGPSDeviceURL(connectionURLString);
        getGpsDeviceURLTextField().setString(connectionURLString);

        String friendlyDeviceName = null;
        try {
            friendlyDeviceName = service.getHostDevice().getFriendlyName(false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (friendlyDeviceName == null) {
            friendlyDeviceName = "(name unknown)"; /// localize
        }

        settings.setGPSDeviceName(friendlyDeviceName);
        getGpsDeviceNameStringItem().setText(friendlyDeviceName);

        getDisplay().setCurrent(getSettingsScreen()); // go back without history
    }

    /**
     * Returns an initiliazed instance of searchCommand component.
     * @return the initialized component instance
     */
    public Command getSearchCommand() {
        if (searchCommand == null) {
            searchCommand = new Command(GPSLoggerLocalization.getMessage("searchCommand"),
            				GPSLoggerLocalization.getMessage("searchCommand"),
            				Command.OK,
            				3);
        }
        return searchCommand;
    }

    /**
     * Performs an action assigned to the startTrack entry-point.
     */
    public void startTrack() {
        // run our tracking stuff in a separate thread
        new Thread() {
            public void run() {
                startTrackingThread();
            }
        }.start();
    }
    
    public void startTrackingThread() {
        waypoints = new Vector(); // (re)initialize

        // initialize the maps
        String mapDescriptorFileName = settings.getMapDescriptorFilePath();
        if (mapDescriptorFileName != null && (!mapDescriptorFileName.equals(""))) { // map specified?
            try {
                System.out.println("Parsing map descriptor file (" + mapDescriptorFileName + ")...");
                mapConfiguration = MapConfiguration.newInstance(mapDescriptorFileName);
            } catch (Exception ex) {
                ex.printStackTrace();
                handleException(ex, startScreen);
                return;
            }
        }

        mainScreen = new GPSScreen(this);
        switchDisplayable(null, mainScreen);
        mainScreen.setTitle("Starting...");
        System.out.println("Starting a track log...");

        startLogging(mainScreen);
    }
    
    /**
     * Returns an initiliazed instance of startScreen component.
     * @return the initialized component instance
     */
    public Form getStartScreen() {
        if (startScreen == null) {
            startScreen = new Form("GPS Logger",
            			   new Item[] {
            			   	getGpsDeviceNameStartScreenStringItem(),
            			   	getGpsDeviceURLStartScreenStringItem(),
            			   	getLogPathStartScreenStringItem(),
            			   	getFreeSpaceStartScreenStringItem(),
            			   	getMapDescriptorFileStartScreenStringItem()
            			   	});
            startScreen.addCommand(getStartCommand());
            startScreen.addCommand(getSettingsCommand());
            startScreen.addCommand(getExitCommand());
            startScreen.addCommand(getHelpCommand());
            startScreen.addCommand(getMinimizeCommand());
            startScreen.setCommandListener(this);
        }
        return startScreen;
    }
   
    /**
     * Returns an initiliazed instance of startCommand component.
     * @return the initialized component instance
     */
    public Command getStartCommand() {
        if (startCommand == null) {
            startCommand = new Command(GPSLoggerLocalization.getMessage("startCommand"), GPSLoggerLocalization.getMessage("startCommand"), Command.OK, 0);
        }
        return startCommand;
    }

    /**
     * Returns an initiliazed instance of helpCommand component.
     * @return the initialized component instance
     */
    public Command getHelpCommand() {
        if (helpCommand == null) {
            helpCommand = new Command(GPSLoggerLocalization.getMessage("helpCommand"), Command.HELP, 3);
        }
        return helpCommand;
    }

    /**
     * Returns an initiliazed instance of backCommand component.
     * @return the initialized component instance
     */
    public Command getBackCommand() {
        if (backCommand == null) {
            backCommand = new Command(GPSLoggerLocalization.getMessage("backCommand"), Command.BACK, 1);
        }
        return backCommand;
    }

    /**
     * Returns an initiliazed instance of settingsScreen component.
     * @return the initialized component instance
     */
    public Form getSettingsScreen() {
        if (settingsScreen == null) {
            settingsScreen = new Form(GPSLoggerLocalization.getMessage("Settings"),
                    new Item[] {
                        getGpsDeviceNameStringItem(),
                        getGpsDeviceURLTextField(),
                        getSearchGPSStringItem(),
                        getSpacer1(),
                        getLogFolderTextField(),
                        getBrowseLogFolderStringItem(),
                        getSpacer(),
                        getLogFileNamePrefixTextField(),
///tmp: uncomment when implemented
                        ///getLogUpdateFrequencyTextField(),
                        ///getLogFormatChoiceGroup(),
                        ///getLogSettingsChoiceGroup(),
///^ uncomment when implemented
                        getSpacer3(),
                        getMapDescriptorFileTextField(),
                        getBrowseMapDescriptorFileStringItem(),
                        getCoordinatesModeChoiceGroup(),
                        getAltitudeUnitsChoiceGroup(),
                        getSpeedUnitsChoiceGroup(),
                        getLanguageChoiceGroup(),
                        getDefaultSmsPhoneNumber()
                    });
            settingsScreen.addCommand(getSaveSettingsCommand());
            settingsScreen.addCommand(getCancelCommand());
            settingsScreen.setCommandListener(this);
        }
        return settingsScreen;
    }

    /**
     * Returns an initiliazed instance of helpScreen component.
     * @return the initialized component instance
     */
    public Form getHelpScreen() {
        if (helpScreen == null) {
            helpScreen = new Form("Help", new Item[] { getCopyrightStringItem1(), getSpacer2(), getTotalMemoryStringItem(), getFreeMemoryStringItem() });
            helpScreen.addCommand(getBackCommand());
            helpScreen.setCommandListener(this);
            Runtime runtime = Runtime.getRuntime();
            getTotalMemoryStringItem().setText(runtime.totalMemory() + " bytes");
            getFreeMemoryStringItem().setText(runtime.freeMemory() + " bytes");
        }
        return helpScreen;
    }

    /**
     * Returns an initiliazed instance of settingsCommand component.
     * @return the initialized component instance
     */
    public Command getSettingsCommand() {
        if (settingsCommand == null) {
            settingsCommand = new Command(GPSLoggerLocalization.getMessage("settingsCommand"), GPSLoggerLocalization.getMessage("settingsCommand"), Command.SCREEN, 10);
        }
        return settingsCommand;
    }

    /**
     * Returns an initiliazed instance of coordinatesModeChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getCoordinatesModeChoiceGroup() {
        if (coordinatesModeChoiceGroup == null) {
            coordinatesModeChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Coordinates"), Choice.EXCLUSIVE);
            coordinatesModeChoiceGroup.append("DD.dd\u00B0", null);
            coordinatesModeChoiceGroup.append("DD\u00B0 MM.mm\'", null);
            coordinatesModeChoiceGroup.append("DD\u00B0 MM\' SS.ss\"", null);
            coordinatesModeChoiceGroup.setSelectedFlags(new boolean[] { false, false, false });
        }
        return coordinatesModeChoiceGroup;
    }

    /**
     * Returns an initiliazed instance of logFolderTextField component.
     * @return the initialized component instance
     */
    public TextField getLogFolderTextField() {
        if (logFolderTextField == null) {
            logFolderTextField = new TextField(GPSLoggerLocalization.getMessage("LogFolder"), "", 4096, TextField.ANY);
            logFolderTextField.addCommand(getBrowseLogFolderCommand());
            logFolderTextField.setItemCommandListener(this);
            logFolderTextField.setDefaultCommand(getBrowseLogFolderCommand());
        }
        return logFolderTextField;
    }

    /**
     * Returns an initiliazed instance of gpsDeviceURLTextField component.
     * @return the initialized component instance
     */
    public TextField getGpsDeviceURLTextField() {
        if (gpsDeviceURLTextField == null) {
            gpsDeviceURLTextField = new TextField(GPSLoggerLocalization.getMessage("GPSDevice"), "", 4096, TextField.URL);
            gpsDeviceURLTextField.addCommand(getSearchCommand());
            gpsDeviceURLTextField.setItemCommandListener(this);
        }
        return gpsDeviceURLTextField;
    }

    /**
     * Called by a system to indicated that a command has been invoked on a particular item.
     * @param command the Command that was invoked
     * @param displayable the Item where the command was invoked
     */
    public void commandAction(Command command, Item item) {
        if (item == browseLogFolderStringItem) {
            if (command == browseLogFolderCommand) {
                browseLogFolder();
            }
        } else if (item == browseMapDescriptorFileStringItem) {
            if (command == browseMapDescriptorFileCommand) {
                browseMapDescriptorFile();
            }
        } else if (item == gpsDeviceURLTextField) {
            if (command == searchCommand) {
                searchDevices();
            }
        } else if (item == logFolderTextField) {
            if (command == browseLogFolderCommand) {
                browseLogFolder();
            }
        } else if (item == mapDescriptorFileTextField) {
            if (command == browseMapDescriptorFileCommand) {
                browseMapDescriptorFile();
            }
        } else if (item == searchGPSStringItem) {
            if (command == searchCommand) {
                searchDevices();
            }
        }
    }

    /**
     * Returns an initiliazed instance of speedUnitsChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getSpeedUnitsChoiceGroup() {
        if (speedUnitsChoiceGroup == null) {
            speedUnitsChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Speed"), Choice.EXCLUSIVE);
            speedUnitsChoiceGroup.append("km/h", null);
            speedUnitsChoiceGroup.append("mph", null);
            speedUnitsChoiceGroup.append("knots", null);
            speedUnitsChoiceGroup.append("m/sec", null);
            speedUnitsChoiceGroup.setSelectedFlags(new boolean[] { false, false, false, false });
        }
        return speedUnitsChoiceGroup;
    }

    /**
     * Returns an initiliazed instance of browseLogFolderCommand component.
     * @return the initialized component instance
     */
    public Command getBrowseLogFolderCommand() {
        if (browseLogFolderCommand == null) {
        	///...
        }
        browseLogFolderCommand = new Command(GPSLoggerLocalization.getMessage("browseCommand"), Command.OK, 0);
        return browseLogFolderCommand;
    }

    void pasteLogFolderToTextField() {
        logFolderTextField.setString(logFolderBrowser.getSelectedFileURL());
        getDisplay().setCurrent(getSettingsScreen()); // without history
    }

    void pasteMapDescriptorFileToTextField() {
        mapDescriptorFileTextField.setString(mapDescriptorFileBrowser.getSelectedFileURL());
        getDisplay().setCurrent(getSettingsScreen()); // without history
    }

    void goToSettingsScreen() {
        getDisplay().setCurrent(getSettingsScreen()); // without history
    }

    /**
     * Performs an action assigned to the saveSettings entry-point.
     */
    public void saveSettings() {
        String gpsDeviceURL = gpsDeviceURLTextField.getString();
        settings.setGPSDeviceURL(gpsDeviceURL);
        gpsDeviceURLStartScreenStringItem.setText(gpsDeviceURL); // copy to the initial screen

        String gpsDeviceName = getGpsDeviceNameStringItem().getText();

        if (gpsDeviceURL.trim().equals("")) { // user removed URL (JSR179 request)
///localize!
            gpsDeviceName = "[built-in]"; // reset the name as well
            getGpsDeviceNameStringItem().setText(gpsDeviceName); // change the source too
        }

        settings.setGPSDeviceName(gpsDeviceName);
        gpsDeviceNameStartScreenStringItem.setText(gpsDeviceName); // copy to the initial screen

        String logPath = logFolderTextField.getString();
        settings.setLogFolder(logPath);
        logPathStartScreenStringItem.setText(logPath); // copy to the initial screen

        String logFileNamePrefix = logFileNamePrefixTextField.getString();
        settings.setLogFilePrefix(logFileNamePrefix);

        String mapDescriptorFileName = mapDescriptorFileTextField.getString();
        settings.setMapDescriptorFilePath(mapDescriptorFileName);

        settings.setCoordinatesMode(getCoordinatesModeChoiceGroup().getSelectedIndex());
        settings.setAltitudeUnits(getAltitudeUnitsChoiceGroup().getSelectedIndex());
        settings.setSpeedUnits(getSpeedUnitsChoiceGroup().getSelectedIndex());

        settings.setDefaultSmsPhoneNumber(getDefaultSmsPhoneNumber().getString());

        try {
            settings.save();
        } catch (RecordStoreException e) {
            handleException(e, startScreen);
            return;
        }
    }

    /**
     * Returns an initiliazed instance of saveSettingsCommand component.
     * @return the initialized component instance
     */
    public Command getSaveSettingsCommand() {
        if (saveSettingsCommand == null) {
            saveSettingsCommand = new Command(GPSLoggerLocalization.getMessage("saveSettingsCommand"), Command.OK, 0);
        }
        return saveSettingsCommand;
    }

    /**
     * Returns an initiliazed instance of copyrightStringItem1 component.
     * @return the initialized component instance
     */
    public StringItem getCopyrightStringItem1() {
        if (copyrightStringItem1 == null) {
            copyrightStringItem1 = new StringItem("(C) 2007-2009 ", "Written by Serge Perinsky");
            copyrightStringItem1.setLayout
                         (ImageItem.LAYOUT_LEFT
                        | Item.LAYOUT_TOP
                        | Item.LAYOUT_VCENTER
                        | ImageItem.LAYOUT_NEWLINE_BEFORE
                        | ImageItem.LAYOUT_NEWLINE_AFTER
                        | Item.LAYOUT_SHRINK);
        }
        return copyrightStringItem1;
    }

    /**
     * Performs an action assigned to the saveWaypoint entry-point.
     */
    public void saveWaypoint() {

        switchDisplayable(null, getMainScreen()); // go back to the main screen immediately anyway

        try {
            saveCurrentWaypoint(getWaypointNameTextField().getString().trim());
        } catch (IOException e) {
            handleException(e, getMainScreen());
        }
    }

    /**
     * Returns an initiliazed instance of markWaypointCommand component.
     * @return the initialized component instance
     */
    public Command getMarkWaypointCommand() {
        if (markWaypointCommand == null) {
            markWaypointCommand = new Command(GPSLoggerLocalization.getMessage("markWaypointCommand"), GPSLoggerLocalization.getMessage("markWaypointCommandLong"), Command.OK, 0);
        }
        return markWaypointCommand;
    }

    /**
     * Returns an initiliazed instance of cancelCommand component.
     * @return the initialized component instance
     */
    public Command getCancelCommand() {
        if (cancelCommand == null) {
            cancelCommand = new Command(GPSLoggerLocalization.getMessage("cancelCommand"), Command.CANCEL, 1);
        }
        return cancelCommand;
    }

    /**
     * Returns an initiliazed instance of altitudeUnitsChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getAltitudeUnitsChoiceGroup() {
        if (altitudeUnitsChoiceGroup == null) {
            altitudeUnitsChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Altitude"), Choice.EXCLUSIVE);
            altitudeUnitsChoiceGroup.append("meters", null);
            altitudeUnitsChoiceGroup.append("feet", null);
            altitudeUnitsChoiceGroup.setSelectedFlags(new boolean[] { false, false });
        }
        return altitudeUnitsChoiceGroup;
    }

    /**
     * Returns an initiliazed instance of languageChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getLanguageChoiceGroup() {
        if (languageChoiceGroup == null) {
            languageChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Language"), Choice.EXCLUSIVE);
            languageChoiceGroup.append("Default", null);
            languageChoiceGroup.append("English", null);
            languageChoiceGroup.append("\u0420\u0443\u0441\u0441\u043A\u0438\u0439", null);
            languageChoiceGroup.setSelectedFlags(new boolean[] { false, false, false });
        }
        return languageChoiceGroup;
    }

    /**
     * Returns an initiliazed instance of gpsDeviceNameStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getGpsDeviceNameStartScreenStringItem() {
        if (gpsDeviceNameStartScreenStringItem == null) {
            gpsDeviceNameStartScreenStringItem = new StringItem("GPS: ", "");
            gpsDeviceNameStartScreenStringItem.setLayout(ImageItem.LAYOUT_DEFAULT | Item.LAYOUT_VSHRINK);
        }
        return gpsDeviceNameStartScreenStringItem;
    }

    /**
     * Returns an initiliazed instance of errorAlert component.
     * @return the initialized component instance
     */
    public Alert getErrorAlert() {
        if (errorAlert == null) {
            errorAlert = new Alert("Error", null, null, AlertType.ERROR);
            errorAlert.setTimeout(Alert.FOREVER);
        }
        return errorAlert;
    }

    /**
     * Returns an initiliazed instance of okCommand component.
     * @return the initialized component instance
     */
    public Command getOkCommand() {
        if (okCommand == null) {
            okCommand = new Command("Ok", Command.OK, 0);
        }
        return okCommand;
    }

    /**
     * Returns an initiliazed instance of logPathStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getLogPathStartScreenStringItem() {
        if (logPathStartScreenStringItem == null) {
            logPathStartScreenStringItem = new StringItem("Log: ", "");
            logPathStartScreenStringItem.setLayout(ImageItem.LAYOUT_DEFAULT | Item.LAYOUT_VSHRINK);
        }
        return logPathStartScreenStringItem;
    }

    /**
     * Returns an initiliazed instance of freeSpaceStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getFreeSpaceStartScreenStringItem() {
        if (freeSpaceStartScreenStringItem == null) {
            freeSpaceStartScreenStringItem = new StringItem("", "");
        }
        return freeSpaceStartScreenStringItem;
    }

    /**
     * Performs an action assigned to the resetOdometer entry-point.
     */
    public void resetOdometer() {
        startTimeMillis = System.currentTimeMillis();
        getMainScreen().setTotalTime("");
    }

    /**
     * Returns an initiliazed instance of resetCommand component.
     * @return the initialized component instance
     */
    public Command getResetCommand() {
        if (resetCommand == null) {
            resetCommand = new Command(GPSLoggerLocalization.getMessage("resetCommand"), Command.OK, 0);
        }
        return resetCommand;
    }

    /**
     * Returns an initiliazed instance of font component.
     * @return the initialized component instance
     */
    public Font getFont() {
        if (font == null) {
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        return font;
    }

    /**
     * Returns an initiliazed instance of boldFont component.
     * @return the initialized component instance
     */
    public Font getBoldFont() {
        if (boldFont == null) {
            boldFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);
        }
        return boldFont;
    }

    /**
     * Returns an initiliazed instance of searchGPSStringItem component.
     * @return the initialized component instance
     */
    public StringItem getSearchGPSStringItem() {
        if (searchGPSStringItem == null) {
            searchGPSStringItem = new StringItem(GPSLoggerLocalization.getMessage("SearchGPS"), "", Item.BUTTON);
            searchGPSStringItem.addCommand(getSearchCommand());
            searchGPSStringItem.setItemCommandListener(this);
            searchGPSStringItem.setDefaultCommand(getSearchCommand());
        }
        return searchGPSStringItem;
    }

    /**
     * Returns an initiliazed instance of browseLogFolderStringItem component.
     * @return the initialized component instance
     */
    public StringItem getBrowseLogFolderStringItem() {
        if (browseLogFolderStringItem == null) {
            browseLogFolderStringItem = new StringItem(GPSLoggerLocalization.getMessage("BrowseFolder"), "", Item.BUTTON);
            browseLogFolderStringItem.addCommand(getBrowseLogFolderCommand());
            browseLogFolderStringItem.setItemCommandListener(this);
            browseLogFolderStringItem.setDefaultCommand(getBrowseLogFolderCommand());
        }
        return browseLogFolderStringItem;
    }

    /**
     * Returns an initiliazed instance of spacer component.
     * @return the initialized component instance
     */
    public Spacer getSpacer() {
        if (spacer == null) {
            spacer = new Spacer(16, 1);
        }
        return spacer;
    }

    /**
     * Returns an initiliazed instance of spacer1 component.
     * @return the initialized component instance
     */
    public Spacer getSpacer1() {
        if (spacer1 == null) {
            spacer1 = new Spacer(16, 1);
        }
        return spacer1;
    }

    /**
     * Performs an action assigned to the browseLogFolder entry-point.
     */
    public void browseLogFolder() {
        getDisplay().setCurrent(getLogFolderBrowser()); // without history
    }

    /**
     * Returns an initiliazed instance of waypointScreen component.
     * @return the initialized component instance
     */
    public Form getWaypointScreen() {
        if (waypointScreen == null) {
            waypointScreen = new Form("Waypoint", new Item[] { getWaypointNameTextField() });
            waypointScreen.addCommand(getCancelWaypointCommand());
            waypointScreen.addCommand(getSaveWaypointCommand());
            waypointScreen.addCommand(getTakePhotoCommand());
            waypointScreen.addCommand(getSendSMSCommand());
            waypointScreen.setCommandListener(this);
        }
        return waypointScreen;
    }

    /**
     * Returns an initiliazed instance of waypointNameTextField component.
     * @return the initialized component instance
     */
    public TextField getWaypointNameTextField() {
        if (waypointNameTextField == null) {
            waypointNameTextField = new TextField("Name:", "", 32, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
        }
        return waypointNameTextField;
    }

    /**
     * Returns an initiliazed instance of cancelWaypointCommand component.
     * @return the initialized component instance
     */
    public Command getCancelWaypointCommand() {
        if (cancelWaypointCommand == null) {
            cancelWaypointCommand = new Command(GPSLoggerLocalization.getMessage("cancelWaypointCommand"), Command.BACK, 0);
        }
        return cancelWaypointCommand;
    }

    /**
     * Returns an initiliazed instance of saveWaypointCommand component.
     * @return the initialized component instance
     */
    public Command getSaveWaypointCommand() {
        if (saveWaypointCommand == null) {
            saveWaypointCommand = new Command(GPSLoggerLocalization.getMessage("saveWaypointCommand"), Command.OK, 0);
        }
        return saveWaypointCommand;
    }

    /**
     * Performs an action assigned to the takePhoto entry-point.
     */
    public void takePhoto() {

        Player player;
        VideoControl videoControl;

        try{
            player = Manager.createPlayer("capture://video");
            player.realize();
            videoControl = (VideoControl)player.getControl("VideoControl");

            cameraCanvas = new CameraCanvas(videoControl);
///            cameraCanvas.addCommand(capture);
            cameraCanvas.addCommand(getBackCommand());
            cameraCanvas.setCommandListener(this);
            switchDisplayable(null, cameraCanvas);

            player.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        } catch (MediaException ex) {
            ex.printStackTrace();
            return;
        }
    }

    /**
     * Returns an initiliazed instance of takePhotoCommand component.
     * @return the initialized component instance
     */
    public Command getTakePhotoCommand() {
        if (takePhotoCommand == null) {
            takePhotoCommand = new Command(GPSLoggerLocalization.getMessage("takePhotoCommand"), Command.OK, 0);
        }
        return takePhotoCommand;
    }

    /**
     * Returns an initiliazed instance of stopCommand component.
     * @return the initialized component instance
     */
    public Command getStopCommand() {
        if (stopCommand == null) {
            stopCommand = new Command(GPSLoggerLocalization.getMessage("stopCommand"), GPSLoggerLocalization.getMessage("stopCommand"), Command.OK, 2);
        }
        return stopCommand;
    }

    /**
     * Performs an action assigned to the stopTrack entry-point.
     */
    public void stopTrack() {

        mainScreen.setTitle("Stopping...");

        synchronized (trackLock) {
            try {
                // (only finish, do not close the whole track log at this time!)
                finishTrackLog();
            } catch (IOException e) {
                handleException(e, startScreen);
            }

            try {
                closeIndividualWaypointLog();
            } catch (IOException e) {
                handleException(e, startScreen);
            }

            try {
                closeGeoLocator();
            } catch (IOException e) {
                handleException(e, startScreen);
            }
        }
        switchDisplayable(null, startScreen);
    }

    /**
     * Returns an initiliazed instance of sendSMSCommand component.
     * @return the initialized component instance
     */
    public Command getSendSMSCommand() {
        if (sendSMSCommand == null) {
            sendSMSCommand = new Command(GPSLoggerLocalization.getMessage("sendSMSCommand"), Command.OK, 0);
        }
        return sendSMSCommand;
    }

    /**
     * Performs an action assigned to the sendSMS entry-point.
     */
    public void sendSMS() {
        GeoLocation waypoint = getLastWaypoint(); // it was already registered

        if (waypoint == null) {
            return; // nothing to send
        }

        double latitude = waypoint.getLatitude();
        double longitude = waypoint.getLongitude();
        float altitude = waypoint.getAltitude();
        float course = waypoint.getCourse();
        float speed = waypoint.getSpeed();

        String message = waypoint.getTimeString() + " UTC\n"
                + (Double.isNaN(latitude)?
                      ""
                    : (GPSLoggerUtils.convertLatitudeToString(latitude, coordinatesMode) + "\n"))
                + (Double.isNaN(longitude)?
                      ""
                    : (GPSLoggerUtils.convertLongitudeToString(longitude, coordinatesMode) + "\n"))
                + (Float.isNaN(altitude)?
                      ""
                    : (GPSLoggerUtils.convertAltitudeToString(altitude, altitudeUnits) + "\n"))
                + (Float.isNaN(course)?
                      ""
                    : (GPSLoggerUtils.convertCourseToString(course) + "\n"))
                + (Float.isNaN(speed)?
                      ""
                    : (GPSLoggerUtils.convertSpeedToString(speed, speedUnits) + "\n"));

        getSmsTextBox().setString(message);
        switchDisplayable(null, getSmsTextBox());
    }

    /**
     * Returns an initiliazed instance of logFormatChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getLogFormatChoiceGroup() {
        if (logFormatChoiceGroup == null) {
            logFormatChoiceGroup = new ChoiceGroup("Log format", Choice.MULTIPLE);
            logFormatChoiceGroup.append("GPX", null);
            logFormatChoiceGroup.append("KML (Google Earth)", null);
            logFormatChoiceGroup.append("TCX (Garmin Training Center)", null);
            logFormatChoiceGroup.setSelectedFlags(new boolean[] { true, false, false });
        }
        return logFormatChoiceGroup;
    }

    /**
     * Returns an initiliazed instance of logSettingsChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getLogSettingsChoiceGroup() {
        if (logSettingsChoiceGroup == null) {
            logSettingsChoiceGroup = new ChoiceGroup("Log settings", Choice.MULTIPLE);
            logSettingsChoiceGroup.append("Save NMEA data (if available)", null);
            logSettingsChoiceGroup.append("Separate NMEA log", null);
            logSettingsChoiceGroup.append("Separate Waypoint log", null);
            logSettingsChoiceGroup.setSelectedFlags(new boolean[] { true, false, false });
        }
        return logSettingsChoiceGroup;
    }

    /**
     * Returns an initiliazed instance of logUpdateFrequencyTextField component.
     * @return the initialized component instance
     */
    public TextField getLogUpdateFrequencyTextField() {
        if (logUpdateFrequencyTextField == null) {
            logUpdateFrequencyTextField = new TextField("Log update frequency (seconds)", "1", 32, TextField.NUMERIC);
            logUpdateFrequencyTextField.setLayout(ImageItem.LAYOUT_DEFAULT);
        }
        return logUpdateFrequencyTextField;
    }

    /**
     * Returns an initiliazed instance of defaultSmsPhoneNumber component.
     * @return the initialized component instance
     */
    public TextField getDefaultSmsPhoneNumber() {
        if (defaultSmsPhoneNumber == null) {
            defaultSmsPhoneNumber = new TextField("default SMS phone number", "+1", 32, TextField.PHONENUMBER);
        }
        return defaultSmsPhoneNumber;
    }

    /**
     * Returns an initiliazed instance of smsScreen component.
     * @return the initialized component instance
     */
    public Form getSmsScreen() {
        if (smsScreen == null) {
            smsScreen = new Form("Send SMS", new Item[] { getPhoneNumberTextField(), getSmsLengthStringItem(), getSmsTextStringItem() });
            smsScreen.addCommand(getOkSendSMSCommand());
            smsScreen.addCommand(getCancelSendSMSCommand());
            smsScreen.setCommandListener(this);
        }
        return smsScreen;
    }

    /**
     * Returns an initiliazed instance of phoneNumberTextField component.
     * @return the initialized component instance
     */
    public TextField getPhoneNumberTextField() {
        if (phoneNumberTextField == null) {
            phoneNumberTextField = new TextField("Phone number", "", 32, TextField.PHONENUMBER);
        }
        return phoneNumberTextField;
    }

    /**
     * Returns an initiliazed instance of smsTextBox component.
     * @return the initialized component instance
     */
    public TextBox getSmsTextBox() {
        if (smsTextBox == null) {
            smsTextBox = new TextBox("SMS Message", null, 160, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
            smsTextBox.addCommand(getOkSMSEditCommand());
            smsTextBox.addCommand(getCancelSMSEditCommand());
            smsTextBox.setCommandListener(this);
        }
        return smsTextBox;
    }

    /**
     * Returns an initiliazed instance of okSMSEditCommand component.
     * @return the initialized component instance
     */
    public Command getOkSMSEditCommand() {
        if (okSMSEditCommand == null) {
            okSMSEditCommand = new Command("Ok", Command.OK, 0);
        }
        return okSMSEditCommand;
    }

    /**
     * Returns an initiliazed instance of cancelSMSEditCommand component.
     * @return the initialized component instance
     */
    public Command getCancelSMSEditCommand() {
        if (cancelSMSEditCommand == null) {
            cancelSMSEditCommand = new Command("Cancel", Command.CANCEL, 0);
        }
        return cancelSMSEditCommand;
    }

    /**
     * Returns an initiliazed instance of okSendSMSCommand component.
     * @return the initialized component instance
     */
    public Command getOkSendSMSCommand() {
        if (okSendSMSCommand == null) {
            okSendSMSCommand = new Command("Ok", Command.OK, 0);
        }
        return okSendSMSCommand;
    }

    /**
     * Returns an initiliazed instance of cancelSendSMSCommand component.
     * @return the initialized component instance
     */
    public Command getCancelSendSMSCommand() {
        if (cancelSendSMSCommand == null) {
            cancelSendSMSCommand = new Command("Cancel", Command.CANCEL, 0);
        }
        return cancelSendSMSCommand;
    }

    /**
     * Performs an action assigned to the doSendSMS entry-point.
     */
    public void doSendSMS() {

        final String message = getSmsTextBox().getString();
        final String phoneAddress = "sms://" + getPhoneNumberTextField().getString();

        System.out.println("Sending message ("
                + message.length()
                + " characters) to "
                + phoneAddress
                + ":\n---\n"
                + message
                + "\n---\n");

        // run blocking code (SMS sending) in a separate thread
        new Thread() {
            public void run() {
                MessageConnection messageConnection = null;

                try {
                    messageConnection = (MessageConnection)Connector.open(phoneAddress);
                    TextMessage textMessage = (TextMessage)messageConnection.newMessage(MessageConnection.TEXT_MESSAGE);
                    textMessage.setPayloadText(message);
                    messageConnection.send(textMessage);
                    messageConnection.close();
                } catch (Exception e) {
                    handleException(e, getMainScreen());
                }
            }
        }.start();

        switchDisplayable(null, getWaypointScreen());
    }

    /**
     * Returns an initiliazed instance of smsLengthStringItem component.
     * @return the initialized component instance
     */
    public StringItem getSmsLengthStringItem() {
        if (smsLengthStringItem == null) {
            smsLengthStringItem = new StringItem("Message length: ", null);
        }
        return smsLengthStringItem;
    }

    /**
     * Returns an initiliazed instance of smsTextStringItem component.
     * @return the initialized component instance
     */
    public StringItem getSmsTextStringItem() {
        if (smsTextStringItem == null) {
            smsTextStringItem = new StringItem("Message text: ", null);
        }
        return smsTextStringItem;
    }

    /**
     * Performs an action assigned to the switchToMainScreen entry-point.
     */
    public void switchToMainScreen() {
        switchDisplayable(null, getMainScreen());
    }

    /**
     * Returns an initiliazed instance of logFileNamePrefixTextField component.
     * @return the initialized component instance
     */
    public TextField getLogFileNamePrefixTextField() {
        if (logFileNamePrefixTextField == null) {
            logFileNamePrefixTextField = new TextField("Log file name prefix", "GPSLogger", 32, TextField.ANY);
        }
        return logFileNamePrefixTextField;
    }

    /**
     * Returns an initiliazed instance of minimizeCommand component.
     * @return the initialized component instance
     */
    public Command getMinimizeCommand() {
        if (minimizeCommand == null) {
            minimizeCommand = new Command(GPSLoggerLocalization.getMessage("minimizeCommand"), Command.OK, 0);
        }
        return minimizeCommand;
    }

    /**
     * Returns an initiliazed instance of spacer2 component.
     * @return the initialized component instance
     */
    public Spacer getSpacer2() {
        if (spacer2 == null) {
            spacer2 = new Spacer(16, 1);
        }
        return spacer2;
    }

    /**
     * Returns an initiliazed instance of totalMemoryStringItem component.
     * @return the initialized component instance
     */
    public StringItem getTotalMemoryStringItem() {
        if (totalMemoryStringItem == null) {
            totalMemoryStringItem = new StringItem("total memory:", null);
        }
        return totalMemoryStringItem;
    }

    /**
     * Returns an initiliazed instance of freeMemoryStringItem component.
     * @return the initialized component instance
     */
    public StringItem getFreeMemoryStringItem() {
        if (freeMemoryStringItem == null) {
            freeMemoryStringItem = new StringItem("free memory:", null);
        }
        return freeMemoryStringItem;
    }

    /**
     * Returns an initiliazed instance of mapDescriptorFileTextField component.
     * @return the initialized component instance
     */
    public TextField getMapDescriptorFileTextField() {
        if (mapDescriptorFileTextField == null) {
            mapDescriptorFileTextField = new TextField(GPSLoggerLocalization.getMessage("MapDescriptorFile"), "", 4096, TextField.ANY);
            mapDescriptorFileTextField.addCommand(getBrowseMapDescriptorFileCommand());
            mapDescriptorFileTextField.setItemCommandListener(this);
            mapDescriptorFileTextField.setDefaultCommand(getBrowseMapDescriptorFileCommand());
        }
        return mapDescriptorFileTextField;
    }

    /**
     * Returns an initiliazed instance of spacer3 component.
     * @return the initialized component instance
     */
    public Spacer getSpacer3() {
        if (spacer3 == null) {
            spacer3 = new Spacer(16, 1);
        }
        return spacer3;
    }

    /**
     * Returns an initiliazed instance of browseMapDescriptorFileStringItem component.
     * @return the initialized component instance
     */
    public StringItem getBrowseMapDescriptorFileStringItem() {
        if (browseMapDescriptorFileStringItem == null) {
            browseMapDescriptorFileStringItem = new StringItem(GPSLoggerLocalization.getMessage("BrowseMapDescriptorFile"), "", Item.BUTTON);
            browseMapDescriptorFileStringItem.addCommand(getBrowseMapDescriptorFileCommand());
            browseMapDescriptorFileStringItem.setItemCommandListener(this);
            browseMapDescriptorFileStringItem.setDefaultCommand(getBrowseMapDescriptorFileCommand());
            browseMapDescriptorFileStringItem.setFont(getFont());
        }
        return browseMapDescriptorFileStringItem;
    }

    /**
     * Returns an initiliazed instance of browseMapDescriptorFileCommand component.
     * @return the initialized component instance
     */
    public Command getBrowseMapDescriptorFileCommand() {
        if (browseMapDescriptorFileCommand == null) {
            browseMapDescriptorFileCommand = new Command("Ok", Command.OK, 0);
        }
        return browseMapDescriptorFileCommand;
    }

    /**
     * Returns an initiliazed instance of gpsDeviceURLStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getGpsDeviceURLStartScreenStringItem() {
        if (gpsDeviceURLStartScreenStringItem == null) {
            gpsDeviceURLStartScreenStringItem = new StringItem("", "");
        }
        return gpsDeviceURLStartScreenStringItem;
    }

    /**
     * Returns an initiliazed instance of gpsDeviceNameStringItem component.
     * @return the initialized component instance
     */
    public StringItem getGpsDeviceNameStringItem() {
        if (gpsDeviceNameStringItem == null) {
            gpsDeviceNameStringItem = new StringItem("GPS name: ", "");
        }
        return gpsDeviceNameStringItem;
    }

    /**
     * Returns an initiliazed instance of mapDescriptorFileStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getMapDescriptorFileStartScreenStringItem() {
        if (mapDescriptorFileStartScreenStringItem == null) {
            mapDescriptorFileStartScreenStringItem = new StringItem("Map: ", "");
        }
        return mapDescriptorFileStartScreenStringItem;
    }

    /**
     * Performs an action assigned to the browseMapDescriptorFile entry-point.
     */
    public void browseMapDescriptorFile() {
        getDisplay().setCurrent(getMapDescriptorFileBrowser()); // without history
    }

    public FileBrowser getMapDescriptorFileBrowser() {
        if (mapDescriptorFileBrowser == null) {
            mapDescriptorFileBrowser = new FileBrowser(getDisplay(),
                                        null,
                                        false, true); // files only
            mapDescriptorFileBrowser.setTitle("Select map descriptor");
            mapDescriptorFileBrowser.setCommandListener(new CommandListener() {
                public void commandAction(Command command, Displayable d) {
                    if (command == FileBrowser.SELECT_ITEM_COMMAND) {
                        pasteMapDescriptorFileToTextField();
                    } else if (command == backCommand) {
                        goToSettingsScreen(); // without history
                    }
                }
            });
            mapDescriptorFileBrowser.addCommand(FileBrowser.SELECT_ITEM_COMMAND);
            mapDescriptorFileBrowser.addCommand(getBackCommand ());
        }
        // this should be done here because current path may have changed since the object creation
        mapDescriptorFileBrowser.setCurrentURL(mapDescriptorFileTextField.getString());
        return mapDescriptorFileBrowser;
    }

    public FileBrowser getLogFolderBrowser() {
        if (logFolderBrowser == null) {
            logFolderBrowser = new FileBrowser(getDisplay(),
                                    null,
                                    true, false); // folders only
            logFolderBrowser.setTitle("Select log folder");
            logFolderBrowser.setCommandListener (new CommandListener() {
                public void commandAction(Command command, Displayable d) {
                    if (command == FileBrowser.SELECT_ITEM_COMMAND) {
                        pasteLogFolderToTextField();
                    } else if (command == backCommand) {
                        goToSettingsScreen(); // without history
                    }
                }
            });
            logFolderBrowser.addCommand(FileBrowser.SELECT_ITEM_COMMAND);
            logFolderBrowser.addCommand(getBackCommand ());
        }
        // this should be done here because current path may have changed since the object creation
        logFolderBrowser.setCurrentURL(logFolderTextField.getString());
        return logFolderBrowser;
    }

    /**
     * The GPSLogger constructor.
     */
    public GPSLogger() {
        midlet = this;
    }

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay () {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        destroyApp(false); // give a chance to prevent exit (by throwing MIDletStateChangeException)
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        
        if (midletPaused) {
            System.out.println("\nThe midlet was resumed\n");
            resumeMIDlet ();
        } else {
            System.out.println("\nThe midlet was started\n");
            initialize ();
            startMIDlet ();
        }
        
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
        System.out.println("\nThe midlet was paused.\n");
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {

        mustBeTerminated = true;

        // try to exit gracefully, make sure all connections are closed
        // and our precious data is not lost
        try {
            closeTrackLogFile();
        } catch (IOException e) {
            // too late to handle this now...
        }

        try {
            closeIndividualWaypointLog();
        } catch (IOException e) {
            // too late to handle this now...
        }

        try {
            closeGeoLocator();
        } catch (IOException e) {
            // too late to handle this now...
        }

        switchDisplayable(null, null);
        notifyDestroyed();
    }
    
    public int getCoordinatesMode() {
        return coordinatesMode;
    }
    
    public int getAltitudeUnits() {
        return altitudeUnits;
    }

    public int getSpeedUnits() {
        return speedUnits;
    }

    public GPSScreen getMainScreen() {
        return mainScreen;
    }

    public MapConfiguration getMapConfiguration() {
        return mapConfiguration;
    }

    public static GPSLoggerSettings getSettings() {
        if (settings == null) {
            settings = new GPSLoggerSettings(midlet);
        }
        return settings;
    }

    void loadSettings() {
        if (settings == null) {
            settings = new GPSLoggerSettings(this);
        }
        
        try {
            settings.load();
        } catch (Exception e) {
            e.printStackTrace();

            // just ignore this time
        }
        
        // initialize the settings screen with data from the loaded configuration
        getGpsDeviceURLTextField().setString(settings.getGPSDeviceURL());
        getGpsDeviceNameStringItem().setText(settings.getGPSDeviceName());

        getLogFolderTextField().setString(settings.getLogFolder());
        String loadedLogFileNamePrefix = settings.getLogFilePrefix();
        getLogFileNamePrefixTextField().setString
                ((loadedLogFileNamePrefix == null || loadedLogFileNamePrefix.equals(""))?
                      "GPSLogger"
                    : loadedLogFileNamePrefix);

        getMapDescriptorFileTextField().setString(settings.getMapDescriptorFilePath());

        coordinatesMode = settings.getCoordinatesMode();
        getCoordinatesModeChoiceGroup().setSelectedIndex(coordinatesMode, true);

        altitudeUnits = settings.getAltitudeUnits();
        getAltitudeUnitsChoiceGroup().setSelectedIndex(altitudeUnits, true);

        speedUnits = settings.getSpeedUnits();
        getSpeedUnitsChoiceGroup().setSelectedIndex(speedUnits, true);

        getPhoneNumberTextField().setString(settings.getDefaultSmsPhoneNumber());
        getDefaultSmsPhoneNumber().setString(settings.getDefaultSmsPhoneNumber());
    }

    void prepareStartScreen() {
        
        boolean mustConfigure = false;

        String gpsDeviceName = settings.getGPSDeviceName();
        String gpsDeviceURLString = settings.getGPSDeviceURL();
            
        if (gpsDeviceName != null) {
            gpsDeviceNameStartScreenStringItem.setText(gpsDeviceName);
        }

        if (gpsDeviceURLString != null) {
            gpsDeviceURLStartScreenStringItem.setText(gpsDeviceURLString);
        }
        
/// LOCALIZE:
        
        // empty URL? should we use built-in GPS receiver?
        if (gpsDeviceURLString == null
            || gpsDeviceURLStartScreenStringItem.getText().trim().equals("")) {
            gpsDeviceNameStartScreenStringItem.setText("[built-in]");
            gpsDeviceURLStartScreenStringItem.setText("[use Location API (JSR-179)]");
        } else if (gpsDeviceName == null) { // URL given but no name?
            gpsDeviceNameStartScreenStringItem.setText("[name available after re-scan]");
        }

        final String logPath = settings.getLogFolder();
        
        if (logPath != null) {
            logPathStartScreenStringItem.setText(logPath);
        } else {
            logPathStartScreenStringItem.setText("[not configured]");
            mustConfigure = true;
        }
        
        String mapDescriptorFilePath = settings.getMapDescriptorFilePath();
        if (mapDescriptorFilePath != null) {
            mapDescriptorFileStartScreenStringItem.setText(mapDescriptorFilePath);
        } else {
            mapDescriptorFileStartScreenStringItem.setText("[not specified]");
        }

        if (mustConfigure) {
            getStartScreen().removeCommand(getStartCommand()); // we cannot start unless we configure the settings
        } else { // everything is already configured
            getStartScreen().addCommand(getStartCommand()); // we cannot start unless we configure the settings
        }
        
        Thread spaceCalculatorThread = new Thread() {
            public void run() {
                FileConnection fileConnection = null;
                long availableSpace = -1L;

                try {
                    fileConnection = (FileConnection)Connector.open(logPath, Connector.READ); // check space only
                    availableSpace = fileConnection.availableSize();
                } catch (Exception e) {
                    availableSpace = -1L; // :-(
                } finally {
                    if (fileConnection != null) {
                        try {
                            fileConnection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            // just ignore?
                        }
                    }
                }
                
                if (availableSpace >= 0) {
                    freeSpaceStartScreenStringItem.setText("(" + (availableSpace / 1048576L)+ " MiB free)");
                } else { // negative means info was N/A
                    freeSpaceStartScreenStringItem.setText("(free space is unknown)");
                }
            }
        };

        spaceCalculatorThread.start();
    }

    void startLogging(Displayable screen) {
        
        String connectionURLString = settings.getGPSDeviceURL();
        System.out.println("Starting tracking, connection URL = " + connectionURLString);
        
///reconnect automatically on errors (user-customizable behaviour?)
        
        try {
            do { // (re)connect loop
                screen.setTitle("Starting...");
                
                System.out.println(getMainScreen().getTitle());
        
                synchronized (trackLock) {
                    if (geoLocator == null) { // connect to the GPS if not connected yet
                        boolean tryJSR179 = false;
                        if (connectionURLString != null) {
                            if (!connectionURLString.trim().equals("")) {
                                geoLocator = new BluetoothGeoLocator(connectionURLString);
                            } else {
                                tryJSR179 = true;
                            }
                        } else { // no bluetooth device was specified: try Location API provider
                            tryJSR179 = true;
                        }

                        if (tryJSR179) {
                            // check JSR-179 availability on this device
                            try {
                                Class.forName("javax.microedition.location.LocationProvider");
                                // if we went as far as here, the JSR-179 is supported alright

                                // next, try to instantiate our JSR-179 GeoLocator "on the fly",
                                // to prevent some JVMs (e.g. Nokia 3610f) from crashing
                                // when trying to reference the missing javax.microedition.location
                                // classes
                                geoLocator = (GeoLocator)
                                        (Class.forName("com.sergebass.geo.JSR179GeoLocator")
                                         .newInstance());
                            } catch (ClassNotFoundException e) { // no JSR-179 support on this device?
                                e.printStackTrace();
                            }
                        }
                    }
                }
            
                if (geoLocator == null) { // still no location provider??
                    throw new Exception("No valid geolocation source found!");
                }

                startTrackLog();

                screen.setTitle(null); // remove the title when started

                resetOdometer();
                
                // show initial screen
                getMainScreen().setMessage("Acquiring location, please wait...");
                getMainScreen().repaint();

                // we will start receiving notifications after this call:
                geoLocator.setLocationListener(this);

                do {
                    Thread.sleep(200);
                } while (!mustBeTerminated); // or until user decides to quit?
                
            } while (false); /// fix this: reconnect?
            
        } catch (Exception e) {
            handleException(e, startScreen);
        }
    }
    
    void handleException(Throwable e, Displayable displayable) {

        e.printStackTrace();

        Instant errorInstant = new Instant();
        
        vibrateSOSRhythm(3);
        
        if (errorPlayer != null) {
            try {
                errorPlayer.start();
            } catch (Exception ee) {
                ee.printStackTrace();
                // ignore the rest
            }
        }

        if (errorPlayer.getDuration() != Player.TIME_UNKNOWN) {
            try {
                // convert microseconds to milliseconds
                Thread.sleep(errorPlayer.getDuration() / 1000L);
            } catch (InterruptedException ee) {
                ee.printStackTrace();
            }
        }

        getErrorAlert().setTitle("Error!");
        getErrorAlert().setString(errorInstant.getISO8601TimeId()
                                  + "\n"
                                  + e.getMessage()
                                  + "\n("
                                  + e.getClass().getName()
                                  + ")");
       
        getDisplay().setCurrent(errorAlert, displayable);
    }
    
    public void markWaypoint() {
        registerCurrentWaypoint(); // save the location immediately!
        switchDisplayable(null, getWaypointScreen()); // let the user edit it
    }

    void registerCurrentWaypoint() {
        if (geoLocator != null) {
            registerWaypoint(geoLocator.getLocation());
        }
    }

    void registerWaypoint(GeoLocation waypoint) {

        if (waypoints == null) {
            waypoints = new Vector(); // make sure the registry is there
        }

        // add this waypoint to our log (it will also be written out at the end of the track)
        waypoints.addElement(waypoint);
    }

    GeoLocation getLastWaypoint() {
        if (waypoints != null) {
            if (waypoints.size() > 0) {
                return (GeoLocation)waypoints.elementAt(waypoints.size() - 1);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    void saveCurrentWaypoint(String comments)
                throws IOException {
        
        GeoLocation waypoint = getLastWaypoint(); // it was already registered

        if (waypoint == null) {
            return; // nothing to save
        }

        waypoint.setName(comments);

        synchronized (waypointLogLock) {
            if (waypointLogFile == null) { // if no log file is being written, make a connection
                String logFolder = settings.getLogFolder();
                if (!logFolder.endsWith("/")) {
                    logFolder = logFolder + "/"; // make sure we have the trailer here
                }

                Instant now = new Instant(); // reflect the time in the log file name

                String logFileNamePrefix = getLogFileNamePrefixTextField().getString().trim();
                String logFilePath = logFolder
                    + (logFileNamePrefix.equals("")? "GPSLogger" : logFileNamePrefix)
                    + "-"
                    + now.getDateId()
                    + "-"
                    + now.getTimeId()
                    + "-waypoints.gpx";
                waypointLogFile = new GPSLogFile(logFilePath);
                waypointLogWriter = new GPXWriter(waypointLogFile.getOutputStream());

                // add platform name to the log, to make logs more easily identifiable
                String platformName = System.getProperty("microedition.platform");
                String deviceModel = System.getProperty("device.model"); // for Motorola phones?

                String waypointLogHeader = "GPSLogger waypoints (" + now.getISO8601UTCDateTimeId() + ")"
                        + (platformName != null? ", " + platformName : "")
                        + (deviceModel != null? ", " + deviceModel : "");

                waypointLogWriter.writeHeader(waypointLogHeader);
                waypointLogWriter.write("\n");
            }

            if (waypointLogWriter != null) {
                waypointLogWriter.writeWaypoint(waypoint);
                waypointLogWriter.write("\n");
            }
        } // synchronized (waypointLogLock)
    }

    void closeIndividualWaypointLog()
            throws IOException {

        synchronized (waypointLogLock) {
            if (waypointLogWriter != null) {
                waypointLogWriter.writeFooter();
                waypointLogWriter.flush();
                waypointLogWriter.close();
                waypointLogWriter = null;
            }

            if (waypointLogFile != null) {
                waypointLogFile.close();
                waypointLogFile = null;
            }
        } // synchronized (waypointLogLock)
    }
    
    void startTrackLog()
            throws IOException {

        synchronized (trackLogLock) {
            Instant now = new Instant(); // reflect the start time in the log file name

            if (trackLogFile == null) { // if no log file is being written, make a connection
                String logFolder = settings.getLogFolder();
                if (!logFolder.endsWith("/")) {
                    logFolder = logFolder + "/"; // make sure we have the trailer here
                }

                String logFileNamePrefix = getLogFileNamePrefixTextField().getString().trim();
                String logFilePath = logFolder
                    + (logFileNamePrefix.equals("")? "GPSLogger" : logFileNamePrefix)
                    + "-"
                    + now.getDateId()
                    + "-"
                    + now.getTimeId()
                    + ".gpx";
                trackLogFile = new GPSLogFile(logFilePath);
            }

            if (trackLogWriter == null) {
                trackLogWriter = new GPXWriter(trackLogFile.getOutputStream());

                // add platform name to the log, to make logs more easily identifiable
                String javaPlatformName = System.getProperty("microedition.platform");
                String phoneDeviceModel = System.getProperty("device.model"); // for Motorola phones?
                String gpsDeviceName = settings.getGPSDeviceName();
                
                String trackLogHeader = "GPSLogger track log (" + now.getISO8601UTCDateTimeId() + ")"
                        + (javaPlatformName != null? ", " + javaPlatformName : "")
                        + (phoneDeviceModel != null? ", " + phoneDeviceModel : "")
                        + (gpsDeviceName != null? ", GPS: " + gpsDeviceName : "");

                trackLogWriter.writeHeader(trackLogHeader);
            }


            // either way, let's create a new track:
            trackLogWriter.writeTrackHeader("GPSLogger track (" + now.getISO8601UTCDateTimeId() + ")");
            trackLogWriter.write("\n");
            isTrackStarted = true;

            trackLogWriter.writeTrackSegmentHeader();
            isTrackSegmentStarted = true;
        } // synchronized (trackLogLock)
    }

    void finishTrackLog()
            throws IOException {

        synchronized (trackLogLock) {
            if (trackLogWriter != null) {
                if (isTrackSegmentStarted) {
                    trackLogWriter.writeTrackSegmentFooter();
                    trackLogWriter.write("\n");
                    isTrackSegmentStarted = false;
                }

                if (isTrackStarted) {
                    trackLogWriter.writeTrackFooter();
                    trackLogWriter.write("\n");
                    isTrackStarted = false;
                }

                // save all of the remembered waypoints in the general track log,
                // after the track data:
                if (waypoints != null) {
                    int waypointCount = waypoints.size();
                    for (int i = 0; i < waypointCount; i++) {
                        GeoLocation waypoint = (GeoLocation)(waypoints.elementAt(i));
                        trackLogWriter.writeWaypoint(waypoint);
                        trackLogWriter.write("\n");
                    }
                    waypoints.removeAllElements(); // clean it up, it's saved
                }

                trackLogWriter.flush();
            }
        } // synchronized (trackLogLock)
    }
    
    void closeTrackLogFile()
            throws IOException {

        // make sure our log is undamaged
        finishTrackLog();

        synchronized (trackLogLock) {
            if (trackLogWriter != null) {
                trackLogWriter.writeFooter();
                trackLogWriter.flush();
                trackLogWriter.close();
                trackLogWriter = null;
            }

            if (trackLogFile != null) {
                trackLogFile.close();
                trackLogFile = null;
            }
        } // synchronized (trackLogLock)
    }

    void closeGeoLocator()
            throws IOException {
        synchronized (geoLocatorLock) {
            if (geoLocator != null) {
                geoLocator.setLocationListener(null);
                geoLocator.close();
                geoLocator = null;
            }
        } // synchronized (geoLocatorLock)
    }


    public void vibrateSOSRhythm(final int times) {
        // since Morse vibrator is blocking, invoke it in a separate thread
        final Display thisDisplay = Display.getDisplay(this);
        new Thread() {
            public void run() {
                MorseVibrator vibrator = new MorseVibrator(thisDisplay);
                for (int i = 0; i < times; i++) {
                    vibrator.vibrateMorseCode("SOS");
                }
            }
        }.start();
    }

    public void vibrateSuccessRhythm(int times) {
        new Vibrator(Display.getDisplay(this)).vibrate
                (new int[] { 300, 100, 300, 100, 100, 100, 100, 100, 300, 300,
                             100, 100, 100, 100, 100, 100, 100, 300,
                             100, 100, 300, 700
                            },
                 times);
    }
    
    public void onLocationUpdated(GeoLocation location) {

        if (location == null) {

            System.out.println("Null location received!");

            getMainScreen().setLocation(null);
            getMainScreen().setMessage("(Invalid location data)");
            getMainScreen().repaint();
            
        } else { // non-null location
            
            System.out.println(location);

            synchronized (trackLogLock) {
                if (trackLogWriter != null) {
                    try {
                        trackLogWriter.writeTrackpoint(location);
                    } catch (IOException e) {
                        handleException(e, getStartScreen());
                    }
                }
            }

            getMainScreen().setLocation(location);
            getMainScreen().setMessage(null);
            
            long totalTimeMillis = System.currentTimeMillis() - startTimeMillis;
            Instant totalTime = new Instant(totalTimeMillis);
            getMainScreen().setTotalTime("+" + totalTime.getISO8601UTCTimeId());
            
            getMainScreen().repaint();
        }
    }

    public void onLocatorStateChanged(int newState) {

        System.out.println("Locator state changed to " + newState);
        
        switch (newState) {
            case GeoLocator.STATE_AVAILABLE:
                break;
            case GeoLocator.STATE_OUT_OF_SERVICE:
                getMainScreen().setLocationValid(false);
                getMainScreen().repaint();
                break;
            case GeoLocator.STATE_TEMPORARILY_UNAVAILABLE:
                getMainScreen().setLocationValid(false);
                getMainScreen().repaint();
                break;
        }
    }

    public void onLocatorException(Exception e) {

        stopTrack();
        
        if (e.getClass().getName().equals(GeoLocatorException.class.getName())) {
///??? should we reconnect here automatically??
        }

        handleException(e, getStartScreen());
    }

    public void settingsChanged(Settings settings, Object key, Object newValue) {

        String parameter = (String) key;
        String value = (String)newValue;

        if (parameter.equals(GPSLoggerSettings.COORDINATES_MODE)) {
            coordinatesMode = Integer.parseInt(value);
        } else if (parameter.equals(GPSLoggerSettings.ALTITUDE_UNITS)) {
            altitudeUnits = Integer.parseInt(value);
        } else if (parameter.equals(GPSLoggerSettings.SPEED_UNITS)) {
            speedUnits = Integer.parseInt(value);
        }
    }
}
