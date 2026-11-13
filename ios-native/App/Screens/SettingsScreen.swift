import SwiftUI
import ChatterCore

/// Settings tab (RN: src/app/(tabs)/settings.tsx): profile header (tap to edit via updateProfile),
/// info rows incl. build info, and Reset chats with a confirmation (resetChats).
struct SettingsScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c
    @State private var confirmReset = false
    @State private var editing = false

    var body: some View {
        let profile = model.state.profile
        let rows = settingsRows(info: getBuildInfo(), crashReportingEnabled: model.crash.enabled)
        ScrollView {
            VStack(spacing: 0) {
                Text("Settings").font(.system(size: 26, weight: .bold)).foregroundColor(c.text)
                    .accessibilityAddTraits(.isHeader)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, CGFloat(Spacing.lg)).padding(.vertical, CGFloat(Spacing.sm))
                Button { editing = true } label: {
                    HStack(spacing: CGFloat(Spacing.lg)) {
                        AvatarView(name: profile.name, size: 64)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(profile.name).font(.system(size: 20, weight: .semibold)).foregroundColor(c.text)
                            Text(profile.about).font(.system(size: 15)).foregroundColor(c.muted)
                        }
                        Spacer(minLength: 0)
                    }
                    .padding(CGFloat(Spacing.lg))
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Edit profile, \(profile.name)")
                .accessibilityIdentifier("edit-profile")
                divider
                ForEach(rows, id: \.label) { row in
                    SettingsRow(row: row)
                    divider
                }
                Button { confirmReset = true } label: {
                    Text("Reset chats").fontWeight(.semibold).foregroundColor(c.danger)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(CGFloat(Spacing.lg)).contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("reset-chats")
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(c.surface)
        .alert(ResetChatsAlert.title, isPresented: $confirmReset) {
            Button(ResetChatsAlert.cancel, role: .cancel) {}
            Button(ResetChatsAlert.confirm, role: .destructive) { model.store.resetChats() }
        } message: { Text(ResetChatsAlert.message) }
        .sheet(isPresented: $editing) {
            EditProfileSheet(draft: ProfileDraft(profile)) { saved in
                model.store.updateProfile(name: saved.name, about: saved.about)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.settingsScreen)
    }

    private var divider: some View { Rectangle().fill(c.border).frame(height: 0.5) }
}

private struct SettingsRow: View {
    let row: SettingsRowModel
    @Environment(\.colors) private var c

    var body: some View {
        HStack(spacing: CGFloat(Spacing.lg)) {
            Text(row.label).font(.system(size: 16)).foregroundColor(c.text)
            Spacer(minLength: 0)
            Text(row.value).font(.system(size: 14)).foregroundColor(c.muted).lineLimit(1).multilineTextAlignment(.trailing)
        }
        .padding(.horizontal, CGFloat(Spacing.lg)).padding(.vertical, CGFloat(Spacing.md))
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(row.testID ?? "settings-row-\(row.label)")
    }
}

private struct EditProfileSheet: View {
    @Environment(\.dismiss) private var dismiss
    @State var draft: ProfileDraft
    let onSave: (ProfileDraft) -> Void

    var body: some View {
        NavigationStack {
            Form {
                TextField("Name", text: $draft.name).accessibilityIdentifier("profile-name-input")
                TextField("About", text: $draft.about).accessibilityIdentifier("profile-about-input")
            }
            .navigationTitle("Edit profile")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { onSave(draft.trimmed); dismiss() }
                        .disabled(!draft.isValid).accessibilityIdentifier("profile-save")
                }
            }
        }
    }
}
