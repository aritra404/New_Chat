package aritra.seal.new_chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    var storyUrl: String = "",
    var profilePicUrl: String = "",
    var username: String = "",
    var timestamp: Long = 0L,
    var userId: String = "",
    var type: StoryType = StoryType.IMAGE,
    var duration: Long = 5000, // Default 5 seconds duration
    var viewedBy: MutableList<String> = mutableListOf(),
    var expiresAt: Long = timestamp + 24 * 60 * 60 * 1000 // 24 hours from creation
) : Parcelable {
    fun isViewedBy(userId: String): Boolean = viewedBy.contains(userId)
}

enum class StoryType {
    IMAGE, VIDEO
}
