alias b := build
alias r := run

build:
  gradle core:shadowJar


run: build
  cp core/build/libs/lr2oraja-0.8.6-endlessdream-all.jar ~/bms/beatoraja0.8.5-modernchic/beatoraja.jar
  ./../../bms/beatoraja0.8.5-modernchic/beatoraja-config.command


nuke:
  rm -rf ~/.gradle/caches/
  rm -rf ./core/build/
  gradle clean

