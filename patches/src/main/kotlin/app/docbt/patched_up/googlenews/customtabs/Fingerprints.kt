package app.docbt.patched_up.googlenews.customtabs

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

// All methods in Google News v5.163.0 that read Ladzz;->i:Z (iget-boolean).
// Ladzz = ArticleLauncher; field i controls CustomTabs (true) vs WebView (false).
//
// Verified read sites (DEX bytecode scan):
//   classes.dex:  Lajpv  (handler, two read sites)
//   classes3.dex: Lajtq  (handler)
//                 Lajtj  (handler)
//                 Laebi  (handler)
//                 Lajqc  (handler)

internal object LajpvFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladzz;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajpv;" },
)

internal object LajtqFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladzz;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajtq;" },
)

internal object LajtjFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladzz;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajtj;" },
)

internal object LaebiFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladzz;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Laebi;" },
)

internal object LajqcFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladzz;", "i", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajqc;" },
)

// Laeah.a(Larjq, Larjq) — experiment flag switch.
// Reads Lardw;->a:Lardw; (experiment flag), calls .a().f() → boolean.
// IF_EQZ branches to the disabled Laeal picker when the flag is OFF.
// NOP-ing the IF_EQZ forces the enabled Laeao path (resolveActivity-based browser picker).
internal object LaeahFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Lardw;", "a", "Lardw;", Opcode.SGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laeah;" },
)

// Laeao.a() — "enabled" Laeaj impl: resolves the OS default browser via Lew.a(...), then checks
// if it's in the experiment allowlist (Lardw). IF_EQZ skips the immediate return when the default
// browser isn't allowlisted (e.g. Firefox/Brave), falling back to an allowlist-only candidate list.
// NOP-ing it always returns the OS default browser, regardless of the allowlist.
internal object LaeaoFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laeao;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laeao;" },
)

// Laeal.a() — "disabled" Laeaj impl: filters installed browsers against the experiment allowlist
// only (no default-browser fallback), returns null if the filtered list is empty. IF_EQZ returns
// null in that case. NOP-ing it always proceeds to compute a candidate from the (possibly empty)
// filtered list instead of bailing out early.
internal object LaealFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laeal;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laeal;" },
)

// CustomTabsTrampolineActivity.onCreate() reads field a (Laeaj) and calls a()Ljava/lang/String;
// then does if-nez on the result; if null, it logs "Unexpected intent; activity is not enabled"
// and finishes. Patch the if-nez into an unconditional goto to its existing target so the
// enabled path is always taken regardless of what Laeaj.a() returns.
internal object CustomTabsTrampolineFingerprint : Fingerprint(
    filters = listOf(
        fieldAccess(
            "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;",
            "a",
            "Laeaj;",
            Opcode.IGET_OBJECT,
        ),
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;"
    },
)
