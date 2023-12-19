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