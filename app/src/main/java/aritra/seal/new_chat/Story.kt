package aritra.seal.new_chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    var storyUrl: String = " ",
    var profilePicUrl: String = " ",
    var username: String = " ",
    var timestamp: Long = 0L,
    var userId: String = " "
) : Parcelable
