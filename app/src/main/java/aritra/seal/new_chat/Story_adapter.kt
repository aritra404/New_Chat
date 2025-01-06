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

class Story_adapter(
    private val context: Context,
    private val storyList: List<Story>,
    private val onStoryClick: (Story) -> Unit
) : RecyclerView.Adapter<Story_adapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyImageView: ImageView = itemView.findViewById(R.id.story_image)
        val userNameTextView: TextView = itemView.findViewById(R.id.story_username)

        fun bind(story: Story) {
            Glide.with(context).load(story.profilePicUrl).into(storyImageView)
            userNameTextView.text = story.username




        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]
        holder.bind(story)

        holder.itemView.setOnClickListener {
            val userId = story.userId
            // Filter all stories of the selected user
            val userStories = storyList.filter { it.userId == userId }
            val intent = Intent(context, FullScreenStoryActivity::class.java)
            intent.putParcelableArrayListExtra("STORY_LIST", ArrayList(userStories))
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return storyList.size
    }
}
