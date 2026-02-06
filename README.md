<div align="center" style="line-height: 1;">

# LR2oraja \~Endless Dream\~

<!-- swap this between default and default-inverted to update the number -->
[![DISCORD](https://dcbadge.limes.pink/api/server/HutCHCZHns?theme=default-inverted)](https://discord.gg/HutCHCZHns)

</div>

Endless Dream is a community fork and drop-in replacement for [beatoraja](https://github.com/exch-bms2/beatoraja) that integrates quality of life patches and new features not present in the upstream version of the game.

Based on [LR2oraja](https://github.com/wcko87/lr2oraja), which is itself a fork with LR2 judges and gauges, Endless Dream aims to fix some of the outstanding issues with the upstream project while being a central place for modifications and extensions that may never be accepted by upstream.

### Key Features
* In-game song downloader
* LR2 GBATTLE support
* Osu file support
* On the fly ratemods/freq
* Increased performance by using the latest graphics backends available to libgdx
* Faster Table Processing
* Compatible with beatoraja 0.8.8 installs
* Built in Mod Menu, accessible using **`F5` or `Insert`**

## Downloads
> [!NOTE]
> As of 0.3.0 the Java version has changed from 8 to 17, please check the releases page to update your installations java version
### Download here
- [**Windows Download**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.4.0/lr2oraja-0.8.8-endlessdream-windows-0.4.0.jar)
- [**Linux Download**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.4.0/lr2oraja-0.8.8-endlessdream-linux-0.4.0.jar)
- [**Macos Download (Apple Silicon)**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.4.0/lr2oraja-0.8.8-endlessdream-macos-aarch64-0.4.0.jar)
- [**Macos Download (Intel)**](https://github.com/seraxis/lr2oraja-endlessdream/releases/download/v0.4.0/lr2oraja-0.8.8-endlessdream-macos-0.4.0.jar)

Development builds are created for every commit and published as [releases](https://github.com/seraxis/lr2oraja-endlessdream/releases)

### Installation

#### From Scratch

1. Download the latest [`beatoraja-0.8.8 JRE`](https://mocha-repository.info/download/beatoraja0.8.8-jre-win64.zip) bundled version.
2. Unzip the file
3. Copy the unzipped directory to any directory on your computer where you keep applications
4. Download the latest Endless Dream for your operating system [from the bottom of the release page](https://github.com/seraxis/lr2oraja-endlessdream/releases/tag/v0.4.0)
5. Go to your beatoraja directory and delete `beatoraja.jar`.
6. Copy the downloaded `lr2oraja.*.jar` from Step 4 into your beatoraja directory.
7. Rename the `lr2oraja.*.jar` file to `beatoraja.jar`

#### From an Existing Beatoraja Install

1. Create a copy of your existing beatoraja install directory.
2. Rename the copy to `endless-dream`.
3. Download the latest Endless Dream for your operating system [from the bottom of the release page](https://github.com/seraxis/lr2oraja-endlessdream/releases/tag/v0.4.0)
4. Go to your `endless-dream` directory and delete `beatoraja.jar`.
5. Copy the downloaded `lr2oraja.*.jar` from Step 3 into your beatoraja directory.
6. Rename the `lr2oraja.*.jar` file to `beatoraja.jar`

### Post Install
Once you're set up with a copy of LR2oraja Endless Dream you might want to check out the excellent [Beatoraja English Guide](https://github.com/wcko87/beatoraja-english-guide/wiki) that has answers to all of your questions about beatoraja and BMS, including a list of skins, where to get songs, and how to use tables.

Alternatively you can ask in our [Discord](https://discord.gg/HutCHCZHns) and we'll be happy to help you out.

## Building from source
A JDK 17 **with javafx** is required to build and run. Consider using [liberica JDK](https://bell-sw.com/pages/downloads/#jdk-17-lts), ensure that you download `Package: Full JDK` to get the JavaFX version.

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

## Working on Endless Dream
Use of an IDE, such as [IntelliJ Community Edition](https://www.jetbrains.com/idea/download/other.html), is recommended for working on Endless Dream.

### Contributing to the project
To get started working on Endless Dream [fork the project](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/fork-a-repo), and start programming on a new branch.

Once you're done you can [open a PR](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request-from-a-fork) against the main project, and one of the maintainers will review your code.

Before starting it's good to think about what you want to do, and to discuss your ideas with other collaborators. Open an issue, or consider joining the [Discord server](https://discord.gg/HutCHCZHns). It's the place where most communication and collaboration happens.

### Running and building
The gradle `core:runShadow` task can be used to quickly test and debug changes made to the project. The `core:shadowJar` task builds the project for your operating system. **Do not use the default gradle run tasks, they will not work.**

<img width="358" height="281" alt="Expanding the gradle tab on the right shows the available tasks for the project. Expanding `core` and then `application` and `shadow` reveals the tasks you will use to run and build the project" src="https://github.com/user-attachments/assets/0adcd7e7-724f-4653-a1b0-e1a637f623f0" />

### Running from an existing install of beatoraja
Configure the `runDir` system property to point to a beatoraja install. If you do not configure this it will run in the `assets/` folder of the git project.

Click the 'Three Dots' next to the run configurations panel in the window bar, Edit the runShadow configuration, and add `-DrunDir="[FULL PATH TO RAJA INSTALL]"`

<img width="1389" height="321" alt="A demonstration of adding a runDir to the runShadow task" src="https://github.com/user-attachments/assets/3dd096b7-6995-4ab7-b7e9-8a18e038dc83" />

If you'd like to test IR dependent changes append the `useIR` system property to the run configuration and set it to `true` (e.g. `-DuseIR=true`). Be aware of this [existing bug](https://github.com/seraxis/lr2oraja-endlessdream/issues/189) with this property.

### Cannot Resolve Symbols for project submodules
IntelliJ sometimes cannot identify the projects submodules leading to missing classes (e.g. `bms.model.*`). This does not affect building or running the project, but can be quite annoying.

First verify that `./core/dependencies` contains the `jbms-parser` and `jbmstable-parser` folders. If they are missing run `git submodule update --init --recursive` to fetch any submodules you may have missed while cloning.

If submodules are present go to `File --> Project Structure --> Modules --> core --> Dependencies --> Add --> JARs or Directories...` and add both `./core/dependencies/jbms-parser` and `./core/dependencies/jbmstable-parser` then hit Apply.

<img width="1687" height="321" alt="endlessdreamprojectstructure" src="https://github.com/user-attachments/assets/dd1b9d41-d1e6-42db-9139-8adbcada1014" />

After you are done you should see no further import related errors. If you encounter difficulty join the [Discord server](https://discord.gg/HutCHCZHns) to ask for help.

### Running from the command line
Running from the command line is as simple as building from source, and looks like this:

**Windows:**
```powershell
.\gradlew.bat core:runShadow -Dplatform=windows -DrunDir="C:\beatoraja0.8.8" -DuseIR=true
```
