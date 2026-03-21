DEV_SETUP — Java & Gradle quick guide

Requirements

- Java 21 JDK is required to build the project. Any vendor that provides a Java 21 compliant JDK should work.

Recommendation

- For CI reproducibility we recommend Eclipse Temurin (Adoptium) 21, but developers may use any vendor.

macOS install (Homebrew)

```
# Install Temurin 21 (preferred for CI)
brew install --cask temurin@21
# or generic cask
brew install --cask temurin
```

macOS install (SDKMAN)

```
# See official install docs: https://sdkman.io/install
curl -s "https://get.sdkman.io" -o install_sdkman.sh
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
# Edit ~/.gradle/gradle.properties and add or update the following line,
# ensuring org.gradle.java.home is defined only once:
# org.gradle.java.home=$(/usr/libexec/java_home -v21)
```

Build (from repo root)

```
./CARMA_android_app/gradlew clean :app:assembleDebug
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
