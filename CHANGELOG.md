<!--
Template:
```
# 0.1.0
New features:
- Added some new feature / ability (3afd86c58d883970ddd236dc7c8a0d5c5a0d9e3)
  - If needed, additional explanation / context here

Behavior changes:
- An existing feature or function changed its behavior (e40dd711d748b6398611db97f54e1622ac008ae)
  - For example, different output to the user

Thanks to @ User1, @ User2, @ User3!
```

Also run the two find-and-replace regexes below for nice formatting:

To quickly annotate commit hashes, append the full hash in parantheses to each line and then run
this find-and-replace regex (VSCode flavor):
- Find: (?<!commit/)([0-9a-f]{7})([0-9a-f]{33})
- Replace: [$1](https://github.com/seraxis/lr2oraja-endlessdream/commit/$1$2)

To quickly make GitHub usernames into clickable links, prepend each username with @ and then run
this find-and-replace regex (VSCode flavor):
- Find: (?<=Thanks to.*)(?<!\[)@([a-z0-9]+)
- Replace: [@$1](https://github.com/$1)
-->

# 0.3.1 Prerelease

New features:
- Song manager now lets you sort songs by least recently played first. (#96)
- Random Trainer now lets you shift or mirror the lane order (#89)
- New launcher option (Music Select tab) to entirely skip the decide screen (#88)
- Can now configure the lane cover, lift, and hidden in-game (misc settings) (#102)
- Added support for sending scores to Discord via webhooks (#97)
- In-game leaderboards for beatoraja IR and for LR2IR (read-only) (#109)
- Automatically normalizing volume per-chart (#105)
- In-game skin widget manager (#79)
- Song select context menu, accessed with former autoplay and practice keys (#119)
- Live skin configuration (#107)
- New difficulty table management UI, allows adding from a built-in list of popular tables (#130)
- Ghost battle against LR2IR scores; accessed through the new in-game LR2IR leaderboard (#131)
- New option in the launcher to display LNs with CN endings (off by default) (#139)
- OBS WebSocket control integration (#135)

Behavior changes:
- Changed the default analog scratch threshold from 100 to 50 (9b3a17b6f0d0a82b1c878a561f039e94cea76b36)
- New ImGui presentation for all notifications (#74)
- Playing courses with CONSTANT no longer grants assist clear (#86)
- The spinning turntable emblem now spins nicer (#94)
- The sound of quickly scrolling through the music select is now less loud (#93)
- Improved audio when changing between scenes (#92)
- Music preview generated with the preview generator tool will now have lower priority (#91)
- Skins with large bitmap fonts will now load faster (#106)
- Currently downloading songs now display a progress bar (#110)
- Launcher: loading tables and BMS files will display a progress window. (#129) Please wait warmly...
- Switching to CN/HCN LN modes has been disabled in game but remains an option in the launcher (#139)
- Esc only closes the game when at the top level of music select, instead it closes the current open folder (#139)
- The game will now show a warning notification when playing a song with modifiers that disable score saving or IR submission (#122)
- In-game song downloader menu now features button to retry failed download tasks (#134)
- Configuration will now automatically save periodically (#136)
- Re-enabled the Konmai downloading service (4022c3c5da2e27a5368c360417d8e726e09a0c57)

Bug fixes:
- Fixed a crash when launching borderless without a set monitor (31f81b8cf87094cda152387cca74a9de5002ee4d)
- Fixed several issues with loading osu files ([parser #2](https://github.com/seraxis/jbms-parser/pull/2))
- On Linux, fixed opening chart folders with F3, the broken new version download link,
  and the launcher becoming non-functional after starting the game (#98)
- Fixed Discord rich presence on macOS (#127)
- Fixed bitmap font text display incorrectly becoming transparent when the previously
  displayed skin element made use of a blending mode. (fdc1d787a584af88f82c76756fa6d0be4ee48c2b)
- Fixed a crash caused by incorrect entries in some difficulty tables. (#128)
- Fixed the F4 fullscreen toggle button causing skins to malfunction with certain resolution configurations (#117)
- Fixed a Wayland JVM crash that affected users with nVidia graphics drivers (52a12e9b64852015ad8877be84d70829672325e0)
- Fixed a SQL usage bug that caused a memory leak while navigating music select  (ef7b0618cc58de744cc492173f2011a6f4dbdc7f)

Known issues:
- Downloading many charts simultaneously may cause the song database to lock up, requiring
  a game restart and manual folder reload to see the new songs.
- After changing music select skins using the new skin menu, some song wheel elements
  from the previous skin might remain on the screen until the game is restarted.
  After changing play skins, measure bars may display incorrectly until reload.
- [Linux] Certain skin fonts may only load partially due to incorrect letter case
  in their filenames. Can be manually resolved by renaming the offending files.
- [Linux] When loading configuration files created on Windows, skin settings will fail
  to transfer. Fix by replacing backslashes with forward slashes in the skin paths in
  saved skin settings in `config.json` and `config_player.json` in the player folder.

# 0.3.0

## Requires Java 17 to run correctly. [Download here](https://bell-sw.com/pages/downloads/#jdk-17-lts) (Ensure `Package: Full JDK`)

New features:
- Added in-game song downloader ([858100d](https://github.com/seraxis/lr2oraja-endlessdream/commit/858100da3cb883d5c083137bdd3733b73b1f2b23))
  - Attempting to play an undownloaded song initiates the download
  - Downloaded songs are added to a new directory for user sorting
- Added support for .osu chart files ([1e75da1](https://github.com/seraxis/lr2oraja-endlessdream/commit/1e75da163322ba0ebb187e8a8634adeb4e1f11c7))
  - Add your osu songs folder to your BMS path as you would any BMS folder
- Added support for Intel and Apple Silicon based Macs ([112c5ce](https://github.com/seraxis/lr2oraja-endlessdream/commit/112c5ce0ce7fd3d7942afef572486b40d142b35f))
- Added chart judge rank override feature in the mod menu ([a28dc06](https://github.com/seraxis/lr2oraja-endlessdream/commit/a28dc0677578cd4364eb831a232b703a496e51cf))
- Added reverse table lookup for charts ([5e59bcc](https://github.com/seraxis/lr2oraja-endlessdream/commit/5e59bcc0594a07b2800af3f49b36be3dbe649f8e))
- Added fullscreen and borderless monitor selection in the launcher ([6d5b85f](https://github.com/seraxis/lr2oraja-endlessdream/commit/6d5b85f7e835f707d43f3dc0b1822846aefda032))
- Updated to upstream beatoraja version 0.8.8 ([36064c8](https://github.com/seraxis/lr2oraja-endlessdream/commit/36064c84296f1c956e7c003f9ff73bf78304aa52))
  - Switch from Java 8 to Java 17
  - Adds CONSTANT function (in Endless Dream this will produce Assist Clears)
  - Some Skin properties have changed, please consult the source.

Behavior changes:
- Freq can no longer be changed mid-game, added flags to discriminate freq plays for IRs ([9afb79b](https://github.com/seraxis/lr2oraja-endlessdream/commit/9afb79bac45217968cc438c523cfbbc316218e34))
- Fixed the LR2 autoadjust logic ([3bad992](https://github.com/seraxis/lr2oraja-endlessdream/commit/3bad992bcd0c36be3d87a1a62ff7cdd5fdf45e82))
- Various fixes for state transition timings

Thanks to [@seraxis](https://github.com/seraxis), [@Catizard](https://github.com/Catizard), [@MatVeiQaaa](https://github.com/MatVeiQaaa)

# 0.2.1
New features:
- Added IntelliJ run gradle run configurations ([a7f3e75](https://github.com/seraxis/lr2oraja-endlessdream/commit/a7f3e751b682951da7b2ddf7a13d1bd3b35eaa74))

Behavior changes:
- Fixed the launcher not closing after player close ([9bfeaa1](https://github.com/seraxis/lr2oraja-endlessdream/commit/9bfeaa1db288643450b19a615c3b2f3cf26c4bbd))
  - This fixes settings not being saved between game launches

Thanks to [@seraxis](https://github.com/seraxis), [@corndogit](https://github.com/corndogit)!

# 0.2.0
New features:
- Updated to upstream beatoraja version 0.8.7 ([fb664a4](https://github.com/seraxis/lr2oraja-endlessdream/commit/fb664a4c7932a8821ba33244fed784d137ca10c2))
  - This brings support for base 62 keysound slots for BMS, some general code cleanup, and the addition of "pomu" mascot characters
  - a new directory `font/` is now required as of this version

Behavior changes:
- `F5` now opens the mod menu along with `Insert` ([800062d](https://github.com/seraxis/lr2oraja-endlessdream/commit/800062dfaa4eef2ce67e3f9d9c86b60051dd31e9))
- Auto adjust now works like LR2's: less sensitive with a more gentle hysteresis ([e81eecb](https://github.com/seraxis/lr2oraja-endlessdream/commit/e81eecb69f3f8d5c9d8a68c7f24acf55a5b161fd))
- Scores that fail to send to IR will now be retried throughout your session, no more lost scores ([d917fc7](https://github.com/seraxis/lr2oraja-endlessdream/commit/d917fc79d349f4d399093c7503fbad93faa7cb5e))
- The `Trainer` config tab has been removed ([800062d](https://github.com/seraxis/lr2oraja-endlessdream/commit/800062dfaa4eef2ce67e3f9d9c86b60051dd31e9))
- Error handling for malformed USB devices that present themselves as controllers ([4b7320d](https://github.com/seraxis/lr2oraja-endlessdream/commit/4b7320de9e786bb2eb808ac48b4c89cd0c660449))
- Negative FREQ being set in the Rates menu no longer stops scores from being saved even when the trainer is disabled ([9f865e5](https://github.com/seraxis/lr2oraja-endlessdream/commit/9f865e519cd89ccc7425e62f516669d7b01dfe96))

Thanks to [@seraxis](https://github.com/seraxis), [@wcko87](https://github.com/wcko87), [@MatVeiQaaa](https://github.com/MatVeiQaaa), [@radiden](https://github.com/radiden)!

# 0.1.1
New features:
- Added the **Rate Modifier**, an ImGui mod menu tool for changing the speed of charts ([e5df0c0](https://github.com/seraxis/lr2oraja-endlessdream/commit/e5df0c058cdd36795b14d687c21360699988096a))
- Consolidated the Random Trainer and new addition into a main menu ([e5df0c0](https://github.com/seraxis/lr2oraja-endlessdream/commit/e5df0c058cdd36795b14d687c21360699988096a))

Behavior changes:
- The 'illegal song hashes' have been removed, previously unimportable bms can now be played ([5b95bdf](https://github.com/seraxis/lr2oraja-endlessdream/commit/5b95bdf933b7560bb18808bd108d9f85fd295fff))
- The config version checker works again now, in English, and redirects you here ([e5df0c0](https://github.com/seraxis/lr2oraja-endlessdream/commit/e5df0c058cdd36795b14d687c21360699988096a))
- Error handling for failed bms/bmson parses during import ([42d07ee](https://github.com/seraxis/lr2oraja-endlessdream/commit/42d07eee732b47d538052fd8e9b27ea139e27b85))

Thanks to [@seraxis](https://github.com/seraxis), [@hadronyche](https://github.com/hadronyche)!

# 0.1.0
New features:
- Update from libGDX 1.9.8 to 1.12.1, migrate to gradle ([bd59751](https://github.com/seraxis/lr2oraja-endlessdream/commit/bd59751954be1c4a1db12014bd4988b598a2adab))
  - gdx-controllers no longer provides RawInput device support, [Lwjgl3ControllerManager](https://github.com/seraxis/lr2oraja-endlessdream/blob/bd59751954be1c4a1db12014bd4988b598a2adab/core/src/bms/player/beatoraja/controller/Lwjgl3ControllerManager.java) has been implemented to replace it
  - Switched the graphics backend provider from lwjgl 2.9.2 to lwjgl 3.3.3, this is reflected in a change in namespace for all Lwjgl functions
  - Switched from the Ant build system to Gradle 8.4, a custom [authored plugin](https://github.com/seraxis/lr2oraja-endlessdream/blob/bd59751954be1c4a1db12014bd4988b598a2adab/buildSrc/src/main/kotlin/org/endlessdream/extra/multiplatform-convention.gradle.kts) has been written to exclude unnecessary transitive dependencies
  - See the [version toml file](https://github.com/seraxis/lr2oraja-endlessdream/blob/bd59751954be1c4a1db12014bd4988b598a2adab/gradle/libs.versions.toml) for a full list of versions changed in this release
- Added dependency [imgui-java](https://github.com/SpaiR/imgui-java#readme), provides an interface for the random trainer accessible with `Insert` ([bd59751](https://github.com/seraxis/lr2oraja-endlessdream/commit/bd59751954be1c4a1db12014bd4988b598a2adab))
- Added logical operators for random select filters specified in `random/default.json` ([bdfa191](https://github.com/seraxis/lr2oraja-endlessdream/commit/bdfa19137b844cc1d6ab5dc237721ff730397241))

Behavior changes:
- Hi-speed Fix now displays in select even when not hovered over a song ([faaa9e1](https://github.com/seraxis/lr2oraja-endlessdream/commit/faaa9e19f82fdc73ae4e59095308137fc5c3eb5e))
- Song select random BGM no longer plays over itself if you quit out too fast ([d4092b3](https://github.com/seraxis/lr2oraja-endlessdream/commit/d4092b34c3b30998f5099515cd3b4de035abcbcf))
- IR Jars that were compatible before upstream commit [3c0c1fe](https://github.com/seraxis/lr2oraja-endlessdream/commit/3c0c1feca2df5a0d5d1dc5c3dc0be580ee39e6c8) (release 0.8.6) remain compatible and will work as usual ([7750aad](https://github.com/seraxis/lr2oraja-endlessdream/commit/7750aad06913fc9ac5b7585a1a787aa5e16afde2))
- BMS Path additions now process subdirectories in parallel, as a result library changes should be processed much faster ([bd59751](https://github.com/seraxis/lr2oraja-endlessdream/commit/bd59751954be1c4a1db12014bd4988b598a2adab))

Thanks to [@seraxis](https://github.com/seraxis), [@Kolyasisan](https://github.com/Kolyasisan), [@MatVeiQaaa](https://github.com/MatVeiQaaa)!

# 0.0.1

Initial Github Release
