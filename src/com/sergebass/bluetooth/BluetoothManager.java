package com.sergebass.bluetooth;



/*
 * (C) Serge Perinsky, 2007, 2008
 */

import java.util.Vector;
import javax.bluetooth.*;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * BluetoothManager.
 *
 * @author Serge Perinsky
 */
public class BluetoothManager
        implements DiscoveryListener {

    MIDlet midlet;
    
    // cached values
    
    DiscoveryAgent agent = null;
    
    Vector /*<RemoteDevice>*/ devices;
    Vector /*<ServiceRecord>*/ services;
    
    Object lock = new Object();

    public static boolean isJSR82Supported() {
        String	bluetoothVersion = null;
        boolean	isJSR82SupportedHere  = true;

        try {
            Class.forName("javax.bluetooth.LocalDevice"); // does the class exist?
            bluetoothVersion = javax.bluetooth.LocalDevice.getProperty("bluetooth.api.version");
        } catch(ClassNotFoundException e) { // class does not exist -> no Bluetooth
            isJSR82SupportedHere  = false;
        }
        
        return isJSR82SupportedHere;
    }
    
    public BluetoothManager(MIDlet midlet) {
        this.midlet = midlet;
    }
    
    public Vector /*<RemoteDevice>*/ searchDevices() {

        System.out.println("Searching for devices...");
                
        devices = new Vector(); /*<RemoteDevice>*/
        
        LocalDevice localDevice = null;
        agent = null;
        
        try {
            localDevice = LocalDevice.getLocalDevice();
            agent = localDevice.getDiscoveryAgent();
        } catch (BluetoothStateException e) {
///handle this
            e.printStackTrace();
        }

        if (agent == null) {
            return devices;
        }
        
        try {
            if (!agent.startInquiry(DiscoveryAgent.GIAC, this)) {
                return devices;
            }
        } catch (BluetoothStateException ex) {
///handle this
            ex.printStackTrace();
            return devices;
        }

        // wait until all of the devices are discovered...

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
///handle this?            
            e.printStackTrace();
        }

        System.out.println("Returning the found devices.");
        return devices;
    }

    public boolean cancelDeviceSearch() {
        
        return agent.cancelInquiry(this);
    }
    
    public void deviceDiscovered(RemoteDevice device,
                                 DeviceClass deviceClass) {
        
        System.out.println("deviceDiscovered: device="
                + device
                + ", deviceClass="
                + deviceClass);
        
        devices.addElement(device);
    }
    
    public void inquiryCompleted(int discoveryType) {
        
        System.out.println("inquiryCompleted: discoveryType=" + discoveryType);
        
        switch (discoveryType) {
            case DiscoveryListener.INQUIRY_COMPLETED :
                System.out.println("INQUIRY_COMPLETED");
                break;
            case DiscoveryListener.INQUIRY_TERMINATED :
                System.out.println("INQUIRY_TERMINATED");
                break;
            case DiscoveryListener.INQUIRY_ERROR :
                Display.getDisplay(midlet).vibrate(1000);
                System.out.println("INQUIRY_ERROR");
                break;
            default :
                System.out.println("Unknown Response Code");
                break;
        }
        
        synchronized(lock){
            lock.notifyAll();
        }
    }
    
    public Vector /*<ServiceRecord>*/ searchServices(RemoteDevice device,
                                                     UUID[] uuidServiceSet) {
        
        System.out.println("Searching for services...");
                
        services = new Vector(); /*<ServiceRecord>*/
        
        LocalDevice localDevice = null;
        agent = null;
        
        try {
            localDevice = LocalDevice.getLocalDevice();
            agent = localDevice.getDiscoveryAgent();
        } catch (BluetoothStateException e) {
///handle this
            e.printStackTrace();
            Display.getDisplay(midlet).vibrate(1000);
        }

        if (agent == null) {
            return services;
        }
        
        try {
            agent.searchServices(null, uuidServiceSet, device, this);
            synchronized (lock) {
                lock.wait(); // wait until all of the services are found...
            }
        } catch (InterruptedException e) {
///handle this?
            e.printStackTrace();
            Display.getDisplay(midlet).vibrate(1000);
        } catch (BluetoothStateException ex) {
///handle this
            ex.printStackTrace();
            Display.getDisplay(midlet).vibrate(1000);
    }
        
        // Ok, the services must have been discovered by now
        return services;
    }
    
    public boolean cancelServiceSearch(int transID) {
        
        return agent.cancelServiceSearch(transID);
    }
        
    public void servicesDiscovered(int transID,
                                   ServiceRecord[] serviceRecords) {
        
        if (serviceRecords != null) {
            System.out.println("servicesDiscovered: " + serviceRecords.length + " items");
            for (int i = 0; i < serviceRecords.length; i++) {
                services.addElement(serviceRecords[i]);
            }
        }
    }
    
    public void serviceSearchCompleted(int transID,
                                       int responseCode) {
        
        System.out.println("serviceSearchCompleted: transID="
                + transID
                + ", responseCode="
                + responseCode
                );
        
        synchronized(lock){
            lock.notifyAll();
        }
    }
}
