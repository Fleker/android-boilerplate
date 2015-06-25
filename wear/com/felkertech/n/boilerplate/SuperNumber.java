package com.felkertech.n.virtualpets.Objects;

/**
 * Created by N on 6/21/2015.
 */
public class SuperNumber {
    private double val;
    public SuperNumber(double input) {
        val = input;
    }
    public void setVal(double newInput) {
        val = newInput;
    }
    public double getVal() {
        return val;
    }
    public double setInRange(double min, double input, double max) {
        if(input < min)
            val = min;
        else if(input > max)
            val = max;
        else
            val = input;
        return val;
    }
    /* Now we have some static functions that add on to the math class */
    public static double setWithinRange(double min, double input, double max) {
        if(input < min)
            return min;
        else if(input > max)
            return max;
        else
            return input;
    }
}
