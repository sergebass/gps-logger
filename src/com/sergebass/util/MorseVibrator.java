/*
 * (C) Serge Perinsky, 2009
s */

package com.sergebass.util;

import javax.microedition.lcdui.Display;

/**
 * International Morse vibrator.
 *
 * @author Serge Perinsky
 */
public class MorseVibrator
        extends Vibrator {

    final static int DEFAULT_UNIT_DURATION_MILLISECONDS = 100; // ms

    // Morse digits (0-9)
    final static MorseCharacter digitCharacters[] = {
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                           }), // 1 (.----)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                           }), // 2 (..---)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                           }), // 3 (...--)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                           }), // 4 (....-)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                           }), // 5 (.....)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                           }), // 6 (-....)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                           }), // 7 (--...)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                           }), // 8 (---..)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                           }), // 9 (----.)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                           }) // 0 (-----)
    };

    // Morse latin characters (A-Z)
    final static MorseCharacter latinCharacters[] = {
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DASH
                           }), // A (.-)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT
                           }), // B (-...)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT
                           }), // C (-.-.)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT
                           }), // D (-..)
        new MorseCharacter(new int[] { MorseCharacter.DOT
                           }), // E (.)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT
                           }), // F (..-.)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT
                           }), // G (--.)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT
                           }), // H (....)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT
                           }), // I (..)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH
                           }), // J (.---)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH
                           }), // K (-.-)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT
                           }), // L (.-..)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH
                           }), // M (--)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DOT
                           }), // N (-.)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH
                           }), // O (---)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT
                           }), // P (.--.)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH
                           }), // Q (--.-)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT
                           }), // R (.-.)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT
                           }), // S (...)
        new MorseCharacter(new int[] { MorseCharacter.DASH
                           }), // T (-)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH
                           }), // U (..-)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH
                           }), // V (...-)
        new MorseCharacter(new int[] { MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH
                           }), // W (.--)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH
                           }), // X (-..-)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DASH
                           }), // Y (-.--)
        new MorseCharacter(new int[] { MorseCharacter.DASH,
                                       MorseCharacter.DASH,
                                       MorseCharacter.DOT,
                                       MorseCharacter.DOT
                           }) // Z (--..)
    };

    int unitDurationMillis = DEFAULT_UNIT_DURATION_MILLISECONDS;

    public MorseVibrator(Display display) {
        this(display, DEFAULT_UNIT_DURATION_MILLISECONDS);
    }

    public MorseVibrator(Display display, int unitDurationMillis) {
        super(display);
        this.unitDurationMillis = unitDurationMillis;
    }

    /**
     * blocking.
     */
    public void vibrateMorseCode(String text) {
        for (int i = 0; i < text.length(); i++) {
            vibrateMorseCode(text.charAt(i));
        }
    }

    /**
     * blocking.
     */
    public void vibrateMorseCode(char c) {
        c = Character.toUpperCase(c);
        if (Character.isDigit(c)) {
            vibrateMorseCode(digitCharacters[Character.digit(c, 10)]);
        } else if (c == ' ') { // space character?
            try {
                // 3 other units (for the total space of 7) are included in vibrateMorseCode()
                Thread.sleep(unitDurationMillis * 4);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else if (c >= 'A' && c <= 'Z') { // latin characters?
            vibrateMorseCode(latinCharacters[c-'A']);
        } else {
            // just ignore unknown characters
        }
    }

    /**
     * blocking.
     */
    public void vibrateMorseCode(MorseCharacter mc) {
        int[] elements = mc.getElements();
        for (int i = 0; i < elements.length; i++) {
            vibrateMorseElement(elements[i], (i < elements.length - 1?
                                              unitDurationMillis
                                            : unitDurationMillis * 3));
        }
    }

    /**
     * blocking.
     */
    public void vibrateMorseElement(int element, int pauseAfterMillis) {

        int pulseDuration = 0;
        
        if (element == MorseCharacter.DOT) {
            pulseDuration = unitDurationMillis; // dot = 1 unit long
        } else if (element == MorseCharacter.DASH) {
            pulseDuration = unitDurationMillis * 3; // dash = 3 units long
        }

        // this call is asynchronous, hence wait until it is over
        vibrate(pulseDuration);
        try {
            Thread.sleep(pulseDuration + pauseAfterMillis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
