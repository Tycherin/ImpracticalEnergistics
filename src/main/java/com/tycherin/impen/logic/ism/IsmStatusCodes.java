package com.tycherin.impen.logic.ism;

public class IsmStatusCodes {

    public static final int IDLE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;

    public static final int OUTPUT_FULL = 100;
    public static final int MISSING_CHANNEL = 101;
    public static final int NOT_FORMATTED = 102;
    public static final int NO_CATALYSTS = 103;
    public static final int INSUFFICIENT_POWER = 104;
    public static final int CELL_FULL = 105;

    public static final int UNKNOWN = 9999;

    public static boolean isError(final int statusCode) {
        return statusCode >= 100 && statusCode < 1000;
    }
}
