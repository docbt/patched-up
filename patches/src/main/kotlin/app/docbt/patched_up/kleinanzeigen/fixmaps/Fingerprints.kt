// Based on [Fix] Restore location maps via MicroG-RE by andrewliang25
// https://github.com/andrewliang25/morphe-patches
// Copyright (C) 2026 andrewliang25
// Licensed under GNU General Public License v3.0
// Adapted for Kleinanzeigen (com.ebay.kleinanzeigen) by plant-saver3000

package app.docbt.patched_up.kleinanzeigen.fixmaps
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// The method that resolves the Maps renderer context via DynamiteModule.
// Anchored on Google Maps SDK string literals — not on any obfuscated app
// class name, so this survives every Kleinanzeigen version bump unchanged.
// All map surfaces (item location, search radius, store locator) load their
// renderer through this single method, so one injection covers everything.
internal object MapsModuleContextFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC),
    returnType = "Landroid/content/Context;",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/google/android/gms/maps/MapsInitializer\$Renderer;",
    ),
    filters = listOf(
        string("com.google.android.gms.maps_legacy_dynamite"),
        string("com.google.android.gms.maps_core_dynamite"),
        string("com.google.android.gms.maps_dynamite"),
        string("Unable to load maps module, maps container context is null"),
    ),
)
