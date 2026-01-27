package com.mad.shared.utils;

import java.util.Random;

public class RandomGenerator extends Random {
    private Random randomGen;

    public RandomGenerator() {
        this.randomGen = new Random();
    }


    public float getFloatInRange(float min, float max) {
        return min + (max - min) * randomGen.nextFloat();
    }

    public double getDoubleInRange(double min, double max) {
        return min + (max - min) * randomGen.nextDouble();
    }

    public int getIntInRange(int min, int max) {
        if (max == Integer.MAX_VALUE) {
            max = max - 1;
        }
        return randomGen.nextInt((max - min) + 1) + min;

    }
}
