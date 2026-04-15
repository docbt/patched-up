package app.docbt.patched_up.googlenews.customtabs

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

// All methods in Google News v5.156.0 that read Laebh;->i:Z (iget-boolean).
// Laebh = CustomTabsArticleLauncher; field i controls CustomTabs (true) vs WebView (false).
//
// Verified read sites (DEX bytecode scan):
//   classes.dex:  Lajev  (StartActivity handler)
//   classes3.dex: Lajii  (handler)
//                 Lajfc  (handler)
//                 Lajir  (handler)
//                 Laecx  (handler)
//                 CustomTabsTrampolineActivity (onCreate)

internal object LajdkFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laebh;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajev;" },
)

internal object LaedzFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laebh;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajii;" },
)

internal object LajdrFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laebh;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajfc;" },
)

internal object LajgzFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laebh;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajir;" },
)

internal object LajhkFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laebh;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Laecx;" },
)

// CustomTabsTrampolineActivity.onCreate() reads Laebh.i; if i==0 it logs
// "Unexpected intent; activity is not enabled" and finishes. Patch i-read to always true.
internal object CustomTabsTrampolineFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laebh;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef ->
        classDef.type == "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;"
    },
)

// aebp.b() returns a Laebq implementation based on experiment flag Laqps.a.get().f().
// When the experiment is OFF, IF_EQZ branches to the null-returning Laebq,
// which causes Laebh.k=null → Laebh.b() fails → CustomTabs never binds.
// Patching the IF_EQZ to NOP forces the always-enabled Laebq path (returns real browser pkg).
internal object LaecrFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laqps;", "a", "Laqps;", Opcode.SGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laebp;" },
)

// Laebs.a() — "disabled" Laebq: filters all installed browsers against an experiment allowlist,
// returns null if none match. IF_EQZ at [15] returns null when the filtered list is empty.
// NOP-ing it forces the fallback fa.a() call which picks the default browser via resolveActivity.
internal object LaecuFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laebs;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laebs;" },
)

// Laebv.a() — "enabled" Laebq: picks the best CT-supporting browser via fa.a(), then checks
// if it's in the experiment allowlist. IF_EQZ at [27] redirects to allowlist-only path when
// the default browser isn't allowlisted (e.g. Firefox/Brave). NOP-ing it always returns
// whatever fa.a() found (default browser has priority via resolveActivity).
internal object LaecxFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laebv;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laebv;" },
)
