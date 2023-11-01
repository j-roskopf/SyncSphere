import SwiftUI
import Firebase

@main
struct SyncSphereiOSApplication: App {
    init() {
      FirebaseApp.configure()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
