package com.example.android.bluetoothlegatt;

/**
 * Created by pranav on 11/9/17.
 */

public class GestureKNN {
    private static int ROW_LENGTH = 4;
    private static int MAX_GESTURE_LENGTH = 10;
    private static int[][] GESTURE_LIBRARY = {
            { 1,  0,  1,  0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            { -1,  0,  -1,  0,  -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            { 0,  -1,  0,  -1,  0,  -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            { 0,  1,  0,  1,  0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
    };
    private static String[] GESTURE_NAMES = {"LRSwipe", "RLSwipe", "UpSwipe", "DownSwipe"};


    public GestureKNN() {

    }

    public static String recognize(String input) {
        int[] gestureDeltas = createGestureDeltas(input);
        int[] MSE = new int[GESTURE_LIBRARY.length];
        for (int i = 0; i < GESTURE_LIBRARY.length; ++i) {
            // Loop over the gesture deltas and compare
            int currentMSE = 0;
            for (int j = 0; j < gestureDeltas.length; ++j) {
                currentMSE += Math.pow((gestureDeltas[j] - GESTURE_LIBRARY[i][j]), 2);
            }
            MSE[i] = currentMSE;
        }
        return getMinGesture(MSE);
    }

    private static String getMinGesture(int[] MSE) {
        int min = 1024 * 1024;
        int minIndex = 0;
        for (int i = 0; i < MSE.length; ++i) {
            if (MSE[i] < min) {
                min = MSE[i];
                minIndex = i;
            }
        }
        return GESTURE_NAMES[minIndex];
    }

    private static int[] createGestureDeltas(String input) {
        int x, y, nextX, nextY;
        int[] gestureDeltas = new int[MAX_GESTURE_LENGTH * 2];
        nextX = (input.charAt(0) - 97) % ROW_LENGTH;
        nextY = (input.charAt(0) - 97) / ROW_LENGTH;
        for (int i = 0; i < input.length() - 1; ++i) {
            x = nextX;
            y = nextY;
            nextX = (input.charAt(i) - 97) % ROW_LENGTH;
            nextY = (input.charAt(i + 1) - 97) / ROW_LENGTH;
            gestureDeltas[2 * i] = nextX - x;
            gestureDeltas[2 * i + 1] = nextY - y;
        }
        return gestureDeltas;
    }
}
