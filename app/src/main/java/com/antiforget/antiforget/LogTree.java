package com.antiforget.antiforget;

import android.util.Log;

import com.jraska.console.Console;

import org.joda.time.DateTime;

import timber.log.Timber.Tree;


public class LogTree extends Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        Console.writeLine(DateTime.now().toString("HH:mm:ss") + ": " + message);
        Console.writeLine();
    }
}
