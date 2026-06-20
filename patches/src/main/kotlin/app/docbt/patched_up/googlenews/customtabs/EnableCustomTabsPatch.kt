package app.docbt.patched_up.googlenews.customtabs

import app.docbt.patched_up.all.misc.packagename.changePackageNamePatch
import app.morphe.patcher.extensions.InstructionExtensions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderOffsetInstruction
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private val COMPAT = Compatibility(
    name = "Google News",
    packageName = "com.google.android.apps.magazines",
    appIconColor = 0x4285F4,
    targets = listOf(
        AppTarget(version = "5.161.0.931240252"),
    ),
)

@Suppress("unused")
val enableCustomTabsPatch = bytecodePatch(
    name = "Enable CustomTabs",
    description = "Enables CustomTabs to open articles in your default browser.",
) {
    dependsOn(changePackageNamePatch)

    compatibleWith(COMPAT)

    execute {
        with(InstructionExtensions) {
            // Step 1: Patch Ladvb.a() — replace IF_EQZ experiment flag check with NOP.
            // Ladvb.a() reads Laqrp experiment flag and returns the disabled Ladvf picker
            // when the flag is OFF. With NOP, it always returns the enabled Ladvi picker.
            val advbMethod = LadvbFingerprint.method
            var ifEqzIndex = -1
            for ((i, instr) in advbMethod.implementation!!.instructions.withIndex()) {
                if (instr.opcode == Opcode.IF_EQZ) {
                    ifEqzIndex = i
                    break
                }
            }
            check(ifEqzIndex != -1) { "IF_EQZ not found in Ladvb.a()" }
            advbMethod.replaceInstruction(ifEqzIndex, "nop")

            // Step 2: Bypass experiment allowlist in Ladvi.a() (enabled browser picker).
            // Ladvi.a() resolves the OS default browser, then checks if it's in the experiment
            // allowlist. NOP-ing the IF_EQZ makes it always return whatever the OS resolved.
            LadviFingerprint.methodOrNull?.let { method ->
                val idx = method.implementation!!.instructions.indexOfFirst {
                    it.opcode == Opcode.IF_EQZ
                }
                if (idx != -1) method.replaceInstruction(idx, "nop")
            }

            // Step 3: Bypass allowlist-only filtering in Ladvf.a() (disabled picker, belt+suspenders).
            // Ladvf.a() returns null when no installed browser matches the allowlist. NOP-ing the
            // IF_EQZ always proceeds to compute a candidate instead of bailing out early.
            LadvfFingerprint.methodOrNull?.let { method ->
                val idx = method.implementation!!.instructions.indexOfFirst {
                    it.opcode == Opcode.IF_EQZ
                }
                if (idx != -1) method.replaceInstruction(idx, "nop")
            }

            // Step 4: In every method that reads Ladut.i, replace iget-boolean Ladut;->i:Z
            // with const/4 vX, 0x1 so the CustomTabs branch is always taken.
            val methods = listOf(
                LajdqFingerprint.method,
                LadwcFingerprint.methodOrNull,
                LajdxFingerprint.methodOrNull,
                LajheFingerprint.methodOrNull,
                LajhlFingerprint.methodOrNull,
            ).filterNotNull()

            for (method in methods) {
                val targets = mutableListOf<Int>()
                var index = 0
                for (instr in method.implementation!!.instructions) {
                    if (instr.opcode == Opcode.IGET_BOOLEAN) {
                        val ref = (instr as ReferenceInstruction).reference
                        if (ref is FieldReference && ref.definingClass == "Ladut;" && ref.name == "i") {
                            targets.add(index)
                        }
                    }
                    index++
                }
                for (i in targets) {
                    val reg = method.getInstruction<OneRegisterInstruction>(i).registerA
                    method.replaceInstruction(i, "const/4 v$reg, 0x1")
                }
            }

            // Step 5: CustomTabsTrampolineActivity.onCreate() reads field a (Ladvd), calls
            // Ladvd.a(), and does an IF_NEZ on the (possibly null) result; if null, it logs
            // "Unexpected intent; activity is not enabled" and finishes. Convert the IF_NEZ into
            // an unconditional GOTO to its existing target so the enabled path is always taken.
            val trampolineMethod = CustomTabsTrampolineFingerprint.method
            val fieldReadIndex = trampolineMethod.implementation!!.instructions.indexOfFirst {
                if (it.opcode != Opcode.IGET_OBJECT) return@indexOfFirst false
                val ref = (it as ReferenceInstruction).reference
                ref is FieldReference && ref.definingClass ==
                    "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;" &&
                    ref.name == "a" && ref.type == "Ladvd;"
            }
            check(fieldReadIndex != -1) { "Field read of CustomTabsTrampolineActivity.a not found" }

            val ifNezIndex = trampolineMethod.implementation!!.instructions
                .drop(fieldReadIndex)
                .indexOfFirst { it.opcode == Opcode.IF_NEZ } + fieldReadIndex
            check(ifNezIndex >= fieldReadIndex) { "IF_NEZ not found after CustomTabsTrampolineActivity.a read" }

            val target = trampolineMethod.getInstruction<BuilderOffsetInstruction>(ifNezIndex).target
            trampolineMethod.replaceInstruction(ifNezIndex, BuilderInstruction10t(Opcode.GOTO, target))
        }
    }
}
