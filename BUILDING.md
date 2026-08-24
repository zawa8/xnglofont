# xNgloFont

An Android app that sets one of the 15 xNglo fonts (same list as
xNglobord and translet-xnglo's web font picker) as the **system-wide**
font on a rooted device.

## Why root is required

Windows lets any user install and apply a font system-wide with no
special permission. Android has never exposed an equivalent API to
third-party apps -- there's no "set system font" call to make. The
only non-root workaround that ever existed was Samsung's proprietary
FlipFont mechanism, and as of OneUI 8.5 (early 2026) Samsung locked
that down to only its own/Monotype-signed font packages, so it no
longer works for third-party apps either.

Root (Magisk) is the only reliable path left. This app does not
attempt to root your device -- it needs a device that's already
rooted.

## How it works

1. Asks root to find every `.ttf`/`.otf` file under `/system/fonts`,
   `/product/fonts`, and `/system_ext/fonts` on **this specific
   device** -- filenames vary by OEM/Android version, so nothing is
   hardcoded.
2. Builds a Magisk module (`/data/adb/modules/xnglofont_<fontid>/`)
   whose `system/` folder mirrors those exact paths, each containing
   the chosen xNglo font's bytes instead of the original.
3. Magisk's magic-mount overlays that onto the real partitions at
   boot. **Nothing on the actual `/system` partition is touched** --
   this is fully reversible by removing/disabling the module (via the
   in-app Remove button, or Magisk Manager itself), with no
   OTA-breaking or bootloop risk from a bad write.
4. Takes effect after a reboot (the in-app Reboot button, or do it
   manually).

## Known limitations

- Requires root. No non-root path exists on current Android.
- Some OEM skins load their font family from an APK resource rather
  than loose files in `/system/fonts` -- this approach won't affect
  those devices, since there's nothing to overwrite.
- Only tested conceptually, not on a real device yet (no rooted
  Android hardware in this build environment). Try it on a spare/test
  device first, and report back exactly what happens if something
  looks wrong -- happy to iterate.

## Getting the APK

Same as xNglobord: every push to `main` triggers
`.github/workflows/build-apk.yml`, which builds a debug APK on
GitHub's servers and publishes it to the repo's **Releases** page --
no Android Studio or local SDK needed. Grab the `.apk` there, or
trigger a build manually from the **Actions** tab.

## Building locally instead (optional)

```
gradle wrapper          # one-time, generates gradlew + gradle-wrapper.jar
./gradlew assembleDebug
```

## What's here vs. what's next

Done:
- Gradle project structure, manifest, launcher UI (`MainActivity.kt`)
- `LocalFonts.kt`: same 15-font list/order as xNglobord and
  translet-xnglo's LocalFontPicker.tsx, with the matching `.ttf` files
  bundled in `assets/fonts/` (1.3MB, same set as xNglobord)
- `RootShell.kt`: minimal su-shell runner
- `RootFontInstaller.kt`: discovers actual system font files on-device,
  builds and installs the Magisk module, handles uninstall/reboot
- GitHub Actions workflow for APK builds without a local toolchain

Not yet built:
- real-device testing/validation (see Known limitations above)
- a check for whether Magisk itself (vs. some other root method) is
  present, and clearer guidance if magic-mount isn't available
- icon/UI polish
