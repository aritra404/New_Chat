package aritra.seal.new_chat

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class FullScreenStoryActivity : AppCompatActivity() {

    private lateinit var storyImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var stories: List<Story>
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_story)

        storyImageView = findViewById<ImageView>(R.id.fullscreen_story_image)
        userNameTextView = findViewById<TextView>(R.id.fullscreen_story_username)

        stories = intent.getParcelableArrayListExtra<Story>("STORY_LIST") ?: emptyList()

        // Show the first story
        showStory(currentIndex)

        // Set on click to move to next story
        storyImageView.setOnClickListener {
            if (currentIndex < stories.size - 1) {
                currentIndex++
                showStory(currentIndex)
            } else {
                finish() // End activity when all stories are viewed
            }
        }
    }

    private fun showStory(index: Int) {
        val story = stories[index]
        Glide.with(this).load(story.storyUrl).into(storyImageView)
        userNameTextView.text = story.username
    }
}
