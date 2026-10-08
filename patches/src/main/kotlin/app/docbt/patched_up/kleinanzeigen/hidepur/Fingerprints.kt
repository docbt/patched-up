package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.Fingerprint
import app.morphe.patches.all.misc.resources.ResourceType
import app.morphe.patches.all.misc.resources.resourceLiteral

// Anchors on both Pur title and subtitle resource IDs to reliably locate
// the settings list builder method across R8 refactoring passes.
internal object HidePurEligibilityFingerprint : Fingerprint(
    filters = listOf(
        resourceLiteral(ResourceType.STRING, "ka_gbl_pur"),
        resourceLiteral(ResourceType.STRING, "ka_settings_v2_top_ad_free_subscription_pur_subtitle"),
    ),
)
