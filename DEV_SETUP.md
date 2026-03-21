DEV_SETUP — Java & Gradle quick guide

Requirements

- Java 21 JDK is required to build the project. Any vendor that provides a Java 21 compliant JDK should work.

Recommendation

- For CI reproducibility we recommend Eclipse Temurin (Adoptium) 21, but developers may use any vendor.

macOS install (Homebrew)

```
# Install Temurin 21 (preferred for CI)
brew install --cask temurin@21
# Confirm Java 21 is installed
java -version   # should report version 21
```

macOS install (SDKMAN)

```
# See official install docs: https://sdkman.io/install
curl -sSfL "https://get.sdkman.io" -o install_sdkman.sh
# Optionally inspect the script before running it
less install_sdkman.sh
bash install_sdkman.sh
rm install_sdkman.sh
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21-temurin
```

Set JAVA_HOME (zsh)

```
export JAVA_HOME=$(/usr/libexec/java_home -v21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Local Gradle override (optional)

```
mkdir -p ~/.gradle
# Edit ~/.gradle/gradle.properties and add or update org.gradle.java.home,
# ensuring it is defined only once:
# 1) In a shell, run: /usr/libexec/java_home -v21  (copy the absolute path it prints)
# 2) Then set in gradle.properties, for example:
#    org.gradle.java.home=/absolute/path/from/java_home
```

Build (from repo root)

```
cd CARMA_android_app
./gradlew clean :app:assembleDebug
```

CI example (GitHub Actions)

```
- uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '21'
```

Notes

- The repo requires Java 21 only; vendor lock-in is not enforced. If your organization requires a specific vendor, document it and pin that in CI.
