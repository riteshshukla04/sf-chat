import SwiftUI
import ChatterCore

/// SwiftUI face of ChatStore (RN: useAppState). Views read `state`, call `store` actions.
final class AppModel: ObservableObject {
    let store: ChatStore
    let crash: CrashReporter
    @Published private(set) var state: AppState
    /// Navigation: pushed chat ids (route chat/<id>) and the new-chat modal.
    @Published var path: [String] = []
    @Published var showingNewChat = false

    private var unsubscribe: (() -> Void)?

    init(store: ChatStore = ChatStore(), crash: CrashReporter = CrashReporting.make()) {
        self.store = store
        self.crash = crash
        self.state = store.state
        unsubscribe = store.subscribe { [weak self] in
            guard let self else { return }
            self.state = self.store.state
        }
    }

    deinit { unsubscribe?() }

    func chat(id: String) -> Chat? { state.chats.first { $0.id == id } }
    var unread: Int { totalUnread(state.chats) }

    func openChat(_ id: String) { path.append(id) }
}
