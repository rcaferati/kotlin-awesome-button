# Releasing

This package publishes to Maven Central as:

```text
dev.caferati:awesome-button-compose
```

## One-time Setup

1. Create or confirm a Sonatype Central Portal account.
2. Verify the `dev.caferati` namespace in Central Portal.
3. Create a Central Portal user token.
4. Create a GPG key and distribute the public key.
5. Store release secrets in either `~/.gradle/gradle.properties` or CI secrets.

Local Gradle properties:

```properties
mavenCentralUsername=...
mavenCentralPassword=...
signingInMemoryKey=...
signingInMemoryKeyPassword=...
```

CI environment variables:

```text
ORG_GRADLE_PROJECT_mavenCentralUsername
ORG_GRADLE_PROJECT_mavenCentralPassword
ORG_GRADLE_PROJECT_signingInMemoryKey
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
```

GitHub Actions reads those values from these repository secrets:

```text
MAVEN_CENTRAL_USERNAME
MAVEN_CENTRAL_PASSWORD
SIGNING_IN_MEMORY_KEY
SIGNING_IN_MEMORY_KEY_PASSWORD
```

The Central Portal username/password must be a generated user token, not the
password used to log in to the web UI.

References:

- Sonatype requirements: https://central.sonatype.org/publish/requirements/
- Sonatype namespace verification: https://central.sonatype.org/register/namespace/
- Vanniktech Maven publish plugin: https://vanniktech.github.io/gradle-maven-publish-plugin/central/

## Release Checklist

1. Update `VERSION_NAME` in `gradle.properties`.
2. Update `CHANGELOG.md`.
3. Confirm README install snippets use the same version.
4. Run package checks:

```bash
./gradlew :awesome-button-compose:testDebugUnitTest
./gradlew --no-configuration-cache :awesome-button-compose:connectedDebugAndroidTest
./gradlew :awesome-button-compose:assembleRelease
```

5. Publish locally and inspect generated artifacts:

```bash
./gradlew :awesome-button-compose:publishToMavenLocal
```

Expected local artifacts:

- `awesome-button-compose-<version>.aar`
- `awesome-button-compose-<version>-sources.jar`
- `awesome-button-compose-<version>-javadoc.jar`
- `awesome-button-compose-<version>.pom`

6. Confirm generated POM metadata includes name, description, URL, license,
   developer, SCM, and Compose dependencies.
7. Create and push a tag:

```bash
git tag -a v1.1.0 <release-merge-commit> -m "Awesome Button Compose 1.1.0"
git push origin refs/tags/v1.1.0
```

8. Confirm the tag-triggered GitHub Actions workflow runs the verified publication step. For a
   transient retry, dispatch the same workflow at the immutable `v1.1.0` tag. Do not create or move a
   replacement tag.

For an explicitly chosen local fallback, run the publication command from the same clean release
commit before pushing the tag, and do not also run the automated publication path:

```bash
./gradlew :awesome-button-compose:publishAndReleaseToMavenCentral
```

9. Confirm Central Portal validation succeeds.
10. Wait for Maven Central propagation and verify consumer installation:

```kotlin
implementation("dev.caferati:awesome-button-compose:1.1.0")
```
