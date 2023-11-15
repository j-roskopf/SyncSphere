# Sync Sphere

Work In Progress

## Useful

After running `./cleanup` script, regenerate framework files via `./gradlew :shared:generateDummyFramework` and execute `pod install` in iosApp folder

```
./gradlew clean && ./cleanup.sh && ./gradlew :shared:generateDummyFramework && cd iosApp && pod install && cd .. && ./gradlew :shared:podInstall
```

Fastlane - `bundle exec fastlane ios build`

Building archive - open iOS workspace via `open iosApp.xcworkspace`

[Fastlane](https://medium.com/revelo-tech/setting-up-automatic-ios-release-with-fastlane-and-match-on-ci-cd-server-16c3f1d79bc5)
