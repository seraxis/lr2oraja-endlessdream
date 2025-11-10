# Uses caseys Just command runner https://github.com/casey/just
#
# This file is intended as a useful command line reference for project actions that
# are 
alias b := build
alias ba := build_all
alias p := pandoc
alias r := run
alias u := update

is_it_arm := if arch() == "aarch64" { arch() + "-" } else { "" }

default_arch := if os() == "macos" { if arch() == "aarch64" { "macos-arm" } else { os() } } else { os() }
default_slug := "lr2oraja-0.8.8-endlessdream-" + os() + "-" + is_it_arm + "pre0.3.1.jar"

# Build the project. Takes "windows", "linux", "macos", and "macos-arm" as arguments. Built JARs are placed in dist/
build arch=default_arch:
  ./gradlew core:shadowJar {{ if arch == "macos-arm" { "-Dplatform=macos -Darch=aarch64" } else { "-Dplatform=" + arch } }}  
  @echo "Justfile: Built JAR for " + arch + " successfully"

# Build the project for all architectures
build_all: (build "windows") (build "linux") (build "macos") (build "macos-arm")
  @echo "Justfile: Built all platforms successfully"

# Run the project in an existing directory 
run dir: build
  cp dist/{{ default_slug }} {{ dir }}/beatoraja.jar
  {{ dir }}/{{ if os_family() == "linux" { "beatoraja-config.command" } else { "beatoraja-config.bat" } }}

# Update submodules to point to the latest commit
update:
  git submodule update --remote --merge

# Build the whats new html file
pandoc:
  pandoc --wrap=preserve doc/whatsnew.md -f markdown -t html -s -o core/src/resources/whatsnew.html --template "./doc/template.html"

# Removes all gradle caches, build intermediaries, and runs gradle clean
[confirm("Are you sure you want to delete all gradle caches and build intermediaries? (y/n)")]
nuke:
  rm -rf ~/.gradle/caches/
  rm -rf ./core/build/
  rm -rf ./dist/
  gradle clean

