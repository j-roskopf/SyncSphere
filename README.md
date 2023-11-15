<div align="center">
  <img style="border-radius: 50%" src="./androidApp/src/androidMain/ic_launcher-playstore.png" width="100px">
  <h1>Sync Sphere</h1>
</div>

Sync Sphere is a work in progress

## Download 📦

Sync Sphere is available for Android and iOS.

<div align="center"><a href="https://apps.apple.com/us/app/sync-sphere/id6471382890"><img src="./assets/app_store_download.svg" width="200px"/></a></div>
<div align="center"><a href="https://play.google.com/store/apps/details?id=com.joetr.sync.sphere"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="230px"/></a></div>

## Useful

After running `./cleanup` script, regenerate framework files via `./gradlew :shared:generateDummyFramework` and execute `pod install` in iosApp folder

Fastlane - `bundle exec fastlane ios build`

Building archive - open iOS workspace via `open iosApp.xcworkspace`

[Fastlane](https://medium.com/revelo-tech/setting-up-automatic-ios-release-with-fastlane-and-match-on-ci-cd-server-16c3f1d79bc5)
