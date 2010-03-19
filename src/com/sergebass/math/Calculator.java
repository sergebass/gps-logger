/*
 * (C) Serge Perinsky, 2009-2010
 * (C) Richard Carless, 2006 (trigonometric portions: asin, acos, atan)
 */

package com.sergebass.math;

/**
 * The Calculator is a multipurpose class for performing miscellaneous
 * mathematical calculations.
 *
 * @see java.lang.Math
 *
 * @author Serge Perinsky
 */
public class Calculator {

    /**
     * Returns a minimum value of an array.
     *
     * @param values the source values array
     * @return the minimum value of the source array values
     */
    public static double min(double values[]) {

        return min(values, 0, values.length);
    }

    /**
     * Returns a minimum value of an array part.
     *
     * @param  values  the source values array
     * @param  from    the starting index
     * @param  to      the ending index (excluding)
     * @return  the minimum value of the source array values
     */
    public static double min(double values[], int from, int to) {

        double min = Double.MAX_VALUE;
        for (int i = from; i < to; i++) {

            if (values[i] < min) {
                min = values[i];
            }
        }

        return min;
    }

    /**
     * Returns an absolute minimum value of an array.
     *
     * @param values the source values array
     * @return the non-negative minimum value of the source array values
     */
    public static double minAbs(double values[]) {

        return minAbs(values, 0, values.length);
    }

    /**
     * Returns an absolute minimum value of an array part.
     *
     * @param  values  the source values array
     * @param  from    the starting index
     * @param  to      the ending index (excluding)
     * @return the non-negative minimum value of the source array values
     */
    public static double minAbs(double values[], int from, int to) {

        double min = Double.MAX_VALUE;
        for (int i = from; i < to; i++) {

            if (Math.abs(values[i]) < min) {
                min = values[i];
            } // let the sign remain...
        }

        return min;
    }

    /**
     * Returns a maximum value of an array.
     *
     * @param values the source values array
     * @return the maximum value of the source array values
     */
    public static double max(double values[]) {

        return max(values, 0, values.length);
    }

    /**
     * Returns a maximum value of an array part.
     *
     * @param  values  the source values array
     * @param  from    the starting index
     * @param  to      the ending index (excluding)
     * @return the maximum value of the source array values
     */
    public static double max(double values[], int from, int to) {

        double max = Double.MIN_VALUE;
        for (int i = from; i < to; i++) {

            if (values[i] > max) {
                max = values[i];
            }
        }

        return max;
    }

    /**
     * Returns an absolute maximum value of an array.
     *
     * @param  values  the source values array
     * @return  the non-negative maximum value of the source array values
     */
    public static double maxAbs(double values[]) {

        return maxAbs(values, 0, values.length);
    }

    /**
     * Returns an absolute maximum value of an array part.
     *
     * @param  values  the source values array
     * @param  from    the starting index
     * @param  to      the ending index (excluding)
     * @return the non-negative maximum value of the source array values
     */
    public static double maxAbs(double values[], int from, int to) {

        double max = Double.MIN_VALUE;
        for (int i = from; i < to; i++) {

            if (Math.abs(values[i]) > max) {
                max = values[i];
            } // let the sign remain...
        }

        return max;
    }

    /**
     * Returns an average arithmetical value of an array.
     *
     * @param  values  the source values array
     * @return the average value of the source array values
     */
    public static double avg(double values[]) {

        return avg(values, 0, values.length);
    }

    /**
     * Returns an average arithmetical value of an array part.
     *
     * @param  values  the source values array
     * @param  from    the starting index
     * @param  to      the ending index (excluding)
     * @return the average value of the source array values
     */
    public static double avg(double values[], int from, int to) {

        double sum = 0;
        for (int i = from; i < to; i++) {
            sum += values[i];
        }

        return sum / (to - from + 1); //fixed by shaman
//      return sum / values.length;   //this is wrong, when from <> 0 and to <>values.length - 1
    }

    /**
     * Returns an absolute average arithmetical value of an array.
     *
     * @param  values  the source values array
     * @return the non-negative average value of the source array values
     */
    public static double avgAbs(double values[]) {

        return avgAbs(values, 0, values.length);
    }

    /**
     * Returns an absolute average arithmetical value of an array part.
     *
     * @param  values  the source values array
     * @param  from    the starting index
     * @param  to      the ending index (excluding)
     * @return the non-negative average value of the source array values
     */
    public static double avgAbs(double values[], int from, int to) {

        double sum = 0;
        for (int i = from; i < to; i++) {
            sum += Math.abs(values[i]);
        }

        return sum / values.length;
    }

    /**
     * Returns a sum value of an array values.
     *
     * @param  values  the source values array
     * @return the total value of the source array values
     */
    public static double sum(double values[]) {

        return sum(values, 0, values.length);
    }

    /**
     * Returns a sum value of an array part values.
     *
     * @param  values  the source values array
     * @param  from    the starting index
     * @param  to      the ending index (excluding)
     * @return the total value of the source array values
     */
    public static double sum(double values[], int from, int to) {

        double total = 0;
        for (int i = from; i < to; i++) {
            total += values[i];
        }

        return total;
    }

    /**
     * Returns a sum of absolute array values.
     *
     * @param  values  the source values array
     * @return the total value of the source array absolute values
     */
    public static double sumAbs(double values[]) {

        return sumAbs(values, 0, values.length);
    }

    /**
     * Returns a sum of absolute array part values.
     *
     * @param  values  the source values array
     * @param  from    the starting index
     * @param  to      the ending index (excluding)
     * @return the total value of the absolute source array values
     */
    public static double sumAbs(double values[], int from, int to) {

        double total = 0;
        for (int i = from; i < to; i++) {
            total += Math.abs(values[i]);
        }

        return total;
    }

    public static double convertDegreesToRadians(double degrees) {

        return degrees * Math.PI / 180;
    }

    public static double convertRadiansToDegrees(double radians) {

        return radians * 180 / Math.PI;
    }

    public static String inflect(int number, String s1, String s2, String s5) {

        switch (number % 10) {

            case 1:
                return s1;
            case 2:
            case 3:
            case 4:
                return s2;
            default:
                return s5;
        }
    }

    // (C) Richard Carless, 2006 (trigonometric portions: asin, acos, atan):
    
    // constants
    static final double sq2p1 = 2.414213562373095048802e0;
    static final double sq2m1  = .414213562373095048802e0;
    static final double p4  = .161536412982230228262e2;
    static final double p3  = .26842548195503973794141e3;
    static final double p2  = .11530293515404850115428136e4;
    static final double p1  = .178040631643319697105464587e4;
    static final double p0  = .89678597403663861959987488e3;
    static final double q4  = .5895697050844462222791e2;
    static final double q3  = .536265374031215315104235e3;
    static final double q2  = .16667838148816337184521798e4;
    static final double q1  = .207933497444540981287275926e4;
    static final double q0  = .89678597403663861962481162e3;
    static final double PIO2 = 1.5707963267948966135E0;
    
    // reduce
    private static double mxatan(double arg)
    {
        double argsq, value;

        argsq = arg*arg;
        value = ((((p4*argsq + p3)*argsq + p2)*argsq + p1)*argsq + p0);
        value = value/(((((argsq + q4)*argsq + q3)*argsq + q2)*argsq + q1)*argsq + q0);
        return value*arg;
    }

    // reduce
    private static double msatan(double arg)
    {
        if(arg < sq2m1) {
            return mxatan(arg);
        }
        if(arg > sq2p1) {
            return PIO2 - mxatan(1 / arg);
        }
        return PIO2/2 + mxatan((arg-1)/(arg+1));
    }

    // implementation of atan
    public static double atan(double arg)
    {
        if(arg > 0) {
            return msatan(arg);
        }
        return -msatan(-arg);
    }

    // implementation of atan2
    public static double atan2(double arg1, double arg2)
    {
        if(arg1+arg2 == arg1)
        {
            if(arg1 >= 0) {
                return PIO2;
            }
            return -PIO2;
        }
        arg1 = atan(arg1/arg2);
        if(arg2 < 0)
        {
            if(arg1 <= 0) {
                return arg1 + Math.PI;
            }
            return arg1 - Math.PI;
        }
        return arg1;

    }

    // implementation of asin
    public static double asin(double arg)
    {
        double temp;
        int sign;

        sign = 0;
        if(arg < 0)
        {
            arg = -arg;
            sign++;
        }
        if(arg > 1) {
            return Double.NaN;
        }
        temp = Math.sqrt(1 - arg*arg);
        if(arg > 0.7) {
            temp = PIO2 - atan(temp / arg);
        }
        else {
            temp = atan(arg / temp);
        }
        if(sign > 0) {
            temp = -temp;
        }
        return temp;
    }

    // implementation of acos
    public static double acos(double arg)
    {
        if(arg > 1 || arg < -1) {
            return Double.NaN;
        }
        return PIO2 - asin(arg);
    }
}
