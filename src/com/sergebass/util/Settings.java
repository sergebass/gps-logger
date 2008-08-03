/*
 * Settings.java (C) Copyright 2008 by Serge Perinsky
 */

package com.sergebass.util;

import java.util.Hashtable;
import java.util.Enumeration;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.*;

/**
 * Settings.
 *
 * @author Serge Perinsky
 */
public class Settings
        extends Hashtable {

    MIDlet midlet;
    String recordStoreName;
    
    public Settings(MIDlet midlet, String recordStoreName) {
        
        this.midlet = midlet;
        this.recordStoreName = recordStoreName;
    }

    public String getRecordStoreName() {
        
        return recordStoreName;
    }
    
    public static Settings load(MIDlet midlet, String recordStoreName)
            throws RecordStoreException {
        
        Settings settings = new Settings(midlet, recordStoreName);
        settings.load();
        
        return settings;
    }
    
    public void load() 
            throws RecordStoreException {
        
        load(recordStoreName);
    }
        
    public void load(String recordStoreName) 
            throws RecordStoreException {
        
        RecordStore recordStore = null;
        try {
            recordStore = RecordStore.openRecordStore(recordStoreName, true); // create if necessary
            int recordCount = recordStore.getNumRecords();

            for (int i = 1; i <= recordCount; i += 2) {

                // first, the key,
                byte[] bytes = recordStore.getRecord(i);
                
                String key = (bytes != null)?
                        new String(bytes)
                        : null;

                // ... and the value
                bytes = recordStore.getRecord(i + 1);
                
                String value = (bytes != null)?
                        new String(bytes)
                        : null;

                if (key != null && value != null) { // neither key nor value key must never be null
                    put(key, value);
                }
            }
            
        } catch (RecordStoreException e) {
            
            throw e;
            
        } finally {
            
            if (recordStore != null) {
                recordStore.closeRecordStore();
            }
        }
    }
    
    public void save()
            throws RecordStoreException {
        
        save(recordStoreName);
    }
    
    public void save(String recordStoreName)
            throws RecordStoreException {
                
        RecordStore.deleteRecordStore(recordStoreName); // delete the old store

        RecordStore recordStore = null;
        try {
            recordStore = RecordStore.openRecordStore(recordStoreName, true); // create new one

            Enumeration enumeration = keys();
            while (enumeration.hasMoreElements()) {
                Object key = enumeration.nextElement();
                Object value = get(key);

                // write the key first,
                byte[] bytes = key.toString().getBytes();
                recordStore.addRecord(bytes, 0, bytes.length);

                // and the value
                if (value != null) {
                    bytes = value.toString().getBytes();
                    recordStore.addRecord(bytes, 0, bytes.length);
                } else { // value is null
                    // replace null values with empty strings
                    bytes = new String("").getBytes();
                    recordStore.addRecord(bytes, 0, bytes.length);
                }
            }
            
        } catch (RecordStoreException e) {
            
            throw e;
            
        } finally {
            
            if (recordStore != null) {
                recordStore.closeRecordStore();
            }
        }
    }
}
