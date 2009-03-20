/*
 * (C) Serge Perinsky, 2007, 2008
 */

package com.sergebass.bluetooth;

import java.util.Vector;
import javax.bluetooth.*;

/**
 * BluetoothManager.
 *
 * @author Serge Perinsky
 */
public class BluetoothManager
        implements DiscoveryListener {

    DiscoveryAgent agent = null;
    
    Vector /*<RemoteDevice>*/ devices;
    Vector /*<ServiceRecord>*/ services;
    
    final Object lock = new Object();

    public static boolean isBluetoothAPISupported() {
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
    
    public BluetoothManager() {
    }
    
    public Vector /*<RemoteDevice>*/ searchDevices()
                throws BluetoothStateException {

        System.out.println("Searching for Bluetooth devices...");
                
        devices = new Vector(); /*<RemoteDevice>*/
        agent = null; // serves also as a flag
        
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        agent = localDevice.getDiscoveryAgent();

        if (agent == null) {
            return devices;
        }
        
        if (!agent.startInquiry(DiscoveryAgent.GIAC, this)) {
            return devices;
        }

        // wait until all of the devices are discovered...

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Returning discovered Bluetooth devices.");
        return devices;
    }

    public boolean cancelDeviceSearch() {
        return agent != null? agent.cancelInquiry(this) : false;
    }
    
    public void deviceDiscovered(RemoteDevice device,
                                 DeviceClass deviceClass) {
        
        System.out.println("Bluetooth device discovered: device="
                + device
                + ", class="
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
                                                     UUID[] uuidServiceSet)
            throws BluetoothStateException {
        
        System.out.println("Searching for Bluetooth services...");
                
        services = new Vector(); /*<ServiceRecord>*/
        
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        agent = localDevice.getDiscoveryAgent();

        if (agent == null) {
            return services;
        }
        
        try {
            agent.searchServices(null, uuidServiceSet, device, this);
            synchronized (lock) {
                lock.wait(); // wait until all of the services are found...
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Ok, the services must have been discovered by now
        System.out.println("Returning discovered Bluetooth services.");
        return services;
    }
    
    public boolean cancelServiceSearch(int transID) {
        return agent != null? agent.cancelServiceSearch(transID) : false;
    }
        
    public void servicesDiscovered(int transID,
                                   ServiceRecord[] serviceRecords) {
        
        if (serviceRecords != null) {
            System.out.println("Bluetooth services discovered: " + serviceRecords.length + " item(s)");
            for (int i = 0; i < serviceRecords.length; i++) {
                services.addElement(serviceRecords[i]);
            }
        }
    }
    
    public void serviceSearchCompleted(int transID,
                                       int responseCode) {
        
        System.out.println("Bluetooth service search completed: transID="
                + transID
                + ", responseCode="
                + responseCode
                );
        
        synchronized(lock){
            lock.notifyAll();
        }
    }
}
