package com.tycherin.impen.logic.ism;

public class IsmStatusCodes {

    public static final int IDLE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;

    public static final int OUTPUT_FULL = 100;
    public static final int MISSING_CHANNEL = 101;
    public static final int MISSING_SCS = 102;
    public static final int SIZE_MISMATCH = 103;

    public static final int UNKNOWN = 9999;

    public static boolean isError(final int statusCode) {
        return statusCode >= 100 && statusCode < 1000;
    }
}
