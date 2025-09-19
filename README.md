# LR2oraja \~Endless Dream\~

<hr>

<div align="center" style="line-height: 1;">

[![DISCORD](https://dcbadge.limes.pink/api/server/HutCHCZHns)](https://discord.gg/HutCHCZHns)

</div>

Endless Dream is a community fork and drop-in replacement for [beatoraja](https://github.com/exch-bms2/beatoraja) that integrates quality of life patches and new features not present in the upstream version of the game.

Based on [LR2oraja](https://github.com/wcko87/lr2oraja), which is itself a fork with LR2 judges and gauges, Endless Dream aims to fix some of the outstanding issues with the upstream project while being a central place for modifications and extensions that may never be accepted by upstream.

### Key Features
* In-game song downloader
* Osu file support
* On the fly ratemods/freq
* Increased performance by using the latest graphics backends available to libgdx
* Faster Table Processing
* Compatible with beatoraja 0.8.8 installs
* Built in Mod Menu, accessible using **`F5` or `Insert`**

### Downloads
Development builds are created for every commit and published as [releases](https://github.com/seraxis/lr2oraja-endlessdream/releases)
- [**Windows Download**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.3.0/lr2oraja-0.8.8-endlessdream-windows-0.3.0.zip)
- [**Linux Download**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.3.0/lr2oraja-0.8.8-endlessdream-linux-0.3.0.zip)

### Installing from scratch
If you don't have an existing beatoraja installation download the latest [`beatoraja-0.8.8 JRE`](https://mocha-repository.info/download/beatoraja0.8.8-jre-win64.zip) bundled version, or choose a version yourself from the [release page](https://mocha-repository.info/download.php).

## Building from source
A JDK 8 **with javafx** is required to build and run. Consider using [liberica JDK](https://bell-sw.com/pages/downloads/#jdk-17-lts)
Clone this repository with submodules
```sh
git clone --recurse-submodules git@github.com:seraxis/lr2oraja-endlessdream.git
```
Run the gradle wrapper for your operating system and specify your desired platform as a [gradle system property](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_system_properties)

**Windows:**
```powershell
.\gradlew.bat core:shadowJar -Dplatform=windows
```
**Linux:**
```sh
./gradlew core:shadowJar -Dplatform=linux
```
**MacOS:**
```sh
./gradlew core:shadowJar -Dplatform=macos
```

> [!NOTE]
> For `arm` user: add -Darch=aarch64

This task will create a jar located in `dist/` that can be used with any working installation of the game.
### Testing changes
Use of an IDE, such as [Intellij](https://www.jetbrains.com/idea/download/other.html), is recommended for working on Endless Dream.

The gradle `core:runShadow` task can be used to quickly test and debug changes made to the project.

Configure the `runDir` system property to point to a beatoraja install or leave blank to have it run in the assets folder

**Windows:**
```powershell
.\gradlew.bat core:runShadow -Dplatform=windows -DrunDir="C:\beatoraja0.8.8"
```

## Contributing
Please state the impact your changes will have on drop-in compatibility with an upstream beatoraja install.

TBD