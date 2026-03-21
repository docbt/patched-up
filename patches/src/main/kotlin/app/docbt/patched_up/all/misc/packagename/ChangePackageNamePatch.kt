/*
 * Ported from https://github.com/MorpheApp/morphe-patches (GPL v3)
 * Original copyright 2026 Morphe.
 *
 * Original hard forked code:
 * https://github.com/ReVanced/revanced-patches/commit/724e6d61b2ecd868c1a9a37d465a688e83a74799
 */
package app.docbt.patched_up.all.misc.packagename

import app.morphe.patcher.patch.Option
import app.morphe.patcher.patch.OptionException
import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.util.logging.Logger

private const val PACKAGE_NAME_REDDIT = "com.reddit.frontpage"

lateinit var packageNameOption: Option<String>

private fun NodeList.elements(): List<Element> =
    (0 until length).map { item(it) as Element }

/**
 * Set the package name to use.
 * If this is called multiple times, the first call will set the package name.
 *
 * @param fallbackPackageName The package name to use if the user has not already specified a package name.
 * @return The package name that was set.
 * @throws OptionException.ValueValidationException If the package name is invalid.
 */
fun setOrGetFallbackPackageName(fallbackPackageName: String): String {
    val packageName = packageNameOption.value!!

    return if (packageName == packageNameOption.default) {
        fallbackPackageName.also { packageNameOption.value = it }
    } else {
        packageName
    }
}

context(ResourcePatchContext)
private fun applyProvidersStrings(oldPackageName: String, newPackageName: String) {
    document("res/values/strings.xml").use { document ->
        val children = document.documentElement.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i) as? Element ?: continue

            node.textContent = when (node.getAttribute("name")) {
                "provider_authority_appdata", "provider_authority_file",
                "provider_authority_userdata", "provider_workmanager_init"
                    -> node.textContent.replace(oldPackageName, newPackageName)

                else -> continue
            }
        }
    }
}

@Suppress("unused")
val changePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Appends \".morphe\" to the package name by default. " +
            "Changing the package name of the app can lead to unexpected issues.",
    use = false,
) {
    packageNameOption = stringOption(
        key = "packageName",
        default = "Default",
        values = mapOf("Default" to "Default"),
        title = "Package name",
        description = "The name of the package to rename the app to.",
        required = true,
    ) { value ->
        value == "Default" || value!!.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$"))
    }

    val updatePermissions = booleanOption(
        key = "updatePermissions",
        default = false,
        title = "Update permissions",
        description = "Update compatibility receiver permissions. " +
            "Enabling this can fix installation errors, but this can also break features in certain apps.",
    ).value

    val updateProviders = booleanOption(
        key = "updateProviders",
        default = false,
        title = "Update providers",
        description = "Update provider names declared by the app. " +
            "Enabling this can fix installation errors, but this can also break features in certain apps.",
    ).value

    val updateProvidersStrings = booleanOption(
        key = "updateProvidersStrings",
        default = false,
        title = "Update providers strings",
        description = "Update additional provider names declared by the app in the strings.xml file. " +
                "Enabling this can fix installation errors, but this can also break features in certain apps.",
    ).value

    fun getReplacementPackageName(originalPackageName: String): String {
        val replacementPackageName = packageNameOption.value
        return if (replacementPackageName != packageNameOption.default) {
            replacementPackageName!!
        } else {
            "$originalPackageName.morphe"
        }
    }

    finalize {
        val incompatibleAppPackages = setOf<String>()

        val packageName = packageMetadata.packageName
        val newPackageName = getReplacementPackageName(packageName)

        val applyUpdatePermissions: Boolean
        val applyUpdateProviders: Boolean
        val applyUpdateProvidersStrings: Boolean

        when (packageName) {
            PACKAGE_NAME_REDDIT -> {
                applyUpdatePermissions = true
                applyUpdateProviders = true
                applyUpdateProvidersStrings = true
            }
            else -> {
                applyUpdatePermissions = updatePermissions!!
                applyUpdateProviders = updateProviders!!
                applyUpdateProvidersStrings = updateProvidersStrings!!
            }
        }

        if (applyUpdateProvidersStrings) {
            applyProvidersStrings(packageName, newPackageName)
        }

        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0) as Element

            if (incompatibleAppPackages.contains(packageName)) {
                return@finalize Logger.getLogger(this::class.java.name).severe(
                    "'$packageName' does not work correctly with \"Change package name\"",
                )
            }

            manifest.setAttribute("package", newPackageName)

            if (applyUpdatePermissions) {
                val receiverNotExported = "DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
                val androidName = "android:name"
                val oldName = "$packageName.$receiverNotExported"
                val newName = "$newPackageName.$receiverNotExported"

                (document.getElementsByTagName("permission").elements() +
                    document.getElementsByTagName("uses-permission").elements())
                    .filter { it.getAttribute(androidName) == oldName }
                    .forEach { it.setAttribute(androidName, newName) }
            }

            if (applyUpdateProviders) {
                val androidAuthority = "android:authorities"
                val authorityPrefix = "$packageName."

                document.getElementsByTagName("provider").elements().forEach { provider ->
                    val authorities = provider.getAttribute(androidAuthority)
                    if (authorities.startsWith(authorityPrefix)) {
                        provider.setAttribute(
                            androidAuthority,
                            authorities.replace(packageName, newPackageName)
                        )
                    }
                }
            }
        }
    }
}
