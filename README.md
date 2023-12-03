# LR2oraja \~Endless Dream\~

JDK 8 with javafx required to build and run. Consider using [liberica JDK](https://bell-sw.com/pages/downloads/#jdk-8-lts)

Build artifacts located in `dist/`. Use `-Pplatform=[windows/linux/macos]` to select appropriate platform natives

Build command:

`./gradlew core:shadowJar -Pplatform=windows`

Run command:

`./gradlew core:runShadow -Pplatform=windows`

