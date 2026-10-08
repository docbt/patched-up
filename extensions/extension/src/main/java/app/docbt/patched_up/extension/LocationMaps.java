// Based on LocationMaps.java by andrewliang25
// https://github.com/andrewliang25/morphe-patches
// Copyright (C) 2026 andrewliang25
// Licensed under GNU General Public License v3.0
// Adapted for Kleinanzeigen (com.ebay.kleinanzeigen) by plant-saver3000

package app.docbt.patched_up.extension;

import android.content.Context;
import android.util.Log;

public final class LocationMaps {

    private LocationMaps() {}

    private static final String TAG = "PatchedUpMaps";
    private static final String MICROG_PACKAGE = "app.revanced.android.gms";
    private static final String CREATOR_CLASS = "com.google.android.gms.maps.internal.CreatorImpl";

    private static Context cached;
    private static boolean tried;

    public static Context getMapsContext(Context context) {
        if (tried) return cached;
        if (context == null) return null;
        tried = true;

        try {
            Context microG = context.createPackageContext(
                MICROG_PACKAGE,
                Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            microG.getClassLoader().loadClass(CREATOR_CLASS);
            cached = microG;
            Log.i(TAG, "Loading maps from MicroG-RE.");
        } catch (Throwable t) {
            cached = null;
            Log.w(TAG, "MicroG-RE maps unavailable; leaving maps on Play Services.", t);
        }

        return cached;
    }
}
