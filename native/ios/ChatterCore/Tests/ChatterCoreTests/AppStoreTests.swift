import XCTest
@testable import ChatterCore

final class AppStoreTests: XCTestCase {
    private var storage = InMemoryStorage()
    private var scheduler = ManualScheduler()
    private var now: Int64 = 1_800_000_000_000

    private func makeStore() -> AppStore {
        AppStore(storage: storage, scheduler: scheduler, clock: { [unowned self] in self.now })
    }

    func testHydrateSeedsFirstLaunchAndSaves() throws {
        let store = makeStore()
        XCTAssertFalse(store.hydrated)
        store.hydrate()
        XCTAssertTrue(store.hydrated)
        XCTAssertEqual(store.chats.count, 6)
        XCTAssertEqual(store.contacts.count, 8)
        XCTAssertEqual(store.calls.count, 4)
        XCTAssertEqual(store.profile, Profile(name: "You", about: "Available"))
        let json = try JSONSerialization.jsonObject(with: XCTUnwrap(storage.getItem("chatter:v1"))) as! [String: Any]
        XCTAssertEqual(Set(json.keys), ["profile", "contacts", "chats", "calls"])
    }

    func testStoredJsonShape() throws {
        let store = makeStore()
        store.hydrate()
        let json = try JSONSerialization.jsonObject(with: XCTUnwrap(storage.getItem("chatter:v1"))) as! [String: Any]
        let contacts = json["contacts"] as! [String: [String: Any]]
        XCTAssertTrue(contacts["asha"]!["lastSeenAt"] is NSNull)
        XCTAssertNotNil(contacts["ben"]!["lastSeenAt"] as? NSNumber)
        let chats = json["chats"] as! [[String: Any]]
        XCTAssertEqual(chats[0]["kind"] as? String, "group")
        let first = (chats[0]["messages"] as! [[String: Any]])[0]
        XCTAssertNil(first["reactions"])
        XCTAssertEqual(first["status"] as? String, "read")
    }

    func testHydrateLoadsReactNativeJson() {
        let raw = """
        {"profile":{"name":"Zed","about":"Hi"},"contacts":{"a":{"id":"a","name":"Ann","about":"x","lastSeenAt":null}},
        "chats":[{"id":"c","kind":"direct","title":"Ann","members":["a"],"messages":[{"id":"m","from":"a","text":"yo","sentAt":5,"status":"sent","reactions":{"👍":["me"]}}],"unread":1,"pinned":false,"muted":false,"createdAt":1}],
        "calls":[{"id":"c1","contactId":"a","at":3,"direction":"missed","video":true}]}
        """
        storage.setItem("chatter:v1", Data(raw.utf8))
        let store = makeStore()
        store.hydrate()
        XCTAssertEqual(store.profile.name, "Zed")
        XCTAssertEqual(store.chats.map(\.id), ["c"])
        XCTAssertEqual(store.chats[0].messages[0].reactions, ["👍": ["me"]])
        XCTAssertNil(store.contacts["a"]!.lastSeenAt)
    }

    func testHydrateReloadsSavedData() {
        let first = makeStore()
        first.hydrate()
        first.togglePin("ben")
        first.updateProfile(name: "Zed")
        let second = makeStore()
        second.hydrate()
        XCTAssertTrue(second.chatById("ben")!.pinned)
        XCTAssertEqual(second.profile.name, "Zed")
        XCTAssertEqual(second.chats, first.chats)
    }

    func testMarkRead() {
        let store = makeStore()
        store.hydrate()
        XCTAssertEqual(store.chatById("asha")!.unread, 2)
        store.markRead("asha")
        XCTAssertEqual(store.chatById("asha")!.unread, 0)
    }

    func testSendAdvancesTicksAndGetsReply() {
        let store = makeStore()
        store.hydrate()
        let before = store.chatById("asha")!.messages.count // 4
        store.sendMessage("asha", "  hello ")
        var chat = store.chatById("asha")!
        XCTAssertEqual(chat.messages.count, before + 1)
        XCTAssertEqual(chat.messages.last!.text, "hello")
        XCTAssertEqual(chat.messages.last!.status, .sending)
        XCTAssertEqual(chat.unread, 0)
        scheduler.advance(by: 400)
        XCTAssertEqual(store.chatById("asha")!.messages.last!.status, .sent)
        scheduler.advance(by: 800)
        XCTAssertEqual(store.chatById("asha")!.messages.last!.status, .delivered)
        scheduler.advance(by: 1400)
        XCTAssertEqual(store.chatById("asha")!.messages.last!.status, .read)
        XCTAssertEqual(store.chatById("asha")!.messages.count, before + 1)
        scheduler.advance(by: 1600) // 4200 ms in total
        chat = store.chatById("asha")!
        XCTAssertEqual(chat.messages.count, before + 2)
        let reply = chat.messages.last!
        XCTAssertEqual(reply.from, "asha")
        XCTAssertEqual(reply.status, .read)
        XCTAssertEqual(reply.text, replies[(before + 1) % replies.count])
        XCTAssertTrue(reply.id.hasPrefix("r-"))
    }

    func testGroupChatGetsNoReply() {
        let store = makeStore()
        store.hydrate()
        let before = store.chatById("family")!.messages.count
        store.sendMessage("family", "hi all")
        scheduler.advance(by: 10_000)
        XCTAssertEqual(store.chatById("family")!.messages.count, before + 1)
        XCTAssertEqual(store.chatById("family")!.messages.last!.status, .read)
    }

    func testStartChatReusesExistingDirectChat() {
        let store = makeStore()
        store.hydrate()
        XCTAssertEqual(store.startChat("asha"), "asha")
        XCTAssertEqual(store.chats.count, 6)
    }

    func testStartChatCreatesNewDirectChatAtTop() {
        let store = makeStore()
        store.hydrate()
        let id = store.startChat("dana")
        XCTAssertEqual(store.chats.count, 7)
        XCTAssertEqual(store.chats[0].id, id)
        XCTAssertEqual(store.chats[0].title, "Dana Silva")
        XCTAssertEqual(store.chats[0].members, ["dana"])
        XCTAssertEqual(store.startChat("dana"), id)
    }

    func testReactToMessage() {
        let store = makeStore()
        store.hydrate()
        let mid = store.chatById("ben")!.messages[0].id
        store.reactToMessage("ben", mid, "👍")
        XCTAssertEqual(store.chatById("ben")!.messages[0].reactions, ["👍": ["me"]])
        store.reactToMessage("ben", mid, "👍")
        XCTAssertNil(store.chatById("ben")!.messages[0].reactions)
    }

    func testResetRestoresSeed() {
        let store = makeStore()
        store.hydrate()
        store.startChat("dana")
        store.togglePin("ben")
        store.resetChats()
        XCTAssertEqual(store.chats.count, 6)
        XCTAssertFalse(store.chatById("ben")!.pinned)
        XCTAssertNotNil(storage.getItem("chatter:v1"))
    }

    func testNothingPersistedBeforeHydrated() {
        let store = makeStore()
        store.updateProfile(name: "x")
        XCTAssertNil(storage.getItem("chatter:v1"))
    }
}

final class SeedTests: XCTestCase {
    func testSeedMatchesReactNative() {
        let seed = buildSeed(1_000_000_000_000)
        XCTAssertEqual(seed.chats.map(\.id), ["family", "asha", "weekend", "ben", "flat", "chen"])
        XCTAssertEqual(seed.chats[0].messages.map(\.id), ["seed-1", "seed-2", "seed-3", "seed-4"])
        XCTAssertEqual(seed.chats[3].messages[1].status, .delivered)
        XCTAssertEqual(seed.chats[2].messages, [])
        XCTAssertEqual(totalUnread(seed.chats), 3)
        XCTAssertEqual(seed.calls.map(\.id), ["c1", "c2", "c3", "c4"])
    }
}
