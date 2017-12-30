package com.mashjulal.android.prostoplay;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by Master on 11.07.2017.
 */

public class ProstoPlayApplication extends Application {
    private static Bus bus = new Bus(ThreadEnforcer.ANY);

    public static Bus getBus() {
        return bus;
    }
}
