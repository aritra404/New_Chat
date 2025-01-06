package aritra.seal.new_chat

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class Story_adapter(
    private val context: Context,
    private val storyList: List<Story>,
    private val onStoryClick: (Story) -> Unit
) : RecyclerView.Adapter<Story_adapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyImageView: ImageView = itemView.findViewById(R.id.story_image)
        val userNameTextView: TextView = itemView.findViewById(R.id.story_username)
        val storyRing: ImageView = itemView.findViewById(R.id.story_ring)

        fun bind(story: Story) {
            // Load profile picture
            Glide.with(context)
                .load(story.profilePicUrl)
                .circleCrop()
                .into(storyImageView)

            userNameTextView.text = story.username

            // Show colored ring for unviewed stories
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isViewed = currentUserId?.let { story.viewedBy.contains(it) } ?: false
            storyRing.visibility = if (isViewed) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]
        holder.bind(story)
        holder.itemView.setOnClickListener { onStoryClick(story) }
    }

    override fun getItemCount() = storyList.size
}