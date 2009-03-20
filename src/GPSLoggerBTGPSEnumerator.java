/*
 * (C) Serge Perinsky, 2009
 */

import java.util.Vector;
import java.io.IOException;

import javax.bluetooth.*;
import javax.microedition.lcdui.List;

import com.sergebass.bluetooth.BluetoothManager;

/**
 *
 * @author Serge Perinsky
 */
public class GPSLoggerBTGPSEnumerator
        extends Thread {

    final static UUID SERIAL_PORT_PROFILE_UUID = new UUID(0x1101); // SPP service (Serial Port Profile)

    GPSLogger logger;
    List list;

    BluetoothManager deviceManager = null;
    Vector deviceServiceRecords = null;

    boolean isRunning = false;

    public GPSLoggerBTGPSEnumerator(GPSLogger logger, List list) {
        this.logger = logger;
        this.list = list;

        deviceManager = new BluetoothManager();
    }

    public void run() {
        isRunning = true;
        System.out.println("Searching Bluetooth GPS devices...");
        
        list.deleteAll();
        list.setTitle("Searching GPS...");

        deviceServiceRecords = new Vector(); /* <ServiceRecord> */

        Vector devices = null;
        try {
            devices = deviceManager.searchDevices();
        } catch (BluetoothStateException ex) {
            logger.handleException(ex, logger.getSettingsForm());
            return;
        }

        if (!isRunning) { // abort?
            return;
        }

        UUID[] uuidServiceSet = new UUID[1];
        uuidServiceSet[0] = SERIAL_PORT_PROFILE_UUID;

        if (devices.size() == 0) {
            list.append("[No GPS devices found]", null);
        } else {
            for (int i = 0; i < devices.size(); i++) {

                RemoteDevice device = (RemoteDevice)devices.elementAt(i);
                System.out.println(device.toString());

                // now let's see if this device supports the needed services...
                Vector services = null;
                try {
                    services = deviceManager.searchServices(device, uuidServiceSet);
                } catch (BluetoothStateException ex) {
                    logger.handleException(ex, logger.getSettingsForm());
                    return;
                }

                if (!isRunning) { // abort?
                    return;
                }

                for (int iService = 0; iService < services.size(); iService++) {

                    ServiceRecord service = (ServiceRecord)services.elementAt(iService);
                    deviceServiceRecords.addElement(service);

                    try { // add matching entry to the selection list
                        list.append(device.getFriendlyName(false) // do not ask
                                             + " ("
                                             + service.getHostDevice().getBluetoothAddress()
                                             + ")",
                                           null); // no image
                    } catch (IOException ex) {
                        list.append("!!"
                                    + ex.getMessage()
                                    + " (" + ex.getClass().getName()
                                    + ")",
                                    null); /// or error icon?
                        logger.getDisplay().vibrate(1000);
                    }
                }
            }
        }

        list.setTitle("Choose GPS:");

        System.out.println("Bluetooth GPS device scan complete.");
    }

    public void cancel() {

        if (!isRunning) { // already done?
            return;
        }

        isRunning = false;

        if (deviceManager != null) {
            deviceManager.cancelDeviceSearch();
        }

        System.out.println("Bluetooth GPS device scan was cancelled.");
    }

    public Vector getDeviceServiceRecords() {
        return deviceServiceRecords;
    }
}
