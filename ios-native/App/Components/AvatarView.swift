import SwiftUI
import ChatterCore

/// Shared avatar (RN: src/components/avatar.tsx): initials on a colour that is stable per name.
struct AvatarView: View {
    let name: String
    var size: CGFloat = 48
    var group = false

    var body: some View {
        Text(initials(name))
            .font(.system(size: size * CGFloat(avatarFontScale(group: group)), weight: .bold))
            .foregroundColor(.white)
            .frame(width: size, height: size)
            .background(Color(hex: avatarColour(name)))
            .clipShape(Circle())
            .accessibilityHidden(true)
    }
}
