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

    public static class GestureDelta {
        public GestureDelta() {
            X = 0.0;
            Y = 0.0;
        }

        public GestureDelta(double X, double Y) {
            this.X = X;
            this.Y = Y;
        }

        public String toString() {
            return "(" + X + ", " + Y + ")";
        }

        public double X, Y;
    }

    public GestureKNN() {

    }

    public static String recognize(String input) {
        GestureDelta[] gestureDeltas = createGestureDeltas(input);
        double[] interpolatedDeltas = interpolateGestureDeltas(gestureDeltas);
        double[] MSE = new double[GESTURE_LIBRARY.length];
        for (int i = 0; i < GESTURE_LIBRARY.length; ++i) {
            // Loop over the gesture deltas and compare
            double currentMSE = 0;
            for (int j = 0; j < interpolatedDeltas.length; ++j) {
                currentMSE += Math.pow((interpolatedDeltas[j] - GESTURE_LIBRARY[i][j]), 2);
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

    private static GestureDelta[] createGestureDeltas(String input) {
        int x, y, nextX, nextY;
        GestureDelta[] gestureDeltas = new GestureDelta[input.length() - 1];
        nextX = (input.charAt(0) - 97) % ROW_LENGTH;
        nextY = (input.charAt(0) - 97) / ROW_LENGTH;
        for (int i = 0; i < input.length() - 1; ++i) {
            x = nextX;
            y = nextY;
            nextX = (input.charAt(i + 1) - 97) % ROW_LENGTH;
            nextY = (input.charAt(i + 1) - 97) / ROW_LENGTH;
            gestureDeltas[i] = new GestureDelta(nextX - x, nextY - y);
        }
        return gestureDeltas;
    }

    private static double[] interpolateGestureDeltas(GestureDelta[] gestureDeltas) {
        double[] interpolatedDeltas = new double[MAX_GESTURE_LENGTH * 2];
        double stepSize = (gestureDeltas.length - 1) / (double) (MAX_GESTURE_LENGTH);
        double currentStep = 0.0;
        for (int i = 0; i < MAX_GESTURE_LENGTH; ++i) {
            // Points to interpolate
            double leftValX = gestureDeltas[(int) Math.floor(currentStep)].X;
            double leftValY = gestureDeltas[(int) Math.floor(currentStep)].Y;
            // Right-side
            double rightValX = gestureDeltas[(int) Math.ceil(currentStep)].X;
            double rightValY = gestureDeltas[(int) Math.ceil(currentStep)].Y;
            // Compute weights
            double leftWeight = (Math.floor(currentStep) + 1) - currentStep;
            double rightWeight = 1 - leftWeight;
            // Insert the interpolated delta
            interpolatedDeltas[i * 2] = leftValX * leftWeight + rightValX * rightWeight;
            interpolatedDeltas[i * 2 + 1] = leftValY * leftWeight + rightValY * rightWeight;
            // Next!
            currentStep += stepSize;
            // DEBUG
            // System.out.println("Interpolating between " + gestureDeltas[(int) Math.floor(currentStep)] + " and " + gestureDeltas[(int) Math.ceil(currentStep)]);
            // System.out.println("Using Weights left: " + leftWeight + " and right: " + rightWeight);
            // System.out.println("Created Delta: " + new GestureDelta(interpolatedDeltas[i * 2], interpolatedDeltas[i * 2 + 1]));
        }
        // DEBUG
        // System.out.println(Arrays.toString(interpolatedDeltas));
        return interpolatedDeltas;
    }
}