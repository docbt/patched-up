package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.Fingerprint

// getShowAdFreeSubscription() controls whether the Pur subscription entry
// is shown in the settings menu. Returning false hides it.
internal object ShowAdFreeSubscriptionFingerprint : Fingerprint(
    custom = { method, _ -> method.name == "getShowAdFreeSubscription" },
)
