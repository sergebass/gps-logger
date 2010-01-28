/*
 * (C) Serge Perinsky, 2007, 2008, 2009
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

    FileBrowser fileBrowser;
        
    Player errorPlayer = null;

    GPSScreen mainScreen = null;
    MapConfiguration mapConfiguration = null;

    long startTimeMillis = 0L;

    int coordinatesMode = 0;
    int altitudeUnits = 0;
    int speedUnits = 0;

    Canvas cameraCanvas = null;

    //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
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
    private Command browseCommand;
    private Command markWaypointCommand;
    private Command cancelCommand;
    private Command sendEmailCommand;
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
    private StringItem stringItem1;
    private StringItem emailItem;
    private Alert errorAlert;
    private Font font;
    private Font boldFont;
    //</editor-fold>//GEN-END:|fields|0|

    //<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
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
    //</editor-fold>//GEN-END:|methods|0|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
    /**
     * Initilizes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
        try {
            errorPlayer = Manager.createPlayer(getClass().getResourceAsStream(ERROR_AUDIO_FILE), "audio/x-wav");
            errorPlayer.prefetch();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore?
        }
//GEN-LINE:|0-initialize|1|0-postInitialize
        // write post-initialize user code here
    }//GEN-BEGIN:|0-initialize|2|
    //</editor-fold>//GEN-END:|0-initialize|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction

        loadSettings();
        switchDisplayable(null, getStartScreen());//GEN-LINE:|3-startMIDlet|1|3-postAction
        prepareStartScreen();
    }//GEN-BEGIN:|3-startMIDlet|2|
    //</editor-fold>//GEN-END:|3-startMIDlet|2|
    
    //<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction

//GEN-LINE:|4-resumeMIDlet|1|4-postAction
        // write post-action user code here
    }//GEN-BEGIN:|4-resumeMIDlet|2|
    //</editor-fold>//GEN-END:|4-resumeMIDlet|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
        // write pre-switch user code here
        Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
        Displayable __currentDisplayable = display.getCurrent();
        if (__currentDisplayable != null  &&  nextDisplayable != null) {
            __previousDisplayables.put(nextDisplayable, __currentDisplayable);
        }
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }//GEN-END:|5-switchDisplayable|1|5-postSwitch
        // write post-switch user code here
    }//GEN-BEGIN:|5-switchDisplayable|2|
    //</editor-fold>//GEN-END:|5-switchDisplayable|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
        if (displayable == gpsDeviceList) {//GEN-BEGIN:|7-commandAction|1|87-preAction
            if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|1|87-preAction
                stopGPSDeviceSearch();
                gpsDeviceListAction();//GEN-LINE:|7-commandAction|2|87-postAction
                // write post-action user code here
            } else if (command == cancelCommand) {//GEN-LINE:|7-commandAction|3|217-preAction
                stopGPSDeviceSearch();
//GEN-LINE:|7-commandAction|4|217-postAction
                getDisplay().setCurrent(getSettingsScreen()); // go back without history
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|5|342-preAction
                stopGPSDeviceSearch();
                gpsDeviceListAction();//GEN-LINE:|7-commandAction|6|342-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|7|150-preAction
        } else if (displayable == helpScreen) {
            if (command == backCommand) {//GEN-END:|7-commandAction|7|150-preAction
                // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|8|150-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|9|215-preAction
        } else if (displayable == settingsScreen) {
            if (command == cancelCommand) {//GEN-END:|7-commandAction|9|215-preAction

                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|10|215-postAction
                // write post-action user code here
            } else if (command == saveSettingsCommand) {//GEN-LINE:|7-commandAction|11|202-preAction

                saveSettings();//GEN-LINE:|7-commandAction|12|202-postAction
                // only show summary if the last (next) displayable is introForm
                if (__previousDisplayables.get(getDisplay().getCurrent()) == startScreen) {
                    prepareStartScreen();
                }
                switchToPreviousDisplayable();
            }//GEN-BEGIN:|7-commandAction|13|329-preAction
        } else if (displayable == smsScreen) {
            if (command == cancelSendSMSCommand) {//GEN-END:|7-commandAction|13|329-preAction
                // write pre-action user code here
                switchDisplayable(null, getSmsTextBox());//GEN-LINE:|7-commandAction|14|329-postAction
                // write post-action user code here
            } else if (command == okSendSMSCommand) {//GEN-LINE:|7-commandAction|15|327-preAction
                // write pre-action user code here
                doSendSMS();//GEN-LINE:|7-commandAction|16|327-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|17|323-preAction
        } else if (displayable == smsTextBox) {
            if (command == cancelSMSEditCommand) {//GEN-END:|7-commandAction|17|323-preAction
                // write pre-action user code here
                switchDisplayable(null, getWaypointScreen());//GEN-LINE:|7-commandAction|18|323-postAction
                // write post-action user code here
            } else if (command == okSMSEditCommand) {//GEN-LINE:|7-commandAction|19|321-preAction
                String messageText = getSmsTextBox().getString();
                getSmsLengthStringItem().setText(messageText.length() + " characters");
                getSmsTextStringItem().setText(messageText);
                // copy phone number from settings (if the field is (still) empty)
                if (getPhoneNumberTextField().getString().trim().equals("")) {
                    getPhoneNumberTextField().setString(settings.getDefaultSmsPhoneNumber());
                }
                switchDisplayable(null, getSmsScreen());//GEN-LINE:|7-commandAction|20|321-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|21|128-preAction
        } else if (displayable == startScreen) {
            if (command == exitCommand) {//GEN-END:|7-commandAction|21|128-preAction
                new Thread() {
                    public void run() {
                        exitMIDlet();//GEN-LINE:|7-commandAction|22|128-postAction
                    }
                }.start();
            } else if (command == helpCommand) {//GEN-LINE:|7-commandAction|23|146-preAction
                // write pre-action user code here
                switchDisplayable(null, getHelpScreen());//GEN-LINE:|7-commandAction|24|146-postAction
                // write post-action user code here
            } else if (command == minimizeCommand) {//GEN-LINE:|7-commandAction|25|348-preAction
                // write pre-action user code here
//GEN-LINE:|7-commandAction|26|348-postAction
                // write post-action user code here
            } else if (command == settingsCommand) {//GEN-LINE:|7-commandAction|27|153-preAction
                // write pre-action user code here
                switchDisplayable(null, getSettingsScreen());//GEN-LINE:|7-commandAction|28|153-postAction
                // write post-action user code here
            } else if (command == startCommand) {//GEN-LINE:|7-commandAction|29|143-preAction
                // write pre-action user code here
                startTrack();//GEN-LINE:|7-commandAction|30|143-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|31|282-preAction
        } else if (displayable == waypointScreen) {
            if (command == cancelWaypointCommand) {//GEN-END:|7-commandAction|31|282-preAction
                // remove the last added waypoint: it was cancelled
                if (waypoints.size() > 1) {
                    waypoints.removeElementAt(waypoints.size() - 1);
                }
                switchToMainScreen();//GEN-LINE:|7-commandAction|32|282-postAction

            } else if (command == saveWaypointCommand) {//GEN-LINE:|7-commandAction|33|284-preAction
                new Thread() {
                    public void run() {
                        saveWaypoint();//GEN-LINE:|7-commandAction|34|284-postAction
                    }
                }.start();
            } else if (command == sendSMSCommand) {//GEN-LINE:|7-commandAction|35|299-preAction
                new Thread() {
                    public void run() {
                        sendSMS();//GEN-LINE:|7-commandAction|36|299-postAction
                    }
                }.start();
            } else if (command == takePhotoCommand) {//GEN-LINE:|7-commandAction|37|291-preAction
                new Thread() {
                    public void run() {
                        takePhoto();//GEN-LINE:|7-commandAction|38|291-postAction
                    }
                }.start();
            }//GEN-BEGIN:|7-commandAction|39|7-postCommandAction
        }//GEN-END:|7-commandAction|39|7-postCommandAction
        else if (displayable == fileBrowser) {
            if (command == FileBrowser.SELECT_ITEM_COMMAND) {
                setLogFolder ();
            } else if (command == backCommand) {
                getDisplay().setCurrent(getSettingsScreen()); // without history
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
    }//GEN-BEGIN:|7-commandAction|40|
    //</editor-fold>//GEN-END:|7-commandAction|40|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|18-getter|0|18-preInit
    /**
     * Returns an initiliazed instance of exitCommand component.
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {//GEN-END:|18-getter|0|18-preInit
            // write pre-init user code here
            exitCommand = new Command(GPSLoggerLocalization.getMessage("exitCommand"), Command.EXIT, 4);//GEN-LINE:|18-getter|1|18-postInit
            // write post-init user code here
        }//GEN-BEGIN:|18-getter|2|
        return exitCommand;
    }
    //</editor-fold>//GEN-END:|18-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: searchDevices ">//GEN-BEGIN:|82-entry|0|83-preAction
    /**
     * Performs an action assigned to the searchDevices entry-point.
     */
    public void searchDevices() {//GEN-END:|82-entry|0|83-preAction
        getGpsDeviceList().deleteAll();
        gpsDeviceEnumerator = new GPSLoggerBTGPSEnumerator(this, getGpsDeviceList());
        gpsDeviceEnumerator.start();
//GEN-LINE:|82-entry|1|83-postAction
        // jump to the discovered bluetooth device list (without history)
        getDisplay().setCurrent(getGpsDeviceList());
    }//GEN-BEGIN:|82-entry|2|
    //</editor-fold>//GEN-END:|82-entry|2|

    void stopGPSDeviceSearch() {
        if (gpsDeviceEnumerator != null) {
            gpsDeviceEnumerator.cancel();
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: gpsDeviceList ">//GEN-BEGIN:|85-getter|0|85-preInit
    /**
     * Returns an initiliazed instance of gpsDeviceList component.
     * @return the initialized component instance
     */
    public List getGpsDeviceList() {
        if (gpsDeviceList == null) {//GEN-END:|85-getter|0|85-preInit
            // write pre-init user code here
            gpsDeviceList = new List("GPS devices", Choice.IMPLICIT);//GEN-BEGIN:|85-getter|1|85-postInit
            gpsDeviceList.addCommand(getCancelCommand());
            gpsDeviceList.addCommand(getOkCommand());
            gpsDeviceList.setCommandListener(this);
            gpsDeviceList.setFitPolicy(Choice.TEXT_WRAP_ON);
            gpsDeviceList.setSelectCommand(getOkCommand());//GEN-END:|85-getter|1|85-postInit
            // write post-init user code here
        }//GEN-BEGIN:|85-getter|2|
        return gpsDeviceList;
    }
    //</editor-fold>//GEN-END:|85-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: gpsDeviceListAction ">//GEN-BEGIN:|85-action|0|85-preAction
    /**
     * Performs an action assigned to the selected list element in the gpsDeviceList component.
     */
    public void gpsDeviceListAction() {//GEN-END:|85-action|0|85-preAction

        String __selectedString = getGpsDeviceList().getString(getGpsDeviceList().getSelectedIndex());//GEN-LINE:|85-action|1|85-postAction
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
    }//GEN-BEGIN:|85-action|2|
    //</editor-fold>//GEN-END:|85-action|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: searchCommand ">//GEN-BEGIN:|101-getter|0|101-preInit
    /**
     * Returns an initiliazed instance of searchCommand component.
     * @return the initialized component instance
     */
    public Command getSearchCommand() {
        if (searchCommand == null) {//GEN-END:|101-getter|0|101-preInit
            // write pre-init user code here
            searchCommand = new Command(GPSLoggerLocalization.getMessage("searchCommand"), GPSLoggerLocalization.getMessage("searchCommand"), Command.OK, 3);//GEN-LINE:|101-getter|1|101-postInit
            // write post-init user code here
        }//GEN-BEGIN:|101-getter|2|
        return searchCommand;
    }
    //</editor-fold>//GEN-END:|101-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: startTrack ">//GEN-BEGIN:|108-entry|0|109-preAction
    /**
     * Performs an action assigned to the startTrack entry-point.
     */
    public void startTrack() {//GEN-END:|108-entry|0|109-preAction
        // run our tracking stuff in a separate thread
        new Thread() {
            public void run() {
                startTrackingThread();
            }
        }.start();
//GEN-LINE:|108-entry|1|109-postAction
    }//GEN-BEGIN:|108-entry|2|
    //</editor-fold>//GEN-END:|108-entry|2|
    
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
    
    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: startScreen ">//GEN-BEGIN:|125-getter|0|125-preInit
    /**
     * Returns an initiliazed instance of startScreen component.
     * @return the initialized component instance
     */
    public Form getStartScreen() {
        if (startScreen == null) {//GEN-END:|125-getter|0|125-preInit
            // write pre-init user code here
            startScreen = new Form("GPS Logger", new Item[] { getGpsDeviceNameStartScreenStringItem(), getGpsDeviceURLStartScreenStringItem(), getLogPathStartScreenStringItem(), getFreeSpaceStartScreenStringItem(), getMapDescriptorFileStartScreenStringItem() });//GEN-BEGIN:|125-getter|1|125-postInit
            startScreen.addCommand(getStartCommand());
            startScreen.addCommand(getSettingsCommand());
            startScreen.addCommand(getExitCommand());
            startScreen.addCommand(getHelpCommand());
            startScreen.addCommand(getMinimizeCommand());
            startScreen.setCommandListener(this);//GEN-END:|125-getter|1|125-postInit

        }//GEN-BEGIN:|125-getter|2|
        return startScreen;
    }
    //</editor-fold>//GEN-END:|125-getter|2|
   
    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: startCommand ">//GEN-BEGIN:|142-getter|0|142-preInit
    /**
     * Returns an initiliazed instance of startCommand component.
     * @return the initialized component instance
     */
    public Command getStartCommand() {
        if (startCommand == null) {//GEN-END:|142-getter|0|142-preInit
            // write pre-init user code here
            startCommand = new Command(GPSLoggerLocalization.getMessage("startCommand"), GPSLoggerLocalization.getMessage("startCommand"), Command.OK, 0);//GEN-LINE:|142-getter|1|142-postInit
            // write post-init user code here
        }//GEN-BEGIN:|142-getter|2|
        return startCommand;
    }
    //</editor-fold>//GEN-END:|142-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: helpCommand ">//GEN-BEGIN:|145-getter|0|145-preInit
    /**
     * Returns an initiliazed instance of helpCommand component.
     * @return the initialized component instance
     */
    public Command getHelpCommand() {
        if (helpCommand == null) {//GEN-END:|145-getter|0|145-preInit
            // write pre-init user code here
            helpCommand = new Command(GPSLoggerLocalization.getMessage("helpCommand"), Command.HELP, 3);//GEN-LINE:|145-getter|1|145-postInit
            // write post-init user code here
        }//GEN-BEGIN:|145-getter|2|
        return helpCommand;
    }
    //</editor-fold>//GEN-END:|145-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: backCommand ">//GEN-BEGIN:|149-getter|0|149-preInit
    /**
     * Returns an initiliazed instance of backCommand component.
     * @return the initialized component instance
     */
    public Command getBackCommand() {
        if (backCommand == null) {//GEN-END:|149-getter|0|149-preInit
            // write pre-init user code here
            backCommand = new Command(GPSLoggerLocalization.getMessage("backCommand"), Command.BACK, 1);//GEN-LINE:|149-getter|1|149-postInit
            // write post-init user code here
        }//GEN-BEGIN:|149-getter|2|
        return backCommand;
    }
    //</editor-fold>//GEN-END:|149-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: settingsScreen ">//GEN-BEGIN:|141-getter|0|141-preInit
    /**
     * Returns an initiliazed instance of settingsScreen component.
     * @return the initialized component instance
     */
    public Form getSettingsScreen() {
        if (settingsScreen == null) {//GEN-END:|141-getter|0|141-preInit
            // write pre-init user code here
            settingsScreen = new Form(GPSLoggerLocalization.getMessage("Settings"), new Item[] { getGpsDeviceNameStringItem(), getGpsDeviceURLTextField(), getSearchGPSStringItem(), getSpacer1(), getLogFolderTextField(), getBrowseLogFolderStringItem(), getSpacer(), getLogFileNamePrefixTextField(), getLogUpdateFrequencyTextField(), getLogFormatChoiceGroup(), getLogSettingsChoiceGroup(), getSpacer3(), getMapDescriptorFileTextField(), getBrowseMapDescriptorFileStringItem(), getCoordinatesModeChoiceGroup(), getAltitudeUnitsChoiceGroup(), getSpeedUnitsChoiceGroup(), getLanguageChoiceGroup(), getDefaultSmsPhoneNumber() });//GEN-BEGIN:|141-getter|1|141-postInit
            settingsScreen.addCommand(getSaveSettingsCommand());
            settingsScreen.addCommand(getCancelCommand());
            settingsScreen.setCommandListener(this);//GEN-END:|141-getter|1|141-postInit
            // write post-init user code here
        }//GEN-BEGIN:|141-getter|2|
        return settingsScreen;
    }
    //</editor-fold>//GEN-END:|141-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: helpScreen ">//GEN-BEGIN:|147-getter|0|147-preInit
    /**
     * Returns an initiliazed instance of helpScreen component.
     * @return the initialized component instance
     */
    public Form getHelpScreen() {
        if (helpScreen == null) {//GEN-END:|147-getter|0|147-preInit
            // write pre-init user code here
            helpScreen = new Form("Help", new Item[] { getStringItem1(), getEmailItem(), getSpacer2(), getTotalMemoryStringItem(), getFreeMemoryStringItem() });//GEN-BEGIN:|147-getter|1|147-postInit
            helpScreen.addCommand(getBackCommand());
            helpScreen.setCommandListener(this);//GEN-END:|147-getter|1|147-postInit
            Runtime runtime = Runtime.getRuntime();
            getTotalMemoryStringItem().setText(runtime.totalMemory() + " bytes");
            getFreeMemoryStringItem().setText(runtime.freeMemory() + " bytes");
        }//GEN-BEGIN:|147-getter|2|
        return helpScreen;
    }
    //</editor-fold>//GEN-END:|147-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: settingsCommand ">//GEN-BEGIN:|152-getter|0|152-preInit
    /**
     * Returns an initiliazed instance of settingsCommand component.
     * @return the initialized component instance
     */
    public Command getSettingsCommand() {
        if (settingsCommand == null) {//GEN-END:|152-getter|0|152-preInit
            // write pre-init user code here
            settingsCommand = new Command(GPSLoggerLocalization.getMessage("settingsCommand"), GPSLoggerLocalization.getMessage("settingsCommand"), Command.SCREEN, 10);//GEN-LINE:|152-getter|1|152-postInit
            // write post-init user code here
        }//GEN-BEGIN:|152-getter|2|
        return settingsCommand;
    }
    //</editor-fold>//GEN-END:|152-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: coordinatesModeChoiceGroup ">//GEN-BEGIN:|157-getter|0|157-preInit
    /**
     * Returns an initiliazed instance of coordinatesModeChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getCoordinatesModeChoiceGroup() {
        if (coordinatesModeChoiceGroup == null) {//GEN-END:|157-getter|0|157-preInit
            // write pre-init user code here
            coordinatesModeChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Coordinates"), Choice.EXCLUSIVE);//GEN-BEGIN:|157-getter|1|157-postInit
            coordinatesModeChoiceGroup.append("DD.dd\u00B0", null);
            coordinatesModeChoiceGroup.append("DD\u00B0 MM.mm\'", null);
            coordinatesModeChoiceGroup.append("DD\u00B0 MM\' SS.ss\"", null);
            coordinatesModeChoiceGroup.setSelectedFlags(new boolean[] { false, false, false });//GEN-END:|157-getter|1|157-postInit
            // write post-init user code here
        }//GEN-BEGIN:|157-getter|2|
        return coordinatesModeChoiceGroup;
    }
    //</editor-fold>//GEN-END:|157-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logFolderTextField ">//GEN-BEGIN:|160-getter|0|160-preInit
    /**
     * Returns an initiliazed instance of logFolderTextField component.
     * @return the initialized component instance
     */
    public TextField getLogFolderTextField() {
        if (logFolderTextField == null) {//GEN-END:|160-getter|0|160-preInit
            // write pre-init user code here
            logFolderTextField = new TextField(GPSLoggerLocalization.getMessage("LogFolder"), "", 4096, TextField.ANY);//GEN-BEGIN:|160-getter|1|160-postInit
            logFolderTextField.addCommand(getBrowseCommand());
            logFolderTextField.setItemCommandListener(this);
            logFolderTextField.setDefaultCommand(getBrowseCommand());//GEN-END:|160-getter|1|160-postInit
            // write post-init user code here
        }//GEN-BEGIN:|160-getter|2|
        return logFolderTextField;
    }
    //</editor-fold>//GEN-END:|160-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: gpsDeviceURLTextField ">//GEN-BEGIN:|161-getter|0|161-preInit
    /**
     * Returns an initiliazed instance of gpsDeviceURLTextField component.
     * @return the initialized component instance
     */
    public TextField getGpsDeviceURLTextField() {
        if (gpsDeviceURLTextField == null) {//GEN-END:|161-getter|0|161-preInit
            // write pre-init user code here
            gpsDeviceURLTextField = new TextField(GPSLoggerLocalization.getMessage("GPSDevice"), "", 4096, TextField.URL);//GEN-BEGIN:|161-getter|1|161-postInit
            gpsDeviceURLTextField.addCommand(getSearchCommand());
            gpsDeviceURLTextField.setItemCommandListener(this);//GEN-END:|161-getter|1|161-postInit
            // write post-init user code here
        }//GEN-BEGIN:|161-getter|2|
        return gpsDeviceURLTextField;
    }
    //</editor-fold>//GEN-END:|161-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Items ">//GEN-BEGIN:|17-itemCommandAction|0|17-preItemCommandAction
    /**
     * Called by a system to indicated that a command has been invoked on a particular item.
     * @param command the Command that was invoked
     * @param displayable the Item where the command was invoked
     */
    public void commandAction(Command command, Item item) {//GEN-END:|17-itemCommandAction|0|17-preItemCommandAction

        if (item == browseLogFolderStringItem) {//GEN-BEGIN:|17-itemCommandAction|1|272-preAction
            if (command == browseCommand) {//GEN-END:|17-itemCommandAction|1|272-preAction

                browseLogFolder();//GEN-LINE:|17-itemCommandAction|2|272-postAction

            }//GEN-BEGIN:|17-itemCommandAction|3|363-preAction
        } else if (item == browseMapDescriptorFileStringItem) {
            if (command == browseMapDescriptorFileCommand) {//GEN-END:|17-itemCommandAction|3|363-preAction
                // write pre-action user code here
//GEN-LINE:|17-itemCommandAction|4|363-postAction
                // write post-action user code here
            }//GEN-BEGIN:|17-itemCommandAction|5|210-preAction
        } else if (item == emailItem) {
            if (command == sendEmailCommand) {//GEN-END:|17-itemCommandAction|5|210-preAction

                sendEmail();//GEN-LINE:|17-itemCommandAction|6|210-postAction

            }//GEN-BEGIN:|17-itemCommandAction|7|236-preAction
        } else if (item == gpsDeviceURLTextField) {
            if (command == searchCommand) {//GEN-END:|17-itemCommandAction|7|236-preAction

                searchDevices();//GEN-LINE:|17-itemCommandAction|8|236-postAction

            }//GEN-BEGIN:|17-itemCommandAction|9|181-preAction
        } else if (item == logFolderTextField) {
            if (command == browseCommand) {//GEN-END:|17-itemCommandAction|9|181-preAction
                browseLogFolder();//GEN-LINE:|17-itemCommandAction|10|181-postAction

            }//GEN-BEGIN:|17-itemCommandAction|11|367-preAction
        } else if (item == mapDescriptorFileTextField) {
            if (command == browseMapDescriptorFileCommand) {//GEN-END:|17-itemCommandAction|11|367-preAction
                // write pre-action user code here
//GEN-LINE:|17-itemCommandAction|12|367-postAction
                // write post-action user code here
            }//GEN-BEGIN:|17-itemCommandAction|13|269-preAction
        } else if (item == searchGPSStringItem) {
            if (command == searchCommand) {//GEN-END:|17-itemCommandAction|13|269-preAction

                searchDevices();//GEN-LINE:|17-itemCommandAction|14|269-postAction

            }//GEN-BEGIN:|17-itemCommandAction|15|17-postItemCommandAction
        }//GEN-END:|17-itemCommandAction|15|17-postItemCommandAction

    }//GEN-BEGIN:|17-itemCommandAction|16|
    //</editor-fold>//GEN-END:|17-itemCommandAction|16|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: speedUnitsChoiceGroup ">//GEN-BEGIN:|163-getter|0|163-preInit
    /**
     * Returns an initiliazed instance of speedUnitsChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getSpeedUnitsChoiceGroup() {
        if (speedUnitsChoiceGroup == null) {//GEN-END:|163-getter|0|163-preInit
            // write pre-init user code here
            speedUnitsChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Speed"), Choice.EXCLUSIVE);//GEN-BEGIN:|163-getter|1|163-postInit
            speedUnitsChoiceGroup.append("km/h", null);
            speedUnitsChoiceGroup.append("mph", null);
            speedUnitsChoiceGroup.append("knots", null);
            speedUnitsChoiceGroup.append("m/sec", null);
            speedUnitsChoiceGroup.setSelectedFlags(new boolean[] { false, false, false, false });//GEN-END:|163-getter|1|163-postInit
            // write post-init user code here
        }//GEN-BEGIN:|163-getter|2|
        return speedUnitsChoiceGroup;
    }
    //</editor-fold>//GEN-END:|163-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: browseCommand ">//GEN-BEGIN:|178-getter|0|178-preInit
    /**
     * Returns an initiliazed instance of browseCommand component.
     * @return the initialized component instance
     */
    public Command getBrowseCommand() {
        if (browseCommand == null) {//GEN-END:|178-getter|0|178-preInit
            // write pre-init user code here
            browseCommand = new Command(GPSLoggerLocalization.getMessage("browseCommand"), Command.OK, 0);//GEN-LINE:|178-getter|1|178-postInit
            // write post-init user code here
        }//GEN-BEGIN:|178-getter|2|
        return browseCommand;
    }
    //</editor-fold>//GEN-END:|178-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: setLogFolder ">//GEN-BEGIN:|183-entry|0|184-preAction
    /**
     * Performs an action assigned to the setLogFolder entry-point.
     */
    public void setLogFolder() {//GEN-END:|183-entry|0|184-preAction
        logFolderTextField.setString(fileBrowser.getSelectedFileURL());
        getDisplay().setCurrent(getSettingsScreen()); // without history
//GEN-LINE:|183-entry|1|184-postAction
        // write post-action user code here
    }//GEN-BEGIN:|183-entry|2|
    //</editor-fold>//GEN-END:|183-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: saveSettings ">//GEN-BEGIN:|188-entry|0|189-preAction
    /**
     * Performs an action assigned to the saveSettings entry-point.
     */
    public void saveSettings() {//GEN-END:|188-entry|0|189-preAction

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
//GEN-LINE:|188-entry|1|189-postAction

    }//GEN-BEGIN:|188-entry|2|
    //</editor-fold>//GEN-END:|188-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: saveSettingsCommand ">//GEN-BEGIN:|186-getter|0|186-preInit
    /**
     * Returns an initiliazed instance of saveSettingsCommand component.
     * @return the initialized component instance
     */
    public Command getSaveSettingsCommand() {
        if (saveSettingsCommand == null) {//GEN-END:|186-getter|0|186-preInit
            // write pre-init user code here
            saveSettingsCommand = new Command(GPSLoggerLocalization.getMessage("saveSettingsCommand"), Command.OK, 0);//GEN-LINE:|186-getter|1|186-postInit
            // write post-init user code here
        }//GEN-BEGIN:|186-getter|2|
        return saveSettingsCommand;
    }
    //</editor-fold>//GEN-END:|186-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: stringItem1 ">//GEN-BEGIN:|191-getter|0|191-preInit
    /**
     * Returns an initiliazed instance of stringItem1 component.
     * @return the initialized component instance
     */
    public StringItem getStringItem1() {
        if (stringItem1 == null) {//GEN-END:|191-getter|0|191-preInit
            // write pre-init user code here
            stringItem1 = new StringItem("(C) 2007-2009 ", "This midlet was written by Serge Perinsky");//GEN-BEGIN:|191-getter|1|191-postInit
            stringItem1.setLayout(ImageItem.LAYOUT_LEFT | Item.LAYOUT_TOP | Item.LAYOUT_VCENTER | ImageItem.LAYOUT_NEWLINE_BEFORE | ImageItem.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_SHRINK);//GEN-END:|191-getter|1|191-postInit
            // write post-init user code here
        }//GEN-BEGIN:|191-getter|2|
        return stringItem1;
    }
    //</editor-fold>//GEN-END:|191-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: saveWaypoint ">//GEN-BEGIN:|197-entry|0|198-preAction
    /**
     * Performs an action assigned to the saveWaypoint entry-point.
     */
    public void saveWaypoint() {//GEN-END:|197-entry|0|198-preAction

        switchDisplayable(null, getMainScreen()); // go back to the main screen immediately anyway

        try {
            saveCurrentWaypoint(getWaypointNameTextField().getString().trim());
        } catch (IOException e) {
            handleException(e, getMainScreen());
        }
//GEN-LINE:|197-entry|1|198-postAction
    }//GEN-BEGIN:|197-entry|2|
    //</editor-fold>//GEN-END:|197-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: markWaypointCommand ">//GEN-BEGIN:|195-getter|0|195-preInit
    /**
     * Returns an initiliazed instance of markWaypointCommand component.
     * @return the initialized component instance
     */
    public Command getMarkWaypointCommand() {
        if (markWaypointCommand == null) {//GEN-END:|195-getter|0|195-preInit
            // write pre-init user code here
            markWaypointCommand = new Command(GPSLoggerLocalization.getMessage("markWaypointCommand"), GPSLoggerLocalization.getMessage("markWaypointCommandLong"), Command.OK, 0);//GEN-LINE:|195-getter|1|195-postInit
            // write post-init user code here
        }//GEN-BEGIN:|195-getter|2|
        return markWaypointCommand;
    }
    //</editor-fold>//GEN-END:|195-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: sendEmail ">//GEN-BEGIN:|211-entry|0|212-preAction
    /**
     * Performs an action assigned to the sendEmail entry-point.
     */
    public void sendEmail() {//GEN-END:|211-entry|0|212-preAction
        try {
            platformRequest("mailto:" + getEmailItem().getText());
        } catch (ConnectionNotFoundException e) {
            handleException(e, startScreen);
        }

//GEN-LINE:|211-entry|1|212-postAction
        // write post-action user code here
    }//GEN-BEGIN:|211-entry|2|
    //</editor-fold>//GEN-END:|211-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: sendEmailCommand ">//GEN-BEGIN:|209-getter|0|209-preInit
    /**
     * Returns an initiliazed instance of sendEmailCommand component.
     * @return the initialized component instance
     */
    public Command getSendEmailCommand() {
        if (sendEmailCommand == null) {//GEN-END:|209-getter|0|209-preInit
            // write pre-init user code here
            sendEmailCommand = new Command(GPSLoggerLocalization.getMessage("sendEmailCommand"), GPSLoggerLocalization.getMessage("sendEmailCommand"), Command.ITEM, 0);//GEN-LINE:|209-getter|1|209-postInit
            // write post-init user code here
        }//GEN-BEGIN:|209-getter|2|
        return sendEmailCommand;
    }
    //</editor-fold>//GEN-END:|209-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: emailItem ">//GEN-BEGIN:|208-getter|0|208-preInit
    /**
     * Returns an initiliazed instance of emailItem component.
     * @return the initialized component instance
     */
    public StringItem getEmailItem() {
        if (emailItem == null) {//GEN-END:|208-getter|0|208-preInit
            // write pre-init user code here
            emailItem = new StringItem("E-mail:", "sergebass@yahoo.com", Item.HYPERLINK);//GEN-BEGIN:|208-getter|1|208-postInit
            emailItem.addCommand(getSendEmailCommand());
            emailItem.setItemCommandListener(this);
            emailItem.setDefaultCommand(getSendEmailCommand());//GEN-END:|208-getter|1|208-postInit
            // write post-init user code here
        }//GEN-BEGIN:|208-getter|2|
        return emailItem;
    }
    //</editor-fold>//GEN-END:|208-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cancelCommand ">//GEN-BEGIN:|214-getter|0|214-preInit
    /**
     * Returns an initiliazed instance of cancelCommand component.
     * @return the initialized component instance
     */
    public Command getCancelCommand() {
        if (cancelCommand == null) {//GEN-END:|214-getter|0|214-preInit
            // write pre-init user code here
            cancelCommand = new Command(GPSLoggerLocalization.getMessage("cancelCommand"), Command.CANCEL, 1);//GEN-LINE:|214-getter|1|214-postInit
            // write post-init user code here
        }//GEN-BEGIN:|214-getter|2|
        return cancelCommand;
    }
    //</editor-fold>//GEN-END:|214-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: altitudeUnitsChoiceGroup ">//GEN-BEGIN:|220-getter|0|220-preInit
    /**
     * Returns an initiliazed instance of altitudeUnitsChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getAltitudeUnitsChoiceGroup() {
        if (altitudeUnitsChoiceGroup == null) {//GEN-END:|220-getter|0|220-preInit
            // write pre-init user code here
            altitudeUnitsChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Altitude"), Choice.EXCLUSIVE);//GEN-BEGIN:|220-getter|1|220-postInit
            altitudeUnitsChoiceGroup.append("meters", null);
            altitudeUnitsChoiceGroup.append("feet", null);
            altitudeUnitsChoiceGroup.setSelectedFlags(new boolean[] { false, false });//GEN-END:|220-getter|1|220-postInit
            // write post-init user code here
        }//GEN-BEGIN:|220-getter|2|
        return altitudeUnitsChoiceGroup;
    }
    //</editor-fold>//GEN-END:|220-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: languageChoiceGroup ">//GEN-BEGIN:|223-getter|0|223-preInit
    /**
     * Returns an initiliazed instance of languageChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getLanguageChoiceGroup() {
        if (languageChoiceGroup == null) {//GEN-END:|223-getter|0|223-preInit
            // write pre-init user code here
            languageChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Language"), Choice.EXCLUSIVE);//GEN-BEGIN:|223-getter|1|223-postInit
            languageChoiceGroup.append("Default", null);
            languageChoiceGroup.append("English", null);
            languageChoiceGroup.append("\u0420\u0443\u0441\u0441\u043A\u0438\u0439", null);
            languageChoiceGroup.setSelectedFlags(new boolean[] { false, false, false });//GEN-END:|223-getter|1|223-postInit
            // write post-init user code here
        }//GEN-BEGIN:|223-getter|2|
        return languageChoiceGroup;
    }
    //</editor-fold>//GEN-END:|223-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: gpsDeviceNameStartScreenStringItem ">//GEN-BEGIN:|232-getter|0|232-preInit
    /**
     * Returns an initiliazed instance of gpsDeviceNameStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getGpsDeviceNameStartScreenStringItem() {
        if (gpsDeviceNameStartScreenStringItem == null) {//GEN-END:|232-getter|0|232-preInit
            // write pre-init user code here
            gpsDeviceNameStartScreenStringItem = new StringItem("GPS: ", "");//GEN-BEGIN:|232-getter|1|232-postInit
            gpsDeviceNameStartScreenStringItem.setLayout(ImageItem.LAYOUT_DEFAULT | Item.LAYOUT_VSHRINK);//GEN-END:|232-getter|1|232-postInit
            // write post-init user code here
        }//GEN-BEGIN:|232-getter|2|
        return gpsDeviceNameStartScreenStringItem;
    }
    //</editor-fold>//GEN-END:|232-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: errorAlert ">//GEN-BEGIN:|233-getter|0|233-preInit
    /**
     * Returns an initiliazed instance of errorAlert component.
     * @return the initialized component instance
     */
    public Alert getErrorAlert() {
        if (errorAlert == null) {//GEN-END:|233-getter|0|233-preInit
            // write pre-init user code here
            errorAlert = new Alert("Error", null, null, AlertType.ERROR);//GEN-BEGIN:|233-getter|1|233-postInit
            errorAlert.setTimeout(Alert.FOREVER);//GEN-END:|233-getter|1|233-postInit
            // write post-init user code here
        }//GEN-BEGIN:|233-getter|2|
        return errorAlert;
    }
    //</editor-fold>//GEN-END:|233-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand ">//GEN-BEGIN:|242-getter|0|242-preInit
    /**
     * Returns an initiliazed instance of okCommand component.
     * @return the initialized component instance
     */
    public Command getOkCommand() {
        if (okCommand == null) {//GEN-END:|242-getter|0|242-preInit
            // write pre-init user code here
            okCommand = new Command("Ok", Command.OK, 0);//GEN-LINE:|242-getter|1|242-postInit
            // write post-init user code here
        }//GEN-BEGIN:|242-getter|2|
        return okCommand;
    }
    //</editor-fold>//GEN-END:|242-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logPathStartScreenStringItem ">//GEN-BEGIN:|249-getter|0|249-preInit
    /**
     * Returns an initiliazed instance of logPathStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getLogPathStartScreenStringItem() {
        if (logPathStartScreenStringItem == null) {//GEN-END:|249-getter|0|249-preInit
            // write pre-init user code here
            logPathStartScreenStringItem = new StringItem("Log: ", "");//GEN-BEGIN:|249-getter|1|249-postInit
            logPathStartScreenStringItem.setLayout(ImageItem.LAYOUT_DEFAULT | Item.LAYOUT_VSHRINK);//GEN-END:|249-getter|1|249-postInit
            // write post-init user code here
        }//GEN-BEGIN:|249-getter|2|
        return logPathStartScreenStringItem;
    }
    //</editor-fold>//GEN-END:|249-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: freeSpaceStartScreenStringItem ">//GEN-BEGIN:|250-getter|0|250-preInit
    /**
     * Returns an initiliazed instance of freeSpaceStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getFreeSpaceStartScreenStringItem() {
        if (freeSpaceStartScreenStringItem == null) {//GEN-END:|250-getter|0|250-preInit
            // write pre-init user code here
            freeSpaceStartScreenStringItem = new StringItem("", "");//GEN-LINE:|250-getter|1|250-postInit
            // write post-init user code here
        }//GEN-BEGIN:|250-getter|2|
        return freeSpaceStartScreenStringItem;
    }
    //</editor-fold>//GEN-END:|250-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: resetOdometer ">//GEN-BEGIN:|256-entry|0|257-preAction
    /**
     * Performs an action assigned to the resetOdometer entry-point.
     */
    public void resetOdometer() {//GEN-END:|256-entry|0|257-preAction

        startTimeMillis = System.currentTimeMillis();
        getMainScreen().setTotalTime("");
//GEN-LINE:|256-entry|1|257-postAction
 // write post-action user code here
    }//GEN-BEGIN:|256-entry|2|
    //</editor-fold>//GEN-END:|256-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: resetCommand ">//GEN-BEGIN:|254-getter|0|254-preInit
    /**
     * Returns an initiliazed instance of resetCommand component.
     * @return the initialized component instance
     */
    public Command getResetCommand() {
        if (resetCommand == null) {//GEN-END:|254-getter|0|254-preInit
 // write pre-init user code here
            resetCommand = new Command(GPSLoggerLocalization.getMessage("resetCommand"), Command.OK, 0);//GEN-LINE:|254-getter|1|254-postInit
 // write post-init user code here
        }//GEN-BEGIN:|254-getter|2|
        return resetCommand;
    }
    //</editor-fold>//GEN-END:|254-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: font ">//GEN-BEGIN:|260-getter|0|260-preInit
    /**
     * Returns an initiliazed instance of font component.
     * @return the initialized component instance
     */
    public Font getFont() {
        if (font == null) {//GEN-END:|260-getter|0|260-preInit
        // write pre-init user code here
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);//GEN-LINE:|260-getter|1|260-postInit
        // write post-init user code here
        }//GEN-BEGIN:|260-getter|2|
        return font;
    }
    //</editor-fold>//GEN-END:|260-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: boldFont ">//GEN-BEGIN:|261-getter|0|261-preInit
    /**
     * Returns an initiliazed instance of boldFont component.
     * @return the initialized component instance
     */
    public Font getBoldFont() {
        if (boldFont == null) {//GEN-END:|261-getter|0|261-preInit
        // write pre-init user code here
            boldFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);//GEN-LINE:|261-getter|1|261-postInit
        // write post-init user code here
        }//GEN-BEGIN:|261-getter|2|
        return boldFont;
    }
    //</editor-fold>//GEN-END:|261-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: searchGPSStringItem ">//GEN-BEGIN:|268-getter|0|268-preInit
    /**
     * Returns an initiliazed instance of searchGPSStringItem component.
     * @return the initialized component instance
     */
    public StringItem getSearchGPSStringItem() {
        if (searchGPSStringItem == null) {//GEN-END:|268-getter|0|268-preInit
        // write pre-init user code here
            searchGPSStringItem = new StringItem(GPSLoggerLocalization.getMessage("SearchGPS"), "", Item.BUTTON);//GEN-BEGIN:|268-getter|1|268-postInit
            searchGPSStringItem.addCommand(getSearchCommand());
            searchGPSStringItem.setItemCommandListener(this);
            searchGPSStringItem.setDefaultCommand(getSearchCommand());//GEN-END:|268-getter|1|268-postInit
        // write post-init user code here
        }//GEN-BEGIN:|268-getter|2|
        return searchGPSStringItem;
    }
    //</editor-fold>//GEN-END:|268-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: browseLogFolderStringItem ">//GEN-BEGIN:|270-getter|0|270-preInit
    /**
     * Returns an initiliazed instance of browseLogFolderStringItem component.
     * @return the initialized component instance
     */
    public StringItem getBrowseLogFolderStringItem() {
        if (browseLogFolderStringItem == null) {//GEN-END:|270-getter|0|270-preInit
        // write pre-init user code here
            browseLogFolderStringItem = new StringItem(GPSLoggerLocalization.getMessage("BrowseFolder"), "", Item.BUTTON);//GEN-BEGIN:|270-getter|1|270-postInit
            browseLogFolderStringItem.addCommand(getBrowseCommand());
            browseLogFolderStringItem.setItemCommandListener(this);
            browseLogFolderStringItem.setDefaultCommand(getBrowseCommand());//GEN-END:|270-getter|1|270-postInit
        // write post-init user code here
        }//GEN-BEGIN:|270-getter|2|
        return browseLogFolderStringItem;
    }
    //</editor-fold>//GEN-END:|270-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: spacer ">//GEN-BEGIN:|271-getter|0|271-preInit
    /**
     * Returns an initiliazed instance of spacer component.
     * @return the initialized component instance
     */
    public Spacer getSpacer() {
        if (spacer == null) {//GEN-END:|271-getter|0|271-preInit
        // write pre-init user code here
            spacer = new Spacer(16, 1);//GEN-LINE:|271-getter|1|271-postInit
        // write post-init user code here
        }//GEN-BEGIN:|271-getter|2|
        return spacer;
    }
    //</editor-fold>//GEN-END:|271-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: spacer1 ">//GEN-BEGIN:|274-getter|0|274-preInit
    /**
     * Returns an initiliazed instance of spacer1 component.
     * @return the initialized component instance
     */
    public Spacer getSpacer1() {
        if (spacer1 == null) {//GEN-END:|274-getter|0|274-preInit
        // write pre-init user code here
            spacer1 = new Spacer(16, 1);//GEN-LINE:|274-getter|1|274-postInit
        // write post-init user code here
        }//GEN-BEGIN:|274-getter|2|
        return spacer1;
    }
    //</editor-fold>//GEN-END:|274-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: browseLogFolder ">//GEN-BEGIN:|275-entry|0|276-preAction
    /**
     * Performs an action assigned to the browseLogFolder entry-point.
     */
    public void browseLogFolder() {//GEN-END:|275-entry|0|276-preAction

        getDisplay().setCurrent(getFileBrowser()); // without history
//GEN-LINE:|275-entry|1|276-postAction
    }//GEN-BEGIN:|275-entry|2|
    //</editor-fold>//GEN-END:|275-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: waypointScreen ">//GEN-BEGIN:|279-getter|0|279-preInit
    /**
     * Returns an initiliazed instance of waypointScreen component.
     * @return the initialized component instance
     */
    public Form getWaypointScreen() {
        if (waypointScreen == null) {//GEN-END:|279-getter|0|279-preInit
        // write pre-init user code here
            waypointScreen = new Form("Waypoint", new Item[] { getWaypointNameTextField() });//GEN-BEGIN:|279-getter|1|279-postInit
            waypointScreen.addCommand(getCancelWaypointCommand());
            waypointScreen.addCommand(getSaveWaypointCommand());
            waypointScreen.addCommand(getTakePhotoCommand());
            waypointScreen.addCommand(getSendSMSCommand());
            waypointScreen.setCommandListener(this);//GEN-END:|279-getter|1|279-postInit
        // write post-init user code here
        }//GEN-BEGIN:|279-getter|2|
        return waypointScreen;
    }
    //</editor-fold>//GEN-END:|279-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: waypointNameTextField ">//GEN-BEGIN:|280-getter|0|280-preInit
    /**
     * Returns an initiliazed instance of waypointNameTextField component.
     * @return the initialized component instance
     */
    public TextField getWaypointNameTextField() {
        if (waypointNameTextField == null) {//GEN-END:|280-getter|0|280-preInit
        // write pre-init user code here
            waypointNameTextField = new TextField("Name:", "", 32, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);//GEN-LINE:|280-getter|1|280-postInit
        // write post-init user code here
        }//GEN-BEGIN:|280-getter|2|
        return waypointNameTextField;
    }
    //</editor-fold>//GEN-END:|280-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cancelWaypointCommand ">//GEN-BEGIN:|281-getter|0|281-preInit
    /**
     * Returns an initiliazed instance of cancelWaypointCommand component.
     * @return the initialized component instance
     */
    public Command getCancelWaypointCommand() {
        if (cancelWaypointCommand == null) {//GEN-END:|281-getter|0|281-preInit
        // write pre-init user code here
            cancelWaypointCommand = new Command(GPSLoggerLocalization.getMessage("cancelWaypointCommand"), Command.BACK, 0);//GEN-LINE:|281-getter|1|281-postInit
        // write post-init user code here
        }//GEN-BEGIN:|281-getter|2|
        return cancelWaypointCommand;
    }
    //</editor-fold>//GEN-END:|281-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: saveWaypointCommand ">//GEN-BEGIN:|283-getter|0|283-preInit
    /**
     * Returns an initiliazed instance of saveWaypointCommand component.
     * @return the initialized component instance
     */
    public Command getSaveWaypointCommand() {
        if (saveWaypointCommand == null) {//GEN-END:|283-getter|0|283-preInit
        // write pre-init user code here
            saveWaypointCommand = new Command(GPSLoggerLocalization.getMessage("saveWaypointCommand"), Command.OK, 0);//GEN-LINE:|283-getter|1|283-postInit
        // write post-init user code here
        }//GEN-BEGIN:|283-getter|2|
        return saveWaypointCommand;
    }
    //</editor-fold>//GEN-END:|283-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: takePhoto ">//GEN-BEGIN:|292-entry|0|293-preAction
    /**
     * Performs an action assigned to the takePhoto entry-point.
     */
    public void takePhoto() {//GEN-END:|292-entry|0|293-preAction

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
//GEN-LINE:|292-entry|1|293-postAction
    }//GEN-BEGIN:|292-entry|2|
    //</editor-fold>//GEN-END:|292-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: takePhotoCommand ">//GEN-BEGIN:|290-getter|0|290-preInit
    /**
     * Returns an initiliazed instance of takePhotoCommand component.
     * @return the initialized component instance
     */
    public Command getTakePhotoCommand() {
        if (takePhotoCommand == null) {//GEN-END:|290-getter|0|290-preInit
        // write pre-init user code here
            takePhotoCommand = new Command(GPSLoggerLocalization.getMessage("takePhotoCommand"), Command.OK, 0);//GEN-LINE:|290-getter|1|290-postInit
        // write post-init user code here
        }//GEN-BEGIN:|290-getter|2|
        return takePhotoCommand;
    }
    //</editor-fold>//GEN-END:|290-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: stopCommand ">//GEN-BEGIN:|295-getter|0|295-preInit
    /**
     * Returns an initiliazed instance of stopCommand component.
     * @return the initialized component instance
     */
    public Command getStopCommand() {
        if (stopCommand == null) {//GEN-END:|295-getter|0|295-preInit
            // write pre-init user code here
            stopCommand = new Command(GPSLoggerLocalization.getMessage("stopCommand"), GPSLoggerLocalization.getMessage("stopCommand"), Command.OK, 2);//GEN-LINE:|295-getter|1|295-postInit
            // write post-init user code here
        }//GEN-BEGIN:|295-getter|2|
        return stopCommand;
    }
    //</editor-fold>//GEN-END:|295-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: stopTrack ">//GEN-BEGIN:|296-entry|0|297-preAction
    /**
     * Performs an action assigned to the stopTrack entry-point.
     */
    public void stopTrack() {//GEN-END:|296-entry|0|297-preAction

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
//GEN-LINE:|296-entry|1|297-postAction
        switchDisplayable(null, startScreen);
    }//GEN-BEGIN:|296-entry|2|
    //</editor-fold>//GEN-END:|296-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: sendSMSCommand ">//GEN-BEGIN:|298-getter|0|298-preInit
    /**
     * Returns an initiliazed instance of sendSMSCommand component.
     * @return the initialized component instance
     */
    public Command getSendSMSCommand() {
        if (sendSMSCommand == null) {//GEN-END:|298-getter|0|298-preInit
            // write pre-init user code here
            sendSMSCommand = new Command(GPSLoggerLocalization.getMessage("sendSMSCommand"), Command.OK, 0);//GEN-LINE:|298-getter|1|298-postInit
            // write post-init user code here
        }//GEN-BEGIN:|298-getter|2|
        return sendSMSCommand;
    }
    //</editor-fold>//GEN-END:|298-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: sendSMS ">//GEN-BEGIN:|300-entry|0|301-preAction
    /**
     * Performs an action assigned to the sendSMS entry-point.
     */
    public void sendSMS() {//GEN-END:|300-entry|0|301-preAction
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
//GEN-LINE:|300-entry|1|301-postAction
        // write post-action user code here
    }//GEN-BEGIN:|300-entry|2|
    //</editor-fold>//GEN-END:|300-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logFormatChoiceGroup ">//GEN-BEGIN:|304-getter|0|304-preInit
    /**
     * Returns an initiliazed instance of logFormatChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getLogFormatChoiceGroup() {
        if (logFormatChoiceGroup == null) {//GEN-END:|304-getter|0|304-preInit
            // write pre-init user code here
            logFormatChoiceGroup = new ChoiceGroup("Log format", Choice.MULTIPLE);//GEN-BEGIN:|304-getter|1|304-postInit
            logFormatChoiceGroup.append("GPX", null);
            logFormatChoiceGroup.append("KML (Google Earth)", null);
            logFormatChoiceGroup.append("TCX (Garmin Training Center)", null);
            logFormatChoiceGroup.setSelectedFlags(new boolean[] { true, false, false });//GEN-END:|304-getter|1|304-postInit
            // write post-init user code here
        }//GEN-BEGIN:|304-getter|2|
        return logFormatChoiceGroup;
    }
    //</editor-fold>//GEN-END:|304-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logSettingsChoiceGroup ">//GEN-BEGIN:|307-getter|0|307-preInit
    /**
     * Returns an initiliazed instance of logSettingsChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getLogSettingsChoiceGroup() {
        if (logSettingsChoiceGroup == null) {//GEN-END:|307-getter|0|307-preInit
            // write pre-init user code here
            logSettingsChoiceGroup = new ChoiceGroup("Log settings", Choice.MULTIPLE);//GEN-BEGIN:|307-getter|1|307-postInit
            logSettingsChoiceGroup.append("Save NMEA data (if available)", null);
            logSettingsChoiceGroup.append("Separate NMEA log", null);
            logSettingsChoiceGroup.append("Separate Waypoint log", null);
            logSettingsChoiceGroup.setSelectedFlags(new boolean[] { true, false, false });//GEN-END:|307-getter|1|307-postInit
            // write post-init user code here
        }//GEN-BEGIN:|307-getter|2|
        return logSettingsChoiceGroup;
    }
    //</editor-fold>//GEN-END:|307-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logUpdateFrequencyTextField ">//GEN-BEGIN:|312-getter|0|312-preInit
    /**
     * Returns an initiliazed instance of logUpdateFrequencyTextField component.
     * @return the initialized component instance
     */
    public TextField getLogUpdateFrequencyTextField() {
        if (logUpdateFrequencyTextField == null) {//GEN-END:|312-getter|0|312-preInit
            // write pre-init user code here
            logUpdateFrequencyTextField = new TextField("Log update frequency (seconds)", "1", 32, TextField.NUMERIC);//GEN-BEGIN:|312-getter|1|312-postInit
            logUpdateFrequencyTextField.setLayout(ImageItem.LAYOUT_DEFAULT);//GEN-END:|312-getter|1|312-postInit
            // write post-init user code here
        }//GEN-BEGIN:|312-getter|2|
        return logUpdateFrequencyTextField;
    }
    //</editor-fold>//GEN-END:|312-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: defaultSmsPhoneNumber ">//GEN-BEGIN:|313-getter|0|313-preInit
    /**
     * Returns an initiliazed instance of defaultSmsPhoneNumber component.
     * @return the initialized component instance
     */
    public TextField getDefaultSmsPhoneNumber() {
        if (defaultSmsPhoneNumber == null) {//GEN-END:|313-getter|0|313-preInit
            // write pre-init user code here
            defaultSmsPhoneNumber = new TextField("default SMS phone number", "+1", 32, TextField.PHONENUMBER);//GEN-LINE:|313-getter|1|313-postInit
            // write post-init user code here
        }//GEN-BEGIN:|313-getter|2|
        return defaultSmsPhoneNumber;
    }
    //</editor-fold>//GEN-END:|313-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smsScreen ">//GEN-BEGIN:|316-getter|0|316-preInit
    /**
     * Returns an initiliazed instance of smsScreen component.
     * @return the initialized component instance
     */
    public Form getSmsScreen() {
        if (smsScreen == null) {//GEN-END:|316-getter|0|316-preInit
            // write pre-init user code here
            smsScreen = new Form("Send SMS", new Item[] { getPhoneNumberTextField(), getSmsLengthStringItem(), getSmsTextStringItem() });//GEN-BEGIN:|316-getter|1|316-postInit
            smsScreen.addCommand(getOkSendSMSCommand());
            smsScreen.addCommand(getCancelSendSMSCommand());
            smsScreen.setCommandListener(this);//GEN-END:|316-getter|1|316-postInit
            // write post-init user code here
        }//GEN-BEGIN:|316-getter|2|
        return smsScreen;
    }
    //</editor-fold>//GEN-END:|316-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: phoneNumberTextField ">//GEN-BEGIN:|317-getter|0|317-preInit
    /**
     * Returns an initiliazed instance of phoneNumberTextField component.
     * @return the initialized component instance
     */
    public TextField getPhoneNumberTextField() {
        if (phoneNumberTextField == null) {//GEN-END:|317-getter|0|317-preInit
            // write pre-init user code here
            phoneNumberTextField = new TextField("Phone number", "", 32, TextField.PHONENUMBER);//GEN-LINE:|317-getter|1|317-postInit
            // write post-init user code here
        }//GEN-BEGIN:|317-getter|2|
        return phoneNumberTextField;
    }
    //</editor-fold>//GEN-END:|317-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smsTextBox ">//GEN-BEGIN:|319-getter|0|319-preInit
    /**
     * Returns an initiliazed instance of smsTextBox component.
     * @return the initialized component instance
     */
    public TextBox getSmsTextBox() {
        if (smsTextBox == null) {//GEN-END:|319-getter|0|319-preInit
            // write pre-init user code here
            smsTextBox = new TextBox("SMS Message", null, 160, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);//GEN-BEGIN:|319-getter|1|319-postInit
            smsTextBox.addCommand(getOkSMSEditCommand());
            smsTextBox.addCommand(getCancelSMSEditCommand());
            smsTextBox.setCommandListener(this);//GEN-END:|319-getter|1|319-postInit
            // write post-init user code here
        }//GEN-BEGIN:|319-getter|2|
        return smsTextBox;
    }
    //</editor-fold>//GEN-END:|319-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okSMSEditCommand ">//GEN-BEGIN:|320-getter|0|320-preInit
    /**
     * Returns an initiliazed instance of okSMSEditCommand component.
     * @return the initialized component instance
     */
    public Command getOkSMSEditCommand() {
        if (okSMSEditCommand == null) {//GEN-END:|320-getter|0|320-preInit
            // write pre-init user code here
            okSMSEditCommand = new Command("Ok", Command.OK, 0);//GEN-LINE:|320-getter|1|320-postInit
            // write post-init user code here
        }//GEN-BEGIN:|320-getter|2|
        return okSMSEditCommand;
    }
    //</editor-fold>//GEN-END:|320-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cancelSMSEditCommand ">//GEN-BEGIN:|322-getter|0|322-preInit
    /**
     * Returns an initiliazed instance of cancelSMSEditCommand component.
     * @return the initialized component instance
     */
    public Command getCancelSMSEditCommand() {
        if (cancelSMSEditCommand == null) {//GEN-END:|322-getter|0|322-preInit
            // write pre-init user code here
            cancelSMSEditCommand = new Command("Cancel", Command.CANCEL, 0);//GEN-LINE:|322-getter|1|322-postInit
            // write post-init user code here
        }//GEN-BEGIN:|322-getter|2|
        return cancelSMSEditCommand;
    }
    //</editor-fold>//GEN-END:|322-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okSendSMSCommand ">//GEN-BEGIN:|326-getter|0|326-preInit
    /**
     * Returns an initiliazed instance of okSendSMSCommand component.
     * @return the initialized component instance
     */
    public Command getOkSendSMSCommand() {
        if (okSendSMSCommand == null) {//GEN-END:|326-getter|0|326-preInit
            // write pre-init user code here
            okSendSMSCommand = new Command("Ok", Command.OK, 0);//GEN-LINE:|326-getter|1|326-postInit
            // write post-init user code here
        }//GEN-BEGIN:|326-getter|2|
        return okSendSMSCommand;
    }
    //</editor-fold>//GEN-END:|326-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cancelSendSMSCommand ">//GEN-BEGIN:|328-getter|0|328-preInit
    /**
     * Returns an initiliazed instance of cancelSendSMSCommand component.
     * @return the initialized component instance
     */
    public Command getCancelSendSMSCommand() {
        if (cancelSendSMSCommand == null) {//GEN-END:|328-getter|0|328-preInit
            // write pre-init user code here
            cancelSendSMSCommand = new Command("Cancel", Command.CANCEL, 0);//GEN-LINE:|328-getter|1|328-postInit
            // write post-init user code here
        }//GEN-BEGIN:|328-getter|2|
        return cancelSendSMSCommand;
    }
    //</editor-fold>//GEN-END:|328-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: doSendSMS ">//GEN-BEGIN:|331-entry|0|332-preAction
    /**
     * Performs an action assigned to the doSendSMS entry-point.
     */
    public void doSendSMS() {//GEN-END:|331-entry|0|332-preAction

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
//GEN-LINE:|331-entry|1|332-postAction
        switchDisplayable(null, getWaypointScreen());
    }//GEN-BEGIN:|331-entry|2|
    //</editor-fold>//GEN-END:|331-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smsLengthStringItem ">//GEN-BEGIN:|334-getter|0|334-preInit
    /**
     * Returns an initiliazed instance of smsLengthStringItem component.
     * @return the initialized component instance
     */
    public StringItem getSmsLengthStringItem() {
        if (smsLengthStringItem == null) {//GEN-END:|334-getter|0|334-preInit
            // write pre-init user code here
            smsLengthStringItem = new StringItem("Message length: ", null);//GEN-LINE:|334-getter|1|334-postInit
            // write post-init user code here
        }//GEN-BEGIN:|334-getter|2|
        return smsLengthStringItem;
    }
    //</editor-fold>//GEN-END:|334-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smsTextStringItem ">//GEN-BEGIN:|335-getter|0|335-preInit
    /**
     * Returns an initiliazed instance of smsTextStringItem component.
     * @return the initialized component instance
     */
    public StringItem getSmsTextStringItem() {
        if (smsTextStringItem == null) {//GEN-END:|335-getter|0|335-preInit
            // write pre-init user code here
            smsTextStringItem = new StringItem("Message text: ", null);//GEN-LINE:|335-getter|1|335-postInit
            // write post-init user code here
        }//GEN-BEGIN:|335-getter|2|
        return smsTextStringItem;
    }
    //</editor-fold>//GEN-END:|335-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: switchToMainScreen ">//GEN-BEGIN:|338-entry|0|339-preAction
    /**
     * Performs an action assigned to the switchToMainScreen entry-point.
     */
    public void switchToMainScreen() {//GEN-END:|338-entry|0|339-preAction
        switchDisplayable(null, getMainScreen());
//GEN-LINE:|338-entry|1|339-postAction
        // write post-action user code here
    }//GEN-BEGIN:|338-entry|2|
    //</editor-fold>//GEN-END:|338-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logFileNamePrefixTextField ">//GEN-BEGIN:|346-getter|0|346-preInit
    /**
     * Returns an initiliazed instance of logFileNamePrefixTextField component.
     * @return the initialized component instance
     */
    public TextField getLogFileNamePrefixTextField() {
        if (logFileNamePrefixTextField == null) {//GEN-END:|346-getter|0|346-preInit
            // write pre-init user code here
            logFileNamePrefixTextField = new TextField("Log file name prefix", "GPSLogger", 32, TextField.ANY);//GEN-LINE:|346-getter|1|346-postInit
            // write post-init user code here
        }//GEN-BEGIN:|346-getter|2|
        return logFileNamePrefixTextField;
    }
    //</editor-fold>//GEN-END:|346-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: minimizeCommand ">//GEN-BEGIN:|347-getter|0|347-preInit
    /**
     * Returns an initiliazed instance of minimizeCommand component.
     * @return the initialized component instance
     */
    public Command getMinimizeCommand() {
        if (minimizeCommand == null) {//GEN-END:|347-getter|0|347-preInit
            // write pre-init user code here
            minimizeCommand = new Command(GPSLoggerLocalization.getMessage("minimizeCommand"), Command.OK, 0);//GEN-LINE:|347-getter|1|347-postInit
            // write post-init user code here
        }//GEN-BEGIN:|347-getter|2|
        return minimizeCommand;
    }
    //</editor-fold>//GEN-END:|347-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: spacer2 ">//GEN-BEGIN:|349-getter|0|349-preInit
    /**
     * Returns an initiliazed instance of spacer2 component.
     * @return the initialized component instance
     */
    public Spacer getSpacer2() {
        if (spacer2 == null) {//GEN-END:|349-getter|0|349-preInit
            // write pre-init user code here
            spacer2 = new Spacer(16, 1);//GEN-LINE:|349-getter|1|349-postInit
            // write post-init user code here
        }//GEN-BEGIN:|349-getter|2|
        return spacer2;
    }
    //</editor-fold>//GEN-END:|349-getter|2|
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: totalMemoryStringItem ">//GEN-BEGIN:|350-getter|0|350-preInit
    /**
     * Returns an initiliazed instance of totalMemoryStringItem component.
     * @return the initialized component instance
     */
    public StringItem getTotalMemoryStringItem() {
        if (totalMemoryStringItem == null) {//GEN-END:|350-getter|0|350-preInit
            // write pre-init user code here
            totalMemoryStringItem = new StringItem("total memory:", null);//GEN-LINE:|350-getter|1|350-postInit
            // write post-init user code here
        }//GEN-BEGIN:|350-getter|2|
        return totalMemoryStringItem;
    }
    //</editor-fold>//GEN-END:|350-getter|2|
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: freeMemoryStringItem ">//GEN-BEGIN:|351-getter|0|351-preInit
    /**
     * Returns an initiliazed instance of freeMemoryStringItem component.
     * @return the initialized component instance
     */
    public StringItem getFreeMemoryStringItem() {
        if (freeMemoryStringItem == null) {//GEN-END:|351-getter|0|351-preInit
            // write pre-init user code here
            freeMemoryStringItem = new StringItem("free memory:", null);//GEN-LINE:|351-getter|1|351-postInit
            // write post-init user code here
        }//GEN-BEGIN:|351-getter|2|
        return freeMemoryStringItem;
    }
    //</editor-fold>//GEN-END:|351-getter|2|
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: mapDescriptorFileTextField ">//GEN-BEGIN:|352-getter|0|352-preInit
    /**
     * Returns an initiliazed instance of mapDescriptorFileTextField component.
     * @return the initialized component instance
     */
    public TextField getMapDescriptorFileTextField() {
        if (mapDescriptorFileTextField == null) {//GEN-END:|352-getter|0|352-preInit
            // write pre-init user code here
            mapDescriptorFileTextField = new TextField("Map descriptor file", "", 4096, TextField.ANY);//GEN-BEGIN:|352-getter|1|352-postInit
            mapDescriptorFileTextField.addCommand(getBrowseMapDescriptorFileCommand());
            mapDescriptorFileTextField.setItemCommandListener(this);
            mapDescriptorFileTextField.setDefaultCommand(getBrowseMapDescriptorFileCommand());//GEN-END:|352-getter|1|352-postInit
            // write post-init user code here
        }//GEN-BEGIN:|352-getter|2|
        return mapDescriptorFileTextField;
    }
    //</editor-fold>//GEN-END:|352-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: spacer3 ">//GEN-BEGIN:|353-getter|0|353-preInit
    /**
     * Returns an initiliazed instance of spacer3 component.
     * @return the initialized component instance
     */
    public Spacer getSpacer3() {
        if (spacer3 == null) {//GEN-END:|353-getter|0|353-preInit
            // write pre-init user code here
            spacer3 = new Spacer(16, 1);//GEN-LINE:|353-getter|1|353-postInit
            // write post-init user code here
        }//GEN-BEGIN:|353-getter|2|
        return spacer3;
    }
    //</editor-fold>//GEN-END:|353-getter|2|


    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: browseMapDescriptorFileStringItem ">//GEN-BEGIN:|361-getter|0|361-preInit
    /**
     * Returns an initiliazed instance of browseMapDescriptorFileStringItem component.
     * @return the initialized component instance
     */
    public StringItem getBrowseMapDescriptorFileStringItem() {
        if (browseMapDescriptorFileStringItem == null) {//GEN-END:|361-getter|0|361-preInit
            // write pre-init user code here
            browseMapDescriptorFileStringItem = new StringItem("///browse map descriptor file...", "", Item.BUTTON);//GEN-BEGIN:|361-getter|1|361-postInit
            browseMapDescriptorFileStringItem.addCommand(getBrowseMapDescriptorFileCommand());
            browseMapDescriptorFileStringItem.setItemCommandListener(this);
            browseMapDescriptorFileStringItem.setDefaultCommand(getBrowseMapDescriptorFileCommand());
            browseMapDescriptorFileStringItem.setFont(getFont());//GEN-END:|361-getter|1|361-postInit
            // write post-init user code here
        }//GEN-BEGIN:|361-getter|2|
        return browseMapDescriptorFileStringItem;
    }
    //</editor-fold>//GEN-END:|361-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: browseMapDescriptorFileCommand ">//GEN-BEGIN:|362-getter|0|362-preInit
    /**
     * Returns an initiliazed instance of browseMapDescriptorFileCommand component.
     * @return the initialized component instance
     */
    public Command getBrowseMapDescriptorFileCommand() {
        if (browseMapDescriptorFileCommand == null) {//GEN-END:|362-getter|0|362-preInit
            // write pre-init user code here
            browseMapDescriptorFileCommand = new Command("Ok", Command.OK, 0);//GEN-LINE:|362-getter|1|362-postInit
            // write post-init user code here
        }//GEN-BEGIN:|362-getter|2|
        return browseMapDescriptorFileCommand;
    }
    //</editor-fold>//GEN-END:|362-getter|2|
    //</editor-fold>
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: gpsDeviceURLStartScreenStringItem ">//GEN-BEGIN:|364-getter|0|364-preInit
    /**
     * Returns an initiliazed instance of gpsDeviceURLStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getGpsDeviceURLStartScreenStringItem() {
        if (gpsDeviceURLStartScreenStringItem == null) {//GEN-END:|364-getter|0|364-preInit
            // write pre-init user code here
            gpsDeviceURLStartScreenStringItem = new StringItem("", "");//GEN-LINE:|364-getter|1|364-postInit
            // write post-init user code here
        }//GEN-BEGIN:|364-getter|2|
        return gpsDeviceURLStartScreenStringItem;
    }
    //</editor-fold>//GEN-END:|364-getter|2|
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: gpsDeviceNameStringItem ">//GEN-BEGIN:|365-getter|0|365-preInit
    /**
     * Returns an initiliazed instance of gpsDeviceNameStringItem component.
     * @return the initialized component instance
     */
    public StringItem getGpsDeviceNameStringItem() {
        if (gpsDeviceNameStringItem == null) {//GEN-END:|365-getter|0|365-preInit
            // write pre-init user code here
            gpsDeviceNameStringItem = new StringItem("GPS name: ", "");//GEN-LINE:|365-getter|1|365-postInit
            // write post-init user code here
        }//GEN-BEGIN:|365-getter|2|
        return gpsDeviceNameStringItem;
    }
    //</editor-fold>//GEN-END:|365-getter|2|
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: mapDescriptorFileStartScreenStringItem ">//GEN-BEGIN:|366-getter|0|366-preInit
    /**
     * Returns an initiliazed instance of mapDescriptorFileStartScreenStringItem component.
     * @return the initialized component instance
     */
    public StringItem getMapDescriptorFileStartScreenStringItem() {
        if (mapDescriptorFileStartScreenStringItem == null) {//GEN-END:|366-getter|0|366-preInit
            // write pre-init user code here
            mapDescriptorFileStartScreenStringItem = new StringItem("Map: ", "");//GEN-LINE:|366-getter|1|366-postInit
            // write post-init user code here
        }//GEN-BEGIN:|366-getter|2|
        return mapDescriptorFileStartScreenStringItem;
    }
    //</editor-fold>//GEN-END:|366-getter|2|

    public FileBrowser getFileBrowser() {
        if (fileBrowser == null) {
            fileBrowser = new FileBrowser(getDisplay(), true, false); // folders only
            fileBrowser.setTitle("Select log folder");
            fileBrowser.setCommandListener (this);
            fileBrowser.addCommand(FileBrowser.SELECT_ITEM_COMMAND);
            fileBrowser.addCommand(getBackCommand ());  
        }
        return fileBrowser;
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
