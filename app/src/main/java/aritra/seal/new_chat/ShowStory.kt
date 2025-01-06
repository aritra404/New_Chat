package aritra.seal.new_chat

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullScreenStoryActivity : AppCompatActivity() {

    private lateinit var storyImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userStories: List<Story>
    private var currentIndex = 0
    private var touchXStart = 0f
    private var touchXEnd = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_story)

        // Initialize views
        storyImageView = findViewById(R.id.fullscreen_story_image)
        userNameTextView = findViewById(R.id.fullscreen_story_username)

        // Retrieve stories from intent safely
        userStories = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("STORY_LIST", Story::class.java) ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("STORY_LIST") ?: emptyList()
        }

        if (userStories.isEmpty()) {
            Toast.makeText(this, "No stories to display.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show the first story
        showStory(currentIndex)

        // Handle swipe gestures
        storyImageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchXStart = event.x
                    true
                }
                MotionEvent.ACTION_UP -> {
                    touchXEnd = event.x
                    handleSwipeGesture()
                    true
                }
                else -> false
            }
        }
    }

    private fun showStory(index: Int) {
        if (index in userStories.indices) {
            val story = userStories[index]

            // Load story image
            Glide.with(this)
                .load(story.storyUrl)
                .into(storyImageView)

            // Update username
            userNameTextView.text = story.username

            // Add a fade-in animation (optional)
            storyImageView.alpha = 0f
            storyImageView.animate().alpha(1f).duration = 300
        } else {
            // Handle invalid index
            Toast.makeText(this, "Invalid story index.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showNextStory() {
        if (currentIndex < userStories.size - 1) {
            currentIndex++
            showStory(currentIndex)
        } else {
            Toast.makeText(this, "You’ve viewed all stories.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showPreviousStory() {
        if (currentIndex > 0) {
            currentIndex--
            showStory(currentIndex)
        } else {
            Toast.makeText(this, "This is the first story.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSwipeGesture() {
        val swipeThreshold = 100f // Minimum distance for a swipe gesture
        val deltaX = touchXEnd - touchXStart

        when {
            deltaX > swipeThreshold -> {
                // Swipe right (go to previous story)
                showPreviousStory()
            }
            deltaX < -swipeThreshold -> {
                // Swipe left (go to next story)
                showNextStory()
            }
        }
    }
}
