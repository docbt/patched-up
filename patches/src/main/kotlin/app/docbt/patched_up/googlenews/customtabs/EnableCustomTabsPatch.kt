package app.docbt.patched_up.googlenews.customtabs

import app.docbt.patched_up.all.misc.packagename.changePackageNamePatch
import app.morphe.patcher.extensions.InstructionExtensions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private val COMPAT = Compatibility(
    name = "Google News",
    packageName = "com.google.android.apps.magazines",
    appIconColor = 0x4285F4,
    targets = listOf(
        AppTarget(version = "5.158.0.908428942"),
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
            // Step 1: Patch Ladqf.b() — replace IF_EQZ experiment flag check with NOP.
            // Ladqf.b() normally returns the disabled Ladqi picker when the experiment is OFF,
            // causing Ladpx.c=null and binding failure. With NOP, it always returns the
            // enabled Ladql picker, which uses PackageManager to find a CT-supporting browser.
            val laecrMethod = LaecrFingerprint.method
            var ifEqzIndex = -1
            for ((i, instr) in laecrMethod.implementation!!.instructions.withIndex()) {
                if (instr.opcode == Opcode.IF_EQZ) {
                    ifEqzIndex = i
                    break
                }
            }
            check(ifEqzIndex != -1) { "IF_EQZ not found in Ladqf.b()" }
            laecrMethod.replaceInstruction(ifEqzIndex, "nop")

            // Step 2: Bypass experiment allowlist in Ladql.a() (enabled Ladqg picker).
            // Ladql.a() picks the best CT-supporting browser via resolveActivity, then checks
            // if it is in the experiment allowlist. Non-Chrome browsers are not in that list.
            // NOP-ing the IF_EQZ makes it always return whatever resolveActivity found.
            LaecxFingerprint.methodOrNull?.let { method ->
                val idx = method.implementation!!.instructions.indexOfFirst {
                    it.opcode == Opcode.IF_EQZ
                }
                if (idx != -1) method.replaceInstruction(idx, "nop")
            }

            // Step 3: Bypass allowlist in Ladqi.a() (disabled Ladqg picker, belt+suspenders).
            // Ladqi.a() filters installed browsers against the allowlist first.
            // NOP-ing the IF_EQZ always attempts resolveActivity so the default browser is tried.
            LaecuFingerprint.methodOrNull?.let { method ->
                val idx = method.implementation!!.instructions.indexOfFirst {
                    it.opcode == Opcode.IF_EQZ
                }
                if (idx != -1) method.replaceInstruction(idx, "nop")
            }

            // Step 4: In every method that reads Ladpx.i, replace iget-boolean Ladpx;->i:Z
            // with const/4 vX, 0x1 so the CustomTabs branch is always taken.
            // Covers click handlers, navigation, ReadNow, and CustomTabsTrampolineActivity.
            val methods = listOf(
                LajdkFingerprint.method,
                LaedzFingerprint.methodOrNull,
                LajdrFingerprint.methodOrNull,
                LajgzFingerprint.methodOrNull,
                LajhkFingerprint.methodOrNull,
                CustomTabsTrampolineFingerprint.methodOrNull,
            ).filterNotNull()

            for (method in methods) {
                val targets = mutableListOf<Int>()
                var index = 0
                for (instr in method.implementation!!.instructions) {
                    if (instr.opcode == Opcode.IGET_BOOLEAN) {
                        val ref = (instr as ReferenceInstruction).reference
                        if (ref is FieldReference && ref.definingClass == "Ladpx;" && ref.name == "i") {
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
        }
    }
}
