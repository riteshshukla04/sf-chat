// swift-tools-version:5.9
import PackageDescription

// ChatterCore holds everything that is not UI (theme tokens, domain logic, seed data, store,
// service stubs). It only needs Foundation, so `swift test` runs on Linux and macOS alike.
// The SwiftUI app target lives in App/ and is generated into an Xcode project by XcodeGen (project.yml).
let package = Package(
    name: "ChatterCore",
    platforms: [.iOS(.v16), .macOS(.v13)],
    products: [.library(name: "ChatterCore", targets: ["ChatterCore"])],
    targets: [
        .target(name: "ChatterCore"),
        .testTarget(name: "ChatterCoreTests", dependencies: ["ChatterCore"]),
    ]
)
