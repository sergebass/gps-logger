/*
 * (C) Serge Perinsky, 2007, 2008, 2009
 */

import com.sergebass.geography.*;
import com.sergebass.bluetooth.BluetoothManager;
import com.sergebass.ui.FileBrowser;
import com.sergebass.util.Instant;

import java.util.Vector;
import java.io.*;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.media.*;
import javax.bluetooth.*;

/**
 * @author Serge Perinsky
 */
public class GPSLogger
        extends MIDlet
        implements CommandListener,
                   ItemCommandListener,
                   GeoLocationListener {

    static final String ERROR_AUDIO_FILE = "sounds/error.wav";
    
    private boolean mustBeTerminated = false;
    private boolean midletPaused = false;

    final Object waitingLock = new Object();
            
    GPSLoggerSettings settings = null;
    
    Vector deviceServiceRecords = new Vector(); /* <ServiceRecord> */
    
    int number = 0;

    GeoLocator geoLocator = null;
    
    GPSLogFile trackLogFile = null;
    GPXWriter trackLogWriter = null;
    final Object trackLogLock = new Object();
    
    GPSLogFile waypointLogFile = null;
    GPXWriter waypointLogWriter = null;
    final Object waypointLogLock = new Object();
    
    Vector waypoints = null;

    FileBrowser fileBrowser;
        
    Player errorPlayer = null;

    GPSScreen mainScreen = null;

    long startTimeMillis = 0L;

    //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private java.util.Hashtable __previousDisplayables = new java.util.Hashtable();
    private Command submitWaypointCommand;
    private Command cancelWaypointCommand;
    private Command exitCommand;
    private Command takePhotoCommand;
    private Command searchCommand;
    private Command startCommand;
    private Command settingsCommand;
    private Command helpCommand;
    private Command backCommand;
    private Command saveSettingsCommand;
    private Command browseCommand;
    private Command markCommand;
    private Command cancelCommand;
    private Command sendEmailCommand;
    private Command resetCommand;
    private Command okCommand;
    private Command stopCommand;
    private Form waypointForm;
    private TextField waypointNameTextField;
    private List deviceList;
    private Form introForm;
    private StringItem gpsDeviceStringItem;
    private StringItem freeSpaceStringItem;
    private StringItem logPathStringItem;
    private Form settingsForm;
    private Spacer spacer1;
    private StringItem browseLogFolderStringItem;
    private Spacer spacer;
    private StringItem searchGPSStringItem;
    private ChoiceGroup coordinateChoiceGroup;
    private ChoiceGroup speedChoiceGroup;
    private TextField gpsDeviceTextField;
    private TextField logFolderTextField;
    private ChoiceGroup altitudeChoiceGroup;
    private ChoiceGroup languageChoiceGroup;
    private Form helpForm;
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
            ///ignore?
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
        switchDisplayable(null, getIntroForm());//GEN-LINE:|3-startMIDlet|1|3-postAction
        showSettings();
    }//GEN-BEGIN:|3-startMIDlet|2|
    //</editor-fold>//GEN-END:|3-startMIDlet|2|
    
    //<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
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
        if (displayable == deviceList) {//GEN-BEGIN:|7-commandAction|1|87-preAction
            if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|1|87-preAction
                // write pre-action user code here
                deviceListAction();//GEN-LINE:|7-commandAction|2|87-postAction
                // write post-action user code here
            } else if (command == cancelCommand) {//GEN-LINE:|7-commandAction|3|217-preAction
                // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|4|217-postAction
                // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|5|243-preAction
                getDeviceList().setTitle("Wait...");
                selectGPSDevice();//GEN-LINE:|7-commandAction|6|243-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|7|150-preAction
        } else if (displayable == helpForm) {
            if (command == backCommand) {//GEN-END:|7-commandAction|7|150-preAction
                // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|8|150-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|9|128-preAction
        } else if (displayable == introForm) {
            if (command == exitCommand) {//GEN-END:|7-commandAction|9|128-preAction
                // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|10|128-postAction
                // write post-action user code here
            } else if (command == helpCommand) {//GEN-LINE:|7-commandAction|11|146-preAction
                // write pre-action user code here
                switchDisplayable(null, getHelpForm());//GEN-LINE:|7-commandAction|12|146-postAction
                // write post-action user code here
            } else if (command == settingsCommand) {//GEN-LINE:|7-commandAction|13|153-preAction
                // write pre-action user code here
                switchDisplayable(null, getSettingsForm());//GEN-LINE:|7-commandAction|14|153-postAction
                // write post-action user code here
            } else if (command == startCommand) {//GEN-LINE:|7-commandAction|15|143-preAction
                // write pre-action user code here
                startTrack();//GEN-LINE:|7-commandAction|16|143-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|17|215-preAction
        } else if (displayable == settingsForm) {
            if (command == cancelCommand) {//GEN-END:|7-commandAction|17|215-preAction
                // write pre-action user code here
                switchDisplayable(null, getIntroForm());//GEN-LINE:|7-commandAction|18|215-postAction
                // write post-action user code here
            } else if (command == saveSettingsCommand) {//GEN-LINE:|7-commandAction|19|202-preAction
                // write pre-action user code here
                saveSettings();//GEN-LINE:|7-commandAction|20|202-postAction
                showSettings();
                switchDisplayable(null, getIntroForm());
            }//GEN-BEGIN:|7-commandAction|21|282-preAction
        } else if (displayable == waypointForm) {
            if (command == cancelWaypointCommand) {//GEN-END:|7-commandAction|21|282-preAction
                // write pre-action user code here
//GEN-LINE:|7-commandAction|22|282-postAction
                // write post-action user code here
            } else if (command == submitWaypointCommand) {//GEN-LINE:|7-commandAction|23|284-preAction
                // write pre-action user code here
                markPoint();//GEN-LINE:|7-commandAction|24|284-postAction
                // write post-action user code here
            } else if (command == takePhotoCommand) {//GEN-LINE:|7-commandAction|25|291-preAction
                // write pre-action user code here
                takePhoto();//GEN-LINE:|7-commandAction|26|291-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|27|7-postCommandAction
        }//GEN-END:|7-commandAction|27|7-postCommandAction
        else if (displayable == fileBrowser) {
            if (command == FileBrowser.SELECT_ITEM_COMMAND) {
                setLogFolder ();
            } else if (command == backCommand) {
                switchToPreviousDisplayable ();
            }
        } else { // all other displayables
            if (command.getCommandType() == Command.EXIT) {
                exitMIDlet();
            } else if (command.getCommandType() == Command.BACK) {
                switchToPreviousDisplayable();
            } else if (command == stopCommand) {
                stopTrack();
            } else if (command == markCommand) {
                switchDisplayable(null, getWaypointForm());
            } else if (command == resetCommand) {
                resetOdometer();
            }
        }
    }//GEN-BEGIN:|7-commandAction|28|
    //</editor-fold>//GEN-END:|7-commandAction|28|


    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|18-getter|0|18-preInit
    /**
     * Returns an initiliazed instance of exitCommand component.
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {//GEN-END:|18-getter|0|18-preInit
            // write pre-init user code here
            exitCommand = new Command(GPSLoggerLocalization.getMessage("exitCommand"), Command.EXIT, 2);//GEN-LINE:|18-getter|1|18-postInit
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

        // switch to the found device list
        switchDisplayable(null, getDeviceList());

        getDeviceList().setTitle("Searching GPS...");
        System.out.println("Searching GPS devices...");

        getDeviceList().deleteAll();

        BluetoothManager deviceManager = new BluetoothManager(this);

        UUID[] uuidServiceSet = new UUID[1];
        uuidServiceSet[0] = new UUID(0x1101); // SPP service (Serial Port Profile)

        Vector devices = deviceManager.searchDevices();

        if (devices.size() == 0) {
            getDeviceList().append("[No GPS devices found]", null);///or special icon?
        } else {
            for (int i = 0; i < devices.size(); i++) {

                RemoteDevice device = (RemoteDevice)devices.elementAt(i);
                System.out.println(device.toString());

                // now let's see if this device supports the needed services...
                Vector services = deviceManager.searchServices(device, uuidServiceSet);

                for (int iService = 0; iService < services.size(); iService++) {

                    ServiceRecord service = (ServiceRecord)services.elementAt(iService);
                    deviceServiceRecords.addElement(service);

                    try { // add matching entry to the selection list
                        getDeviceList().append(device.getFriendlyName(false) // do not ask
                                             + " ("
                                             + service.getHostDevice().getBluetoothAddress()
                                             + ")",
                                           null); // no image
                    } catch (IOException ex) {
                        getDeviceList().append
                                    ("!!"
                                    + ex.getMessage()
                                    + " (" + ex.getClass().getName()
                                    + ")",
                                    null); /// or error icon?
                        getDisplay().vibrate(1000);
                    }
                }
            }
        }
//GEN-LINE:|82-entry|1|83-postAction
        getDeviceList().setTitle("Choose GPS device:");
        System.out.println("GPS device scan complete.");
    }//GEN-BEGIN:|82-entry|2|
    //</editor-fold>//GEN-END:|82-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: deviceList ">//GEN-BEGIN:|85-getter|0|85-preInit
    /**
     * Returns an initiliazed instance of deviceList component.
     * @return the initialized component instance
     */
    public List getDeviceList() {
        if (deviceList == null) {//GEN-END:|85-getter|0|85-preInit
            // write pre-init user code here
            deviceList = new List("GPS devices", Choice.IMPLICIT);//GEN-BEGIN:|85-getter|1|85-postInit
            deviceList.addCommand(getCancelCommand());
            deviceList.addCommand(getOkCommand());
            deviceList.setCommandListener(this);
            deviceList.setFitPolicy(Choice.TEXT_WRAP_ON);
            deviceList.setSelectCommand(getOkCommand());//GEN-END:|85-getter|1|85-postInit
            // write post-init user code here
        }//GEN-BEGIN:|85-getter|2|
        return deviceList;
    }
    //</editor-fold>//GEN-END:|85-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: deviceListAction ">//GEN-BEGIN:|85-action|0|85-preAction
    /**
     * Performs an action assigned to the selected list element in the deviceList component.
     */
    public void deviceListAction() {//GEN-END:|85-action|0|85-preAction
        // enter pre-action user code here
        String __selectedString = getDeviceList().getString(getDeviceList().getSelectedIndex());//GEN-LINE:|85-action|1|85-postAction
        selectGPSDevice();
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
        waypoints = new Vector(); // (re)initialize
        mainScreen = new GPSScreen(this);
        go(mainScreen); /// or getMainForm() for text mode
    }

    void go(final Displayable screen) {
        switchDisplayable(null, screen);
        screen.setTitle("Connecting to GPS...");
        System.out.println("Connecting to GPS receiver...");

        // run our tracking stuff in a separate thread
        new Thread() {
            public void run() {
                startTracking(screen);
            }
        }.start();
//GEN-LINE:|108-entry|1|109-postAction
    }//GEN-BEGIN:|108-entry|2|
    //</editor-fold>//GEN-END:|108-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: introForm ">//GEN-BEGIN:|125-getter|0|125-preInit
    /**
     * Returns an initiliazed instance of introForm component.
     * @return the initialized component instance
     */
    public Form getIntroForm() {
        if (introForm == null) {//GEN-END:|125-getter|0|125-preInit
            // write pre-init user code here
            introForm = new Form("GPS Logger", new Item[] { getGpsDeviceStringItem(), getLogPathStringItem(), getFreeSpaceStringItem() });//GEN-BEGIN:|125-getter|1|125-postInit
            introForm.addCommand(getStartCommand());
            introForm.addCommand(getSettingsCommand());
            introForm.addCommand(getExitCommand());
            introForm.addCommand(getHelpCommand());
            introForm.setCommandListener(this);//GEN-END:|125-getter|1|125-postInit

        }//GEN-BEGIN:|125-getter|2|
        return introForm;
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
            helpCommand = new Command(GPSLoggerLocalization.getMessage("helpCommand"), Command.HELP, 5);//GEN-LINE:|145-getter|1|145-postInit
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: settingsForm ">//GEN-BEGIN:|141-getter|0|141-preInit
    /**
     * Returns an initiliazed instance of settingsForm component.
     * @return the initialized component instance
     */
    public Form getSettingsForm() {
        if (settingsForm == null) {//GEN-END:|141-getter|0|141-preInit
            // write pre-init user code here
            settingsForm = new Form(GPSLoggerLocalization.getMessage("Settings"), new Item[] { getGpsDeviceTextField(), getSearchGPSStringItem(), getSpacer1(), getLogFolderTextField(), getBrowseLogFolderStringItem(), getSpacer(), getCoordinateChoiceGroup(), getAltitudeChoiceGroup(), getSpeedChoiceGroup(), getLanguageChoiceGroup() });//GEN-BEGIN:|141-getter|1|141-postInit
            settingsForm.addCommand(getSaveSettingsCommand());
            settingsForm.addCommand(getCancelCommand());
            settingsForm.setCommandListener(this);//GEN-END:|141-getter|1|141-postInit
            // write post-init user code here
        }//GEN-BEGIN:|141-getter|2|
        return settingsForm;
    }
    //</editor-fold>//GEN-END:|141-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: helpForm ">//GEN-BEGIN:|147-getter|0|147-preInit
    /**
     * Returns an initiliazed instance of helpForm component.
     * @return the initialized component instance
     */
    public Form getHelpForm() {
        if (helpForm == null) {//GEN-END:|147-getter|0|147-preInit
            // write pre-init user code here
            helpForm = new Form("Help", new Item[] { getStringItem1(), getEmailItem() });//GEN-BEGIN:|147-getter|1|147-postInit
            helpForm.addCommand(getBackCommand());
            helpForm.setCommandListener(this);//GEN-END:|147-getter|1|147-postInit
            // write post-init user code here
        }//GEN-BEGIN:|147-getter|2|
        return helpForm;
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
            settingsCommand = new Command(GPSLoggerLocalization.getMessage("settingsCommand"), GPSLoggerLocalization.getMessage("settingsCommand"), Command.SCREEN, 1);//GEN-LINE:|152-getter|1|152-postInit
            // write post-init user code here
        }//GEN-BEGIN:|152-getter|2|
        return settingsCommand;
    }
    //</editor-fold>//GEN-END:|152-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: coordinateChoiceGroup ">//GEN-BEGIN:|157-getter|0|157-preInit
    /**
     * Returns an initiliazed instance of coordinateChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getCoordinateChoiceGroup() {
        if (coordinateChoiceGroup == null) {//GEN-END:|157-getter|0|157-preInit
            // write pre-init user code here
            coordinateChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Coordinates"), Choice.EXCLUSIVE);//GEN-BEGIN:|157-getter|1|157-postInit
            coordinateChoiceGroup.append("DD.dd\u00B0", null);
            coordinateChoiceGroup.append("DD\u00B0 MM.mm\'", null);
            coordinateChoiceGroup.append("DD\u00B0 MM\' SS.ss\"", null);
            coordinateChoiceGroup.setSelectedFlags(new boolean[] { false, false, false });
            coordinateChoiceGroup.setFont(0, null);
            coordinateChoiceGroup.setFont(1, null);
            coordinateChoiceGroup.setFont(2, null);//GEN-END:|157-getter|1|157-postInit
            // write post-init user code here
        }//GEN-BEGIN:|157-getter|2|
        return coordinateChoiceGroup;
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: gpsDeviceTextField ">//GEN-BEGIN:|161-getter|0|161-preInit
    /**
     * Returns an initiliazed instance of gpsDeviceTextField component.
     * @return the initialized component instance
     */
    public TextField getGpsDeviceTextField() {
        if (gpsDeviceTextField == null) {//GEN-END:|161-getter|0|161-preInit
            // write pre-init user code here
            gpsDeviceTextField = new TextField(GPSLoggerLocalization.getMessage("GPSDevice"), "", 4096, TextField.ANY);//GEN-BEGIN:|161-getter|1|161-postInit
            gpsDeviceTextField.addCommand(getSearchCommand());
            gpsDeviceTextField.setItemCommandListener(this);//GEN-END:|161-getter|1|161-postInit
            // write post-init user code here
        }//GEN-BEGIN:|161-getter|2|
        return gpsDeviceTextField;
    }
    //</editor-fold>//GEN-END:|161-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Items ">//GEN-BEGIN:|17-itemCommandAction|0|17-preItemCommandAction
    /**
     * Called by a system to indicated that a command has been invoked on a particular item.
     * @param command the Command that was invoked
     * @param displayable the Item where the command was invoked
     */
    public void commandAction(Command command, Item item) {//GEN-END:|17-itemCommandAction|0|17-preItemCommandAction
        // write pre-action user code here
        if (item == browseLogFolderStringItem) {//GEN-BEGIN:|17-itemCommandAction|1|272-preAction
            if (command == browseCommand) {//GEN-END:|17-itemCommandAction|1|272-preAction
            // write pre-action user code here
                browseLogFolder();//GEN-LINE:|17-itemCommandAction|2|272-postAction
            // write post-action user code here
            }//GEN-BEGIN:|17-itemCommandAction|3|210-preAction
        } else if (item == emailItem) {
            if (command == sendEmailCommand) {//GEN-END:|17-itemCommandAction|3|210-preAction
                // write pre-action user code here
                sendEmail();//GEN-LINE:|17-itemCommandAction|4|210-postAction
                // write post-action user code here
            }//GEN-BEGIN:|17-itemCommandAction|5|236-preAction
        } else if (item == gpsDeviceTextField) {
            if (command == searchCommand) {//GEN-END:|17-itemCommandAction|5|236-preAction
                // write pre-action user code here
                searchDevices();//GEN-LINE:|17-itemCommandAction|6|236-postAction
                // write post-action user code here
            }//GEN-BEGIN:|17-itemCommandAction|7|181-preAction
        } else if (item == logFolderTextField) {
            if (command == browseCommand) {//GEN-END:|17-itemCommandAction|7|181-preAction
                browseLogFolder();//GEN-LINE:|17-itemCommandAction|8|181-postAction
                // write post-action user code here
            }//GEN-BEGIN:|17-itemCommandAction|9|269-preAction
        } else if (item == searchGPSStringItem) {
            if (command == searchCommand) {//GEN-END:|17-itemCommandAction|9|269-preAction
            // write pre-action user code here
                searchDevices();//GEN-LINE:|17-itemCommandAction|10|269-postAction
            // write post-action user code here
            }//GEN-BEGIN:|17-itemCommandAction|11|17-postItemCommandAction
        }//GEN-END:|17-itemCommandAction|11|17-postItemCommandAction
        // write post-action user code here
    }//GEN-BEGIN:|17-itemCommandAction|12|
    //</editor-fold>//GEN-END:|17-itemCommandAction|12|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: speedChoiceGroup ">//GEN-BEGIN:|163-getter|0|163-preInit
    /**
     * Returns an initiliazed instance of speedChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getSpeedChoiceGroup() {
        if (speedChoiceGroup == null) {//GEN-END:|163-getter|0|163-preInit
            // write pre-init user code here
            speedChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Speed"), Choice.EXCLUSIVE);//GEN-BEGIN:|163-getter|1|163-postInit
            speedChoiceGroup.append("km/h", null);
            speedChoiceGroup.append("mph", null);
            speedChoiceGroup.append("knots", null);
            speedChoiceGroup.setSelectedFlags(new boolean[] { false, false, false });
            speedChoiceGroup.setFont(0, null);
            speedChoiceGroup.setFont(1, null);
            speedChoiceGroup.setFont(2, null);//GEN-END:|163-getter|1|163-postInit
            // write post-init user code here
        }//GEN-BEGIN:|163-getter|2|
        return speedChoiceGroup;
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
        switchDisplayable(null, getSettingsForm());
//GEN-LINE:|183-entry|1|184-postAction
        // write post-action user code here
    }//GEN-BEGIN:|183-entry|2|
    //</editor-fold>//GEN-END:|183-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: saveSettings ">//GEN-BEGIN:|188-entry|0|189-preAction
    /**
     * Performs an action assigned to the saveSettings entry-point.
     */
    public void saveSettings() {//GEN-END:|188-entry|0|189-preAction

        String gpsDeviceURL = gpsDeviceTextField.getString();
        settings.setGPSDeviceURL(gpsDeviceURL);
        gpsDeviceStringItem.setText(gpsDeviceTextField.getString()); // copy to the initial screen

        String logPath = logFolderTextField.getString();
        settings.setLogFolder(logPath);
        logPathStringItem.setText(logPath);

        try {
            settings.save();
        } catch (RecordStoreException e) {
///handle UI
            handleException(e, introForm);

            return;
        }
//GEN-LINE:|188-entry|1|189-postAction
        switchDisplayable(null, getMainScreen());
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: markPoint ">//GEN-BEGIN:|197-entry|0|198-preAction
    /**
     * Performs an action assigned to the markPoint entry-point.
     */
    public void markPoint() {//GEN-END:|197-entry|0|198-preAction

        switchDisplayable(null, getMainScreen()); // go back to the main screen immediately anyway

        try {
            saveCurrentWaypoint(getWaypointNameTextField().getString().trim());
        } catch (IOException e) {
            handleException(e, getMainScreen());
        }
//GEN-LINE:|197-entry|1|198-postAction
    }//GEN-BEGIN:|197-entry|2|
    //</editor-fold>//GEN-END:|197-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: markCommand ">//GEN-BEGIN:|195-getter|0|195-preInit
    /**
     * Returns an initiliazed instance of markCommand component.
     * @return the initialized component instance
     */
    public Command getMarkCommand() {
        if (markCommand == null) {//GEN-END:|195-getter|0|195-preInit
            // write pre-init user code here
            markCommand = new Command(GPSLoggerLocalization.getMessage("markCommand"), GPSLoggerLocalization.getMessage("markCommandLong"), Command.OK, 0);//GEN-LINE:|195-getter|1|195-postInit
            // write post-init user code here
        }//GEN-BEGIN:|195-getter|2|
        return markCommand;
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
            handleException(e, introForm);
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: altitudeChoiceGroup ">//GEN-BEGIN:|220-getter|0|220-preInit
    /**
     * Returns an initiliazed instance of altitudeChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getAltitudeChoiceGroup() {
        if (altitudeChoiceGroup == null) {//GEN-END:|220-getter|0|220-preInit
            // write pre-init user code here
            altitudeChoiceGroup = new ChoiceGroup(GPSLoggerLocalization.getMessage("Altitude"), Choice.EXCLUSIVE);//GEN-BEGIN:|220-getter|1|220-postInit
            altitudeChoiceGroup.append("meters", null);
            altitudeChoiceGroup.append("feet", null);
            altitudeChoiceGroup.setSelectedFlags(new boolean[] { false, false });
            altitudeChoiceGroup.setFont(0, null);
            altitudeChoiceGroup.setFont(1, null);//GEN-END:|220-getter|1|220-postInit
            // write post-init user code here
        }//GEN-BEGIN:|220-getter|2|
        return altitudeChoiceGroup;
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
            languageChoiceGroup.setSelectedFlags(new boolean[] { false, false, false });
            languageChoiceGroup.setFont(0, null);
            languageChoiceGroup.setFont(1, null);
            languageChoiceGroup.setFont(2, null);//GEN-END:|223-getter|1|223-postInit
            // write post-init user code here
        }//GEN-BEGIN:|223-getter|2|
        return languageChoiceGroup;
    }
    //</editor-fold>//GEN-END:|223-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: selectGPSDevice ">//GEN-BEGIN:|227-entry|0|228-preAction
    /**
     * Performs an action assigned to the selectGPSDevice entry-point.
     */
    public void selectGPSDevice() {//GEN-END:|227-entry|0|228-preAction

        int selectedDeviceIndex = getDeviceList().getSelectedIndex();

        final ServiceRecord service = (ServiceRecord)deviceServiceRecords.elementAt(selectedDeviceIndex);
        System.out.println("Chosen device: " + service.getHostDevice().getBluetoothAddress());
        System.out.println("Chosen service: " + service.toString());

        getDeviceList().setTitle("Checking GPS...");

        String connectionURLString = service.getConnectionURL(0, // security
                                                              true); // master mode
        if(connectionURLString==null){
            handleException(new Exception("Device does not support Simple SPP Service."),
                            introForm);
            return;
        }

        settings.setGPSDeviceURL(connectionURLString);
        getGpsDeviceTextField().setString(connectionURLString);
        switchDisplayable(null, getSettingsForm());//GEN-LINE:|227-entry|1|228-postAction
        // write post-action user code here
    }//GEN-BEGIN:|227-entry|2|
    //</editor-fold>//GEN-END:|227-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: gpsDeviceStringItem ">//GEN-BEGIN:|232-getter|0|232-preInit
    /**
     * Returns an initiliazed instance of gpsDeviceStringItem component.
     * @return the initialized component instance
     */
    public StringItem getGpsDeviceStringItem() {
        if (gpsDeviceStringItem == null) {//GEN-END:|232-getter|0|232-preInit
            // write pre-init user code here
            gpsDeviceStringItem = new StringItem("GPS: ", "");//GEN-BEGIN:|232-getter|1|232-postInit
            gpsDeviceStringItem.setLayout(ImageItem.LAYOUT_DEFAULT | Item.LAYOUT_VSHRINK);//GEN-END:|232-getter|1|232-postInit
            // write post-init user code here
        }//GEN-BEGIN:|232-getter|2|
        return gpsDeviceStringItem;
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logPathStringItem ">//GEN-BEGIN:|249-getter|0|249-preInit
    /**
     * Returns an initiliazed instance of logPathStringItem component.
     * @return the initialized component instance
     */
    public StringItem getLogPathStringItem() {
        if (logPathStringItem == null) {//GEN-END:|249-getter|0|249-preInit
            // write pre-init user code here
            logPathStringItem = new StringItem("Log: ", "");//GEN-BEGIN:|249-getter|1|249-postInit
            logPathStringItem.setLayout(ImageItem.LAYOUT_DEFAULT | Item.LAYOUT_VSHRINK);//GEN-END:|249-getter|1|249-postInit
            // write post-init user code here
        }//GEN-BEGIN:|249-getter|2|
        return logPathStringItem;
    }
    //</editor-fold>//GEN-END:|249-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: freeSpaceStringItem ">//GEN-BEGIN:|250-getter|0|250-preInit
    /**
     * Returns an initiliazed instance of freeSpaceStringItem component.
     * @return the initialized component instance
     */
    public StringItem getFreeSpaceStringItem() {
        if (freeSpaceStringItem == null) {//GEN-END:|250-getter|0|250-preInit
            // write pre-init user code here
            freeSpaceStringItem = new StringItem("", "");//GEN-LINE:|250-getter|1|250-postInit
            // write post-init user code here
        }//GEN-BEGIN:|250-getter|2|
        return freeSpaceStringItem;
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

    switchDisplayable(null, getFileBrowser());
//GEN-LINE:|275-entry|1|276-postAction
    }//GEN-BEGIN:|275-entry|2|
    //</editor-fold>//GEN-END:|275-entry|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: waypointForm ">//GEN-BEGIN:|279-getter|0|279-preInit
    /**
     * Returns an initiliazed instance of waypointForm component.
     * @return the initialized component instance
     */
    public Form getWaypointForm() {
        if (waypointForm == null) {//GEN-END:|279-getter|0|279-preInit
        // write pre-init user code here
            waypointForm = new Form("Waypoint", new Item[] { getWaypointNameTextField() });//GEN-BEGIN:|279-getter|1|279-postInit
            waypointForm.addCommand(getCancelWaypointCommand());
            waypointForm.addCommand(getSubmitWaypointCommand());
            waypointForm.addCommand(getTakePhotoCommand());
            waypointForm.setCommandListener(this);//GEN-END:|279-getter|1|279-postInit
        // write post-init user code here
        }//GEN-BEGIN:|279-getter|2|
        return waypointForm;
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
            waypointNameTextField = new TextField("Name:", "", 32, TextField.ANY);//GEN-LINE:|280-getter|1|280-postInit
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
            cancelWaypointCommand = new Command("Cancel", Command.BACK, 0);//GEN-LINE:|281-getter|1|281-postInit
        // write post-init user code here
        }//GEN-BEGIN:|281-getter|2|
        return cancelWaypointCommand;
    }
    //</editor-fold>//GEN-END:|281-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: submitWaypointCommand ">//GEN-BEGIN:|283-getter|0|283-preInit
    /**
     * Returns an initiliazed instance of submitWaypointCommand component.
     * @return the initialized component instance
     */
    public Command getSubmitWaypointCommand() {
        if (submitWaypointCommand == null) {//GEN-END:|283-getter|0|283-preInit
        // write pre-init user code here
            submitWaypointCommand = new Command(GPSLoggerLocalization.getMessage("submitWaypointCommand"), Command.OK, 0);//GEN-LINE:|283-getter|1|283-postInit
        // write post-init user code here
        }//GEN-BEGIN:|283-getter|2|
        return submitWaypointCommand;
    }
    //</editor-fold>//GEN-END:|283-getter|2|



    //<editor-fold defaultstate="collapsed" desc=" Generated Method: takePhoto ">//GEN-BEGIN:|292-entry|0|293-preAction
    /**
     * Performs an action assigned to the takePhoto entry-point.
     */
    public void takePhoto() {//GEN-END:|292-entry|0|293-preAction
    ///take a photo here
//GEN-LINE:|292-entry|1|293-postAction
///tmp:
Display.getDisplay(this).vibrate(200);
///
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
            stopCommand = new Command(GPSLoggerLocalization.getMessage("stopCommand"), GPSLoggerLocalization.getMessage("stopCommand"), Command.OK, 0);//GEN-LINE:|295-getter|1|295-postInit
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
        try {
            closeIndividualWaypointLog();

            finishTrackLog();
            // (do not close the whole track log at this time!)

            closeGeoLocator();
        } catch (IOException e) {
            handleException(e, introForm);
        }
//GEN-LINE:|296-entry|1|297-postAction
        switchDisplayable(null, introForm);
    }//GEN-BEGIN:|296-entry|2|
    //</editor-fold>//GEN-END:|296-entry|2|

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

        //exit gracefully, make sure all connections are closed
        try {
            shutDown();
        } catch (IOException e) {
            // too late to handle this now...
        }
        
        mustBeTerminated = true;

        switchDisplayable(null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        
        if (midletPaused) {
            resumeMIDlet ();
        } else {
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
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
    }
    
    public GPSScreen getMainScreen() {
        return mainScreen;
    }

    void loadSettings() {
        if (settings == null) {
            settings = new GPSLoggerSettings(this);
        }
        
        try {
            settings.load();
        } catch (Exception e) {
///???        handleException(e);
/// just ignore?
            e.printStackTrace();
        }
        
        // initialize the settings screen with data from the loaded configuration
        getGpsDeviceTextField().setString(settings.getGPSDeviceURL());
        getLogFolderTextField().setString(settings.getLogFolder());
    }

    void showSettings() {
        
        String connectionURLString = settings.getGPSDeviceURL();
            
        boolean mustConfigure = false;
        
        if (connectionURLString != null) {
            gpsDeviceStringItem.setText(connectionURLString);
        } else { // empty bluetooth URL
            gpsDeviceStringItem.setText("");
        }

        // check again
        if (gpsDeviceStringItem.getText().trim().equals("")) { // empty URL?
            gpsDeviceStringItem.setText("[use Location API (JSR-179)]");
            //commented because will fall back to JSR-179 is BT device was not specified
            //mustConfigure = true;
        }

        final String logPath = settings.getLogFolder();
        
        if (logPath != null) {
            logPathStringItem.setText(logPath);
        } else {
            logPathStringItem.setText("[not configured]");
            mustConfigure = true;
        }
        
        if (mustConfigure) {
            getIntroForm().removeCommand(getStartCommand()); // we cannot start unless we configure the settings
        } else { // everything is already configured
            getIntroForm().addCommand(getStartCommand()); // we cannot start unless we configure the settings
        }
        
        Thread spaceCalculatorThread = new Thread() {
            public void run() {
                FileConnection fileConnection = null;
                long availableSpace = -1L;

                try {
                    fileConnection = (FileConnection)Connector.open(logPath);
                    availableSpace = fileConnection.availableSize();
                } catch (Exception e) {
                    availableSpace = -1L; // :-(
                } finally {
                    if (fileConnection != null) {
                        try {
                            fileConnection.close();
                        } catch (Exception e) {
                            // just ignore?
                        }
                    }
                }
                
                if (availableSpace >= 0) {
                    freeSpaceStringItem.setText("(" + (availableSpace / 1048576L)+ " MiB free)");
                } else { // negative means info was N/A
                    freeSpaceStringItem.setText("(unknown free space info)");
                }
            }
        };

        spaceCalculatorThread.start();
    }

    void startTracking(Displayable screen) {
        
        String connectionURLString = settings.getGPSDeviceURL();
        System.out.println("Starting tracking, connection URL = " + connectionURLString);
        
        boolean mustReconnectToGPS = false;
            
        try {
            do { // (re)connect loop
                screen.setTitle(mustReconnectToGPS?
                          "Restarting..."
                        : "Starting...");
                
                System.out.println(getMainScreen().getTitle());
        
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
/// check JSR-179 availability here!
                        geoLocator = new JSR179GeoLocator(); /// pass Criteria?
                    }

                    mustReconnectToGPS = false; // drop the flag
                }
            
                startTrackLog();

                screen.setTitle(null); // remove the title when started

                resetOdometer();
                
                // show initial screen
                getMainScreen().setSatelliteInfo("Acquiring location, please wait...");
                getMainScreen().forceRepaint();

                // we will start receiving notifications after this call:
                geoLocator.setLocationListener(this);

                do {
                    Thread.sleep(200);
                } while (!mustBeTerminated && !mustReconnectToGPS); // or until user decides to quit?
            
                if (mustReconnectToGPS) {
                    try {
                        geoLocator.close();
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        geoLocator = null;
                    }
                }
                
            } while (mustReconnectToGPS);
            
        } catch (Exception e) {
            handleException(e, introForm);
        } finally {
            try {
                shutDown();
            } catch (Exception e) {
                handleException(e, introForm);
            }
        }
    }
    
    void handleException(Throwable e, Displayable displayable) {

        e.printStackTrace();

        Instant errorInstant = new Instant();
        
        getDisplay().vibrate(3000); // 3 seconds: enough to notice! ;-)
        
        if (errorPlayer != null) {
            try {
                errorPlayer.start();
            } catch (Exception ee) {
                ///ignore?
            }
        }

        if (errorPlayer.getDuration() != Player.TIME_UNKNOWN) {
            try {
                // convert microseconds to milliseconds
                Thread.sleep(errorPlayer.getDuration() / 1000L);
            } catch (InterruptedException ee) {
                ///ignore?
            }
        }

        getErrorAlert().setTitle("Error!");
        getErrorAlert().setString(errorInstant.getDateId()
                                  + " "
                                  + errorInstant.getTimeId()
                                  + ": "
                                  + e.getClass().getName()
                                  + ":\n"
                                  + e.getMessage());
       
        getDisplay().setCurrent(errorAlert, displayable);

        ///waitUntilDisposed(errorAlert);
    }
    
///FIX this, it doesn't work:
    void waitUntilDisposed(Displayable displayable) {
        
        new Thread() {
            public void run() {
                while (getErrorAlert().isShown()) {}
                waitingLock.notifyAll();
            }
        }.start();
        
        synchronized (waitingLock) {
            try {
                waitingLock.wait();
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
    
    void saveCurrentWaypoint(String comments)
                throws IOException {
        
        synchronized (waypointLogLock) {
            if (waypointLogFile == null) { // if no log file is being written, make a connection
                String logFolder = settings.getLogFolder();
                if (!logFolder.endsWith("/")) {
                    logFolder = logFolder + "/"; // make sure we have the trailer here
                }

                Instant now = new Instant(); // reflect the time in the log file name

                String logFilePath = logFolder
                    + "GPSLogger-"
                    + now.getDateId()
                    + "-"
                    + now.getTimeId()
                    + "-waypoints.gpx";
                waypointLogFile = new GPSLogFile(logFilePath);
                waypointLogWriter = new GPXWriter(waypointLogFile.getOutputStream());
                waypointLogWriter.writeHeader("GPSLogger waypoints (" + now.getISO8601UTCDateTimeId() + ")");
                waypointLogWriter.write("\n");
            }

            if (waypointLogWriter != null) {
                GeoLocation waypoint = geoLocator.getLocation();
                waypoint.setName(comments);

                // add this waypoint to our log (it will also be written out at the end of the track)
                if (waypoints == null) {
                    waypoints = new Vector();
                }

                waypoints.addElement(waypoint);

                waypointLogWriter.writeWaypoint(waypoint);
                waypointLogWriter.write("\n");
            }
        }
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
        }
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

                String logFilePath = logFolder
                    + "GPSLogger-"
                    + now.getDateId()
                    + "-"
                    + now.getTimeId()
                    + ".gpx";
                trackLogFile = new GPSLogFile(logFilePath);
            }

            if (trackLogWriter == null) {
                trackLogWriter = new GPXWriter(trackLogFile.getOutputStream());
                trackLogWriter.writeHeader("GPSLogger track log (" + now.getISO8601UTCDateTimeId() + ")");
                trackLogWriter.write("\n");
            }

            // either way, let's create a new track:
            trackLogWriter.writeTrackHeader("GPSLogger track (" + now.getISO8601UTCDateTimeId() + ")");
            trackLogWriter.write("\n");

            trackLogWriter.writeTrackSegmentHeader();
        }
    }

    void finishTrackLog()
            throws IOException {

        synchronized (trackLogLock) {
            if (trackLogWriter != null) {
                trackLogWriter.writeTrackSegmentFooter();
                trackLogWriter.write("\n");

                trackLogWriter.writeTrackFooter();
                trackLogWriter.write("\n");

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
        }
    }
    
    void closeTrackLogFile()
            throws IOException {

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
        }
    }

    void closeGeoLocator()
            throws IOException {
        if (geoLocator != null) {
            geoLocator.close();
            geoLocator = null;
        }
    }

    synchronized void shutDown()
            throws IOException {

        closeIndividualWaypointLog();
        closeTrackLogFile();
        closeGeoLocator();
    }

    public void locationChanged(GeoLocation location) {

        if (location == null) {
            System.out.println("Null location received!");
/// update screen: alert user of wrong data somehow
///... show data state attribute (validity?) on the screen
getMainScreen().setSatelliteInfo("(Invalid location data)");
getMainScreen().repaint();
///
            return;
        }

        System.out.println(location);

        synchronized (trackLogLock) {
            if (trackLogWriter != null) {
                try {
                    trackLogWriter.writeTrackpoint(location);
                } catch (IOException e) {
                    handleException(e, getIntroForm());
                }
            }
        }

        getMainScreen().setLocation(location);
        long totalTimeMillis = System.currentTimeMillis() - startTimeMillis;
        Instant totalTime = new Instant(totalTimeMillis);
        getMainScreen().setTotalTime(totalTime.getISO8601UTCTimeId());

        getMainScreen().repaint();
    }

    public void handleLocatorException(Exception e) {
///???
        handleException(e, getIntroForm());
    }
}
