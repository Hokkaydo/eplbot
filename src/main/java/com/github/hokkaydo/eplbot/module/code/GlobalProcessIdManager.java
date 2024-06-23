package com.github.hokkaydo.eplbot.module.code;

public class GlobalProcessIdManager {
    private static int currentNumber = 0;
    private static final int MAXNUMBER = 10;

    public synchronized int getNextNumber() {
        if (currentNumber >= MAXNUMBER) {
            currentNumber = 1; // Reset to 1 when maxNumber is reached
        } else {
            currentNumber++;
        }
        return currentNumber;
    }
    public synchronized void reset() {
        currentNumber = 0;
    }
}
