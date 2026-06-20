package app.docbt.patched_up.googlenews.customtabs

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

// All methods in Google News v5.161.0 that read Ladut;->i:Z (iget-boolean).
// Ladut = ArticleLauncher; field i controls CustomTabs (true) vs WebView (false).
//
// Verified read sites (DEX bytecode scan):
//   classes.dex:  Lajdq  (handler, method D, two read sites)
//   classes3.dex: Ladwc  (handler)
//                 Lajdx  (handler)
//                 Lajhe  (handler)
//                 Lajhl  (handler)

internal object LajdqFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladut;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajdq;" },
)

internal object LadwcFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladut;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Ladwc;" },
)

internal object LajdxFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladut;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajdx;" },
)

internal object LajheFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladut;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajhe;" },
)

internal object LajhlFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladut;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajhl;" },
)

// Ladvb.a(Laqxf, Laqxf) — experiment flag switch.
// Reads Laqrp;->a:Laqrp; (experiment flag), calls .a().f() → boolean.
// IF_EQZ branches to the disabled Ladvf picker when the flag is OFF.
// NOP-ing the IF_EQZ forces the enabled Ladvi path (resolveActivity-based browser picker).
internal object LadvbFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laqrp;", "a", "Laqrp;", Opcode.SGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladvb;" },
)

// Ladvi.a() — "enabled" Ladvd impl: resolves the OS default browser via Leu.a(...), then checks
// if it's in the experiment allowlist (Laqrp). IF_EQZ skips the immediate return when the default
// browser isn't allowlisted (e.g. Firefox/Brave), falling back to an allowlist-only candidate list.
// NOP-ing it always returns the OS default browser, regardless of the allowlist.
internal object LadviFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladvi;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladvi;" },
)

// Ladvf.a() — "disabled" Ladvd impl: filters installed browsers against the experiment allowlist
// only (no default-browser fallback), returns null if the filtered list is empty. IF_EQZ returns
// null in that case. NOP-ing it always proceeds to compute a candidate from the (possibly empty)
// filtered list instead of bailing out early.
internal object LadvfFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladvf;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladvf;" },
)

// CustomTabsTrampolineActivity.onCreate() reads field a (Ladvd) and calls a()Ljava/lang/String;
// then does if-nez on the result; if null, it logs "Unexpected intent; activity is not enabled"
// and finishes. Patch the if-nez into an unconditional goto to its existing target so the
// enabled path is always taken regardless of what Ladvd.a() returns.
internal object CustomTabsTrampolineFingerprint : Fingerprint(
    filters = listOf(
        fieldAccess(
            "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;",
            "a",
            "Ladvd;",
            Opcode.IGET_OBJECT,
        ),
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;"
    },
)
