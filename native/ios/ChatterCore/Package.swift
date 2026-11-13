// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "ChatterCore",
    platforms: [.iOS(.v17), .macOS(.v14)],
    products: [.library(name: "ChatterCore", targets: ["ChatterCore"])],
    targets: [
        .target(name: "ChatterCore"),
        .testTarget(name: "ChatterCoreTests", dependencies: ["ChatterCore"]),
    ]
)
