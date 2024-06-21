<div align="center">
  <img style="border-radius: 50%" src="./androidApp/src/androidMain/ic_launcher-playstore.png" width="100px">
  <h1>Sync Sphere</h1>
</div>

Are you tired of endless group chats and email threads trying to plan a simple get-together? Sync Sphere is here to streamline your social planning with ease and efficiency.

Create a Room, Share, and Sync!

[![CI](https://github.com/j-roskopf/SyncSphere/actions/workflows/build.yml/badge.svg)](https://github.com/j-roskopf/SyncSphere/actions/workflows/build.yml)

## Download 📦

Sync Sphere is available for Android, iOS, and Desktop

<div align="center"><a href="https://apps.apple.com/us/app/sync-sphere/id6471382890"><img src="./assets/app_store_download.svg" width="200px"/></a></div>
<div align="center"><a href="https://play.google.com/store/apps/details?id=com.joetr.sync.sphere"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="230px"/></a></div>
<div align="center"><a href="https://apps.apple.com/us/app/sync-sphere-desktop/id6472360923"><img src="assets/mac_store_download.svg" width="200px" /></a></div>


## Publishing

### iOS
iOS is published to the iOS App Store on release via fastlane by creating a tag (release/x.y.z)

### Android
Android is published to the Google Play Store on release via fastlane by creating a tag (release/x.y.z)

### Mac Desktop
Mac is published to the Mac App Store on release by creating a tag (release/x.y.z)

## Useful

Fastlane - `bundle exec fastlane ios build`

iOS -
[Fastlane](https://medium.com/revelo-tech/setting-up-automatic-ios-release-with-fastlane-and-match-on-ci-cd-server-16c3f1d79bc5)
1. Github repo with certificates (MATCH_GIT_BASIC_AUTHORIZATION, MATCH_PASSWORD)
2. App Store Connect API Key, Issuer ID, and Key ID
3. `openssl base64 < path/to/key.p8 | tr -d '\n' | pbcopy`
4. echo -n your_github_username:your_personal_access_token | base64 for `MATCH_GIT_BASIC_AUTHORIZATION`

Android -
[Fastlane](https://docs.fastlane.tools/actions/upload_to_play_store/)
[CI](https://proandroiddev.com/how-to-securely-build-and-sign-your-android-app-with-github-actions-ad5323452ce)
1. Keystore, alias, alias password, keystore password
2. Google Play Services JSON account credentials

Mac -
[Native distributions & local execution](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md#native-distributions--local-execution)
[Signing and notarizing distributions for macOS](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md)
[Publishing to Github Actions](https://www.marcogomiero.com/posts/2024/kmp-ci-macos-appstore/)
[Publishing to Mac App Store](https://www.marcogomiero.com/posts/2024/compose-macos-app-store/)

```
./gradlew clean spotlessApply detektAll :androidApp:build :desktopApp:packageDistributionForCurrentOS && bundle exec fastlane ios build
```

## On A Fresh Clone
1. Add this to your home `gradle.properties` (~/.gradle/gradle.properties) for a debug environment on Desktop. iOS and Android are already configured for debug.
   1. ```groovy
        systemProp.syncSphereDebug=true
      ```

2. Add `.env.default` under `fastlane` based on the `.env.default.sample` for building and using fastlane locally (only needed for deploying)

3. Add `keystore.jks` under `androidApp` with a `key.properties` file that looks like:
   1. ```
        keyAlias=<alias>
        keyPassword=<key password>
        storePassword=<stote password>
        storeFile=keystore.jks
      ```

## Mockups
https://studio.app-mockup.com/
