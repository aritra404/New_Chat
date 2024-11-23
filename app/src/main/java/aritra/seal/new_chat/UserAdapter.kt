package aritra.seal.new_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserAdapter(private val UserList: List<User>, private val onUserClick: (User) -> Unit): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val profileImageView: ImageView = itemView.findViewById(R.id.userProfileImageView)
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
            val user = UserList[position]

            holder.usernameTextView.text = user.username
            if (user.imageUri != null) {
                Glide.with(holder.itemView.context)
                    .load(user.imageUri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(holder.profileImageView)
            } else {
                holder.profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
            }
            holder.itemView.setOnClickListener {
                onUserClick.invoke(user)
            }
        }

        override fun getItemCount(): Int {
            return UserList.size
        }

    }
