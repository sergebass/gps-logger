/*
 * (C) Serge Perinsky, 2009
 */

package com.sergebass.util;

/**
 *
 * @author Serge Perinsky
 */
public class MorseCharacter {

    public final static int DOT = 0;
    public final static int DASH = 1;

    int[] elements;

    public MorseCharacter(int[] codes) {
        this.elements = codes;
    }

    public int[] getElements() {
        return elements;
    }
}
