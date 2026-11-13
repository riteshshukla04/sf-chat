import XCTest
@testable import ChatterCore

/// Fake timers: nothing fires until `advance` moves the clock.
final class FakeScheduler: Scheduler {
    private(set) var now = 0
    private var pending: [(at: Int, order: Int, block: () -> Void)] = []
    private var order = 0
    func schedule(afterMillis: Int, _ block: @escaping () -> Void) {
        order += 1
        pending.append((now + afterMillis, order, block))
    }
    func advance(by ms: Int) {
        let target = now + ms
        while let next = pending.filter({ $0.at <= target }).min(by: { ($0.at, $0.order) < ($1.at, $1.order) }) {
            pending.removeAll { $0.order == next.order }
            now = next.at
            next.block()
        }
        now = target
    }
    var pendingCount: Int { pending.count }
}

final class ChatStoreTests: XCTestCase {
    let T0: Millis = 1_800_000_000_000
    var storage: InMemoryStorage!
    var scheduler: FakeScheduler!
    var clockValue: Millis!
    var store: ChatStore!

    override func setUp() {
        storage = InMemoryStorage()
        scheduler = FakeScheduler()
        clockValue = T0
        store = makeStore()
    }

    func makeStore() -> ChatStore {
        ChatStore(storage: storage, scheduler: scheduler, clock: { [unowned self] in self.clockValue + Millis(self.scheduler.now) })
    }

    func testHydrateWithNothingSavedUsesTheSeedAndSavesIt() throws {
        XCTAssertFalse(store.state.hydrated)
        store.hydrate(now: T0)
        XCTAssertTrue(store.state.hydrated)
        XCTAssertEqual(store.state.chats.map(\.id), ["family", "asha", "weekend", "ben", "flat", "chen"])
        XCTAssertEqual(store.state.calls.count, 4)
        XCTAssertEqual(store.state.contacts.count, 8)
        XCTAssertEqual(store.state.profile, Profile(name: "You", about: "Available"))
        XCTAssertNotNil(storage.getItem("chatter:v1"))
    }

    func testSavedJSONHasTheSameShapeAsTheRNApp() throws {
        store.hydrate(now: T0)
        let json = try JSONSerialization.jsonObject(with: storage.getItem("chatter:v1")!.data(using: .utf8)!) as! [String: Any]
        XCTAssertEqual(Set(json.keys), ["profile", "contacts", "chats", "calls"])
        let asha = (json["contacts"] as! [String: Any])["asha"] as! [String: Any]
        XCTAssertTrue(asha["lastSeenAt"] is NSNull, "online contacts are saved as null, not omitted")
        let chat = (json["chats"] as! [[String: Any]])[0]
        XCTAssertEqual(chat["kind"] as? String, "group")
        XCTAssertEqual((chat["messages"] as! [[String: Any]])[0]["status"] as? String, "read")
        XCTAssertEqual((json["calls"] as! [[String: Any]])[0]["direction"] as? String, "missed")
    }

    func testHydrateLoadsWhatWasSavedAndReadsRNJSON() {
        let rn = """
        {"profile":{"name":"Rita","about":"Hi"},"contacts":{"asha":{"id":"asha","name":"Asha Rao","about":"x","lastSeenAt":null}},
         "chats":[{"id":"c1","kind":"direct","title":"Asha Rao","members":["asha"],"messages":[{"id":"m1","from":"asha","text":"yo","sentAt":1700000000000,"status":"read"}],"unread":1,"pinned":false,"muted":false,"createdAt":1690000000000}],
         "calls":[{"id":"k","contactId":"asha","at":1700000000000,"direction":"incoming","video":true}]}
        """
        storage.setItem("chatter:v1", rn)
        store.hydrate(now: T0)
        XCTAssertEqual(store.state.profile.name, "Rita")
        XCTAssertEqual(store.state.chats.map(\.id), ["c1"])
        XCTAssertNil(store.state.contacts["asha"]!.lastSeenAt)
        XCTAssertEqual(store.state.calls[0].video, true)
        XCTAssertEqual(storage.getItem("chatter:v1"), rn, "loading saved data does not rewrite it")
    }

    func testCorruptSavedDataFallsBackToTheSeed() {
        storage.setItem("chatter:v1", "{not json")
        store.hydrate(now: T0)
        XCTAssertEqual(store.state.chats.count, 6)
    }

    func testSendFlowMovesTicksThenAnswersInADirectChat() {
        store.hydrate(now: T0)
        let before = store.chat(id: "ben")!.messages.count   // 2
        store.sendMessage("ben", "  Hello  ")
        var sent = store.chat(id: "ben")!.messages.last!
        XCTAssertEqual(sent.text, "Hello")
        XCTAssertEqual(sent.from, "me")
        XCTAssertEqual(sent.status, .sending)
        XCTAssertEqual(store.chat(id: "ben")!.messages.count, before + 1)

        scheduler.advance(by: 399)
        XCTAssertEqual(store.chat(id: "ben")!.messages.last!.status, .sending)
        scheduler.advance(by: 1)    // 400
        XCTAssertEqual(store.chat(id: "ben")!.messages.last!.status, .sent)
        scheduler.advance(by: 800)  // 1200
        XCTAssertEqual(store.chat(id: "ben")!.messages.last!.status, .delivered)
        scheduler.advance(by: 1400) // 2600
        XCTAssertEqual(store.chat(id: "ben")!.messages.last!.status, .read)

        scheduler.advance(by: 1599)
        XCTAssertEqual(store.chat(id: "ben")!.messages.count, before + 1, "no reply before 4200ms")
        scheduler.advance(by: 1)    // 4200
        let messages = store.chat(id: "ben")!.messages
        XCTAssertEqual(messages.count, before + 2)
        let reply = messages.last!
        XCTAssertEqual(reply.from, "ben")
        XCTAssertEqual(reply.status, .read)
        XCTAssertEqual(reply.text, replies[(before + 1) % replies.count])
        XCTAssertTrue(reply.id.hasPrefix("r-"))
        sent = messages[before]
        XCTAssertEqual(sent.status, .read)
        // Persisted along the way.
        XCTAssertTrue(storage.getItem("chatter:v1")!.contains(reply.text))
    }

    func testGroupChatsDoNotAnswerAndSendingClearsUnread() {
        store.hydrate(now: T0)
        XCTAssertEqual(store.chat(id: "family")!.unread, 1)
        store.sendMessage("family", "hi all")
        XCTAssertEqual(store.chat(id: "family")!.unread, 0)
        scheduler.advance(by: 10_000)
        XCTAssertEqual(store.chat(id: "family")!.messages.count, 5)
        XCTAssertEqual(store.chat(id: "family")!.messages.last!.status, .read)
    }

    func testSendToUnknownChatDoesNothing() {
        store.hydrate(now: T0)
        store.sendMessage("nope", "x")
        XCTAssertEqual(scheduler.pendingCount, 0)
    }

    func testStartChatReusesTheDirectChatOrCreatesOne() {
        store.hydrate(now: T0)
        XCTAssertEqual(store.startChat("asha"), "asha")
        XCTAssertEqual(store.state.chats.count, 6)
        let id = store.startChat("dana", now: T0)   // no direct chat with Dana in the seed
        XCTAssertEqual(store.state.chats.count, 7)
        XCTAssertEqual(store.state.chats[0].id, id)
        XCTAssertEqual(store.state.chats[0].title, "Dana Silva")
        XCTAssertEqual(store.state.chats[0].members, ["dana"])
        XCTAssertEqual(store.startChat("dana"), id)
        XCTAssertEqual(store.state.chats.count, 7)
    }

    func testTogglePinAndMarkRead() {
        store.hydrate(now: T0)
        XCTAssertFalse(store.chat(id: "ben")!.pinned)
        store.togglePin("ben")
        XCTAssertTrue(store.chat(id: "ben")!.pinned)
        store.togglePin("ben")
        XCTAssertFalse(store.chat(id: "ben")!.pinned)
        XCTAssertEqual(totalUnread(store.state.chats), 3) // family 1 + asha 2; flat is muted
        store.markRead("asha")
        XCTAssertEqual(store.chat(id: "asha")!.unread, 0)
        XCTAssertEqual(totalUnread(store.state.chats), 1)
    }

    func testUpdateProfileIsSaved() {
        store.hydrate(now: T0)
        store.updateProfile(name: "Rita")
        XCTAssertEqual(store.state.profile, Profile(name: "Rita", about: "Available"))
        let second = makeStore()
        second.hydrate(now: T0)
        XCTAssertEqual(second.state.profile.name, "Rita")
    }

    func testResetChatsBringsTheSeedBack() {
        store.hydrate(now: T0)
        store.updateProfile(name: "Rita")
        store.sendMessage("ben", "x")
        store.togglePin("chen")
        store.resetChats(now: T0)
        XCTAssertEqual(store.chat(id: "ben")!.messages.count, 2)
        XCTAssertFalse(store.chat(id: "chen")!.pinned)
        XCTAssertEqual(store.state.profile.name, "Rita", "profile is kept, as in the RN app")
        XCTAssertTrue(store.state.hydrated)
    }

    func testSubscribersAreNotifiedUntilTheyUnsubscribe() {
        var calls = 0
        let unsubscribe = store.subscribe { calls += 1 }
        store.hydrate(now: T0)
        XCTAssertEqual(calls, 1)
        unsubscribe()
        store.togglePin("ben")
        XCTAssertEqual(calls, 1)
    }

    func testNothingIsSavedBeforeHydration() {
        store.updateProfile(name: "x")
        XCTAssertNil(storage.getItem("chatter:v1"))
    }
}
