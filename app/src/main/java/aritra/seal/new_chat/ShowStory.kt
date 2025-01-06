package aritra.seal.new_chat

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.abs

class FullScreenStoryActivity : AppCompatActivity() {
    private lateinit var storyImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var userStories: List<Story>
    private var currentIndex = 0
    private var storyTimer: CountDownTimer? = null
    private var touchXStart = 0f
    private var touchYStart = 0f
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_story)

        initializeViews()
        setupStories()
        setupTouchListeners()
    }

    private fun initializeViews() {
        storyImageView = findViewById(R.id.fullscreen_story_image)
        userNameTextView = findViewById(R.id.fullscreen_story_username)
        progressBar = findViewById(R.id.storyProgressBar)
    }

    private fun setupStories() {
        userStories = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("STORY_LIST", Story::class.java) ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("STORY_LIST") ?: emptyList()
        }

        if (userStories.isEmpty()) {
            Toast.makeText(this, "No stories to display", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Start from the specified position (first unseen story)
        currentIndex = intent.getIntExtra("START_POSITION", 0)
        showStory(currentIndex)
    }

    private fun setupTouchListeners() {
        storyImageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchXStart = event.x
                    touchYStart = event.y
                    pauseStory()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    handleTouchEvent(event)
                    resumeStory()
                    true
                }
                else -> false
            }
        }
    }

    private fun showStory(index: Int) {
        if (index !in userStories.indices) {
            finish()
            return
        }


        val story = userStories[index]

        // Load story content based on type
        when (story.type) {
            StoryType.IMAGE -> loadImage(story)
            StoryType.VIDEO -> loadVideo(story)
        }

        userNameTextView.text = story.username
        markStoryAsViewed(story)
        startStoryTimer(story.duration)
    }

    private fun loadImage(story: Story) {
        Glide.with(this)
            .load(story.storyUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(storyImageView)
    }

    private fun loadVideo(story: Story) {
        // Implement video loading logic here
    }

    private fun startStoryTimer(duration: Long) {
        storyTimer?.cancel()
        progressBar.progress = 0
        progressBar.max = duration.toInt()

        storyTimer = object : CountDownTimer(duration, 50) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    val progress = (duration - millisUntilFinished).toInt()
                    progressBar.progress = progress
                }
            }

            override fun onFinish() {
                showNextStory()
            }
        }.start()
    }

    private fun pauseStory() {
        isPaused = true
    }

    private fun resumeStory() {
        isPaused = false
    }

    private fun markStoryAsViewed(story: Story) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (!story.viewedBy.contains(currentUserId)) {
            story.viewedBy.add(currentUserId)

            // Update viewed status in Firebase
            FirebaseDatabase.getInstance().reference
                .child("stories")
                .child(story.userId)
                .child(story.timestamp.toString())
                .child("viewedBy")
                .setValue(story.viewedBy)
        }
    }

    private fun handleTouchEvent(event: MotionEvent) {
        val deltaX = event.x - touchXStart
        val deltaY = event.y - touchYStart

        when {
            abs(deltaX) > abs(deltaY) && abs(deltaX) > 100 -> {
                // Horizontal swipe
                if (deltaX > 0) showPreviousStory() else showNextStory()
            }
            abs(deltaY) > abs(deltaX) && abs(deltaY) > 100 -> {
                // Vertical swipe
                finish()
            }
            else -> {
                // Tap
                handleTapNavigation(event)
            }
        }
    }

    private fun handleTapNavigation(event: MotionEvent) {
        val screenWidth = resources.displayMetrics.widthPixels
        when {
            event.x < screenWidth / 3 -> showPreviousStory()
            event.x > screenWidth * 2 / 3 -> showNextStory()
        }
    }

    private fun showNextStory() {
        if (currentIndex < userStories.size - 1) {
            currentIndex++
            showStory(currentIndex)
        } else {
            finish()
        }
    }

    private fun showPreviousStory() {
        if (currentIndex > 0) {
            currentIndex--
            showStory(currentIndex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        storyTimer?.cancel()
    }
}
