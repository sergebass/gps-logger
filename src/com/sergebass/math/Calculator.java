/*
 * (C) Serge Perinsky, 2009
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

            if (values[i] < min)
                min = values[i];
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

            if (Math.abs(values[i]) < min)
                min = values[i]; // let the sign remain...
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

            if (values[i] > max)
                max = values[i];
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

            if (Math.abs(values[i]) > max)
                max = values[i]; // let the sign remain...
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
        for (int i = from; i < to; i++)
            sum += values[i];

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
        for (int i = from; i < to; i++)
            sum += Math.abs(values[i]);

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
        for (int i = from; i < to; i++)
            total += values[i];

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
        for (int i = from; i < to; i++)
            total += Math.abs(values[i]);

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
}
