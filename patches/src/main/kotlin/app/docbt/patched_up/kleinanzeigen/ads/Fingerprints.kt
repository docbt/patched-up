package app.docbt.patched_up.kleinanzeigen.ads

import app.morphe.patcher.Fingerprint

// Liberty init method: initializes the ad/analytics SDK (Liberty SDK).
// "https://prebid-server.rubiconproject.com/openrtb2/auction" is a stable string inside the setup method
// across versions. Restricting to `returnType == "V"` (void) ensures we target the main
// initializer execution method rather than a configuration factory or getter.
internal object LibertyInitFingerprint : Fingerprint(
    strings = listOf("https://prebid-server.rubiconproject.com/openrtb2/auction"),
    custom = { method, _ -> method.returnType == "V" },
)
