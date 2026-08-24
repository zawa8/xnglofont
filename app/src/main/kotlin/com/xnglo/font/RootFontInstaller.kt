package com.xnglo.font

import android.content.Context
import java.io.File

/**
 * Root-only system font installer. This is the piece Windows gives you
 * for free (any user-installed font, no admin tricks needed) but stock
 * Android has never exposed to third-party apps -- there is no public
 * API for "set the system font." The closest non-root workaround
 * (Samsung's FlipFont) has also just been locked down as of OneUI 8.5,
 * so root + Magisk is the only reliable path left.
 *
 * How it works:
 *   1. Ask root to `find` every .ttf/.otf under /system/fonts,
 *      /product/fonts, /system_ext/fonts. Whatever filenames actually
 *      exist on THIS device/OEM/Android version, that's what gets
 *      overwritten -- no hardcoded font-file list to go stale.
 *   2. Build a Magisk module directory
 *      (/data/adb/modules/xnglofont_<fontId>/) whose system/ folder
 *      mirrors those exact paths, each one containing the chosen
 *      xNglo font's bytes instead of the original.
 *   3. Magisk's magic-mount overlays that onto /system (and
 *      /product, /system_ext) at boot -- nothing on the real
 *      partition is touched, so it survives reboots but is fully
 *      reversible by disabling/removing the module in Magisk Manager
 *      (no OTA-breaking, no bootloop risk from a bad write).
 *
 * Caveats, stated plainly:
 *   - Requires root (Magisk or equivalent). No non-root path exists.
 *   - Font file naming varies by OEM/Android version -- this covers
 *     whatever's actually found on-device rather than guessing names,
 *     but a device that keeps its real font family elsewhere (some
 *     OEM skins load fonts from an APK resource, not loose files)
 *     won't be affected by this approach at all.
 *   - Takes effect after a reboot, since Magisk's mount happens at
 *     boot time.
 */
object RootFontInstaller {

    data class InstallResult(val success: Boolean, val message: String)

    private const val SYSTEM_FONT_DIRS = "/system/fonts /product/fonts /system_ext/fonts"

    fun moduleId(fontId: String): String = "xnglofont_${fontId.lowercase()}"

    /** Copies the chosen font's asset bytes to a path root can read (app-private storage is world-readable-by-root). */
    private fun stageFontFile(context: Context, option: LocalFontOption): File {
        val staged = File(context.filesDir, "staged_${option.assetFileName}")
        context.assets.open("fonts/${option.assetFileName}").use { input ->
            staged.outputStream().use { output -> input.copyTo(output) }
        }
        return staged
    }

    /** Every existing system font file path on this device, discovered fresh each time (not hardcoded). */
    fun discoverSystemFontFiles(): List<String> {
        val result = RootShell.run(
            "find $SYSTEM_FONT_DIRS -type f \\( -name '*.ttf' -o -name '*.otf' \\) 2>/dev/null"
        )
        return result.stdout.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
    }

    fun install(context: Context, option: LocalFontOption): InstallResult {
        if (!RootShell.hasRoot()) {
            return InstallResult(false, "No root access. This app only works on a rooted device (Magisk).")
        }

        val fontFiles = discoverSystemFontFiles()
        if (fontFiles.isEmpty()) {
            return InstallResult(false, "Couldn't find any system font files under $SYSTEM_FONT_DIRS on this device.")
        }

        val staged = try {
            stageFontFile(context, option)
        } catch (e: Exception) {
            return InstallResult(false, "Failed to read the ${option.displayName} font from assets: ${e.message}")
        }

        val id = moduleId(option.id)
        val moduleRoot = "/data/adb/modules/$id"

        val prop = """
            id=$id
            name=xNglo Font: ${option.displayName}
            version=v1
            versionCode=1
            author=xnglo
            description=Overlays the ${option.displayName} xi38 font onto every system font file found on this device.
        """.trimIndent()

        val commands = mutableListOf(
            "rm -rf $moduleRoot",
            "mkdir -p $moduleRoot"
        )
        // Write module.prop via a heredoc so we don't fight shell quoting.
        commands.add("cat > $moduleRoot/module.prop << 'EOF'\n$prop\nEOF")

        for (originalPath in fontFiles) {
            val destPath = "$moduleRoot${originalPath}" // mirrors the absolute path under the module root
            val destDir = destPath.substringBeforeLast('/')
            commands.add("mkdir -p '$destDir'")
            commands.add("cp '${staged.absolutePath}' '$destPath'")
            commands.add("chmod 644 '$destPath'")
        }
        commands.add("chmod 644 $moduleRoot/module.prop")

        val result = RootShell.run(*commands.toTypedArray())
        staged.delete()

        return if (result.exitCode == 0) {
            InstallResult(
                true,
                "Installed as Magisk module \"$id\", overlaying ${fontFiles.size} font file(s). Reboot to apply."
            )
        } else {
            InstallResult(false, "Install failed: ${result.stderr.ifBlank { result.stdout }}")
        }
    }

    /** Marks the module for removal on next reboot (Magisk convention) -- same effect as disabling it in Magisk Manager. */
    fun uninstall(fontId: String): InstallResult {
        if (!RootShell.hasRoot()) {
            return InstallResult(false, "No root access.")
        }
        val id = moduleId(fontId)
        val moduleRoot = "/data/adb/modules/$id"
        val result = RootShell.run(
            "[ -d $moduleRoot ] && touch $moduleRoot/remove || echo 'not installed'"
        )
        return if (result.exitCode == 0) {
            InstallResult(true, "Marked \"$id\" for removal. Reboot to restore the original system font.")
        } else {
            InstallResult(false, "Removal failed: ${result.stderr.ifBlank { result.stdout }}")
        }
    }

    fun reboot() {
        RootShell.run("reboot")
    }
}
