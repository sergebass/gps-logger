/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.util;

/**
 *
 * @author Serge Perinsky
 */
public interface SettingsListener {
    public void settingsChanged(Settings settings, Object key, Object newValue);
}
