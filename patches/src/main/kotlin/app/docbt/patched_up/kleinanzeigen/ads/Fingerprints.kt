package app.docbt.patched_up.kleinanzeigen.ads

import app.morphe.patcher.Fingerprint

// Liberty.init() initializes the ad/analytics SDK.
// Returning early prevents ads and Microsoft Clarity analytics from loading.
internal object LibertyInitFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type.contains("/Liberty;") && method.name == "init"
    },
)
