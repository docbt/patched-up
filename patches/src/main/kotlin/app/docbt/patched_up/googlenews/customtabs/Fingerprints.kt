package app.docbt.patched_up.googlenews.customtabs

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

// All methods in Google News v5.158.0 that read Ladpx;->i:Z (iget-boolean).
// Ladpx = CustomTabsArticleLauncher; field i controls CustomTabs (true) vs WebView (false).
//
// Verified read sites (DEX bytecode scan):
//   classes.dex:  Laiud  (StartActivity handler)
//   classes3.dex: Ladrm  (handler)
//                 Laiuk  (handler)
//                 Laixq  (handler)
//                 Laixz  (handler)
//                 CustomTabsTrampolineActivity (onCreate)

internal object LajdkFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladpx;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Laiud;" },
)

internal object LaedzFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladpx;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Ladrm;" },
)

internal object LajdrFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladpx;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Laiuk;" },
)

internal object LajgzFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladpx;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Laixq;" },
)

internal object LajhkFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladpx;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Laixz;" },
)

// CustomTabsTrampolineActivity.onCreate() reads Ladpx.i; if i==0 it logs
// "Unexpected intent; activity is not enabled" and finishes. Patch i-read to always true.
internal object CustomTabsTrampolineFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladpx;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef ->
        classDef.type == "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;"
    },
)

// Ladqf.b() returns a Ladqg implementation based on experiment flag Laqhh.a.get().
// When the experiment is OFF, IF_EQZ branches to the disabled Ladqi (filters all browsers
// against an allowlist, returns null if none match), causing Ladpx.c=null → binding failure.
// Patching the IF_EQZ to NOP forces the enabled Ladql path (picks browser via resolveActivity).
internal object LaecrFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laqhh;", "a", "Laqhh;", Opcode.SGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladqf;" },
)

// Ladqi.a() — "disabled" Ladqg: filters installed browsers against an experiment allowlist,
// returns null if none match. IF_EQZ returns null when the filtered list is empty.
// NOP-ing it forces the fallback resolveActivity call which picks the default browser.
internal object LaecuFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladqi;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladqi;" },
)

// Ladql.a() — "enabled" Ladqg: picks the best CT-supporting browser via resolveActivity,
// then checks if it's in the experiment allowlist. IF_EQZ redirects to allowlist-only path
// when the default browser isn't allowlisted (e.g. Firefox/Brave). NOP-ing it always returns
// whatever resolveActivity found (default browser has priority).
internal object LaecxFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladql;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladql;" },
)
