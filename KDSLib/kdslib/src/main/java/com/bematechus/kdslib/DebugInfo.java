package com.bematechus.kdslib;

/**
 * for debug
 *
 *
 */

public class DebugInfo extends Exception {
    public int line() {
        StackTraceElement[] trace = getStackTrace();
        if (trace == null || trace.length == 0) {
            return -1;
        }
        return trace[0].getLineNumber();
    }

    public String fun() {
        StackTraceElement[] trace = getStackTrace();
        if (trace == null || trace.length == 0) {
            return "";
        }
        return trace[0].getMethodName();
    }

    public DebugInfo() {
        super();
    }

    @Override
    public String toString() {
        return line() + "|" + fun() + "|";
    }

    public static int line(StackTraceElement e) {
        return e.getLineNumber();
    }

    public static String method(StackTraceElement e) {
        return e.getMethodName();
    }

    public static String info(StackTraceElement e) {
        String ret = line(e) + "|" + method(e) + "|";
        return ret;
    }
}

