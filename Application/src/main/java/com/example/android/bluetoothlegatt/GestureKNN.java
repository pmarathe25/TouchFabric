package com.example.android.bluetoothlegatt;

/**
 * Created by pranav on 11/9/17.
 */

public class GestureKNN {
    private static int ROW_LENGTH = 4;
    private static int MAX_GESTURE_LENGTH = 15;
    private static double[][] GESTURE_LIBRARY = {
        { 1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000 },
        { -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000 },
        { 0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000,  0.000000,  -1.000000 },
        { 0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000,  0.000000,  1.000000 },
    };
    private static String[] GESTURE_NAMES = { "LRSwipe", "RLSwipe", "UpSwipe", "DownSwipe" };

    public GestureKNN() {

    }

    public static String recognize(String input) {
        double[] gestureDeltas = createGestureDeltas(input);
        double[] interpolatedDeltas = interpolateGestureDeltas(gestureDeltas);
        double[] MSE = new double[GESTURE_LIBRARY.length];
        for (int i = 0; i < GESTURE_LIBRARY.length; ++i) {
            // Loop over the gesture deltas and compare
            double currentMSE = 0;
            for (int j = 0; j < gestureDeltas.length; ++j) {
                currentMSE += Math.pow((gestureDeltas[j] - GESTURE_LIBRARY[i][j]), 2);
            }
            MSE[i] = currentMSE;
        }
        return getMinGesture(MSE);
    }

    private static String getMinGesture(double[] MSE) {
        double min = 1024 * 1024;
        int minIndex = 0;
        for (int i = 0; i < MSE.length; ++i) {
            if (MSE[i] < min) {
                min = MSE[i];
                minIndex = i;
            }
        }
        return GESTURE_NAMES[minIndex];
    }

    private static double[] createGestureDeltas(String input) {
        int x, y, nextX, nextY;
        double[] gestureDeltas = new double[input.length() * 2];
        nextX = (input.charAt(0) - 97) % ROW_LENGTH;
        nextY = (input.charAt(0) - 97) / ROW_LENGTH;
        for (int i = 0; i < input.length() - 1; ++i) {
            x = nextX;
            y = nextY;
            nextX = (input.charAt(i) - 97) % ROW_LENGTH;
            nextY = (input.charAt(i + 1) - 97) / ROW_LENGTH;
            gestureDeltas[i * 2] = nextX - x;
            gestureDeltas[i * 2 + 1] = nextY - y;
        }
        return gestureDeltas;
    }

    private static double[] interpolateGestureDeltas(double[] gestureDeltas) {
        double[] interpolatedDeltas = new double[MAX_GESTURE_LENGTH * 2];
        double stepSize = (gestureDeltas.length - 1) / MAX_GESTURE_LENGTH;
        double currentStep = 0.0;
        for (int i = 0; i < MAX_GESTURE_LENGTH * 2; i += 2) {
            // Points to interpolate
            double leftValX = gestureDeltas[(int) Math.floor(currentStep) * 2];
            double leftValY = gestureDeltas[(int) Math.floor(currentStep) * 2 + 1];
            double rightValX = gestureDeltas[(int) Math.ceil(currentStep) * 2];
            double rightValY = gestureDeltas[(int) Math.ceil(currentStep) * 2 + 1];
            // Compute weights
            double leftWeight = (Math.floor(currentStep) + 1) - currentStep;
            double rightWeight = 1 - leftWeight;
            // Insert the interpolated delta
            interpolatedDeltas[i] = leftValX * leftWeight + rightValX + rightWeight;
            interpolatedDeltas[i + 1] = leftValY * leftWeight + rightValY + rightWeight;
            // Next!
            currentStep += stepSize;
        }
        return interpolatedDeltas;
    }

}
