package aritra.seal.new_chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var activeChatsRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: ArrayList<User>
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var storiesRecyclerView: RecyclerView
    private lateinit var storyAdapter: Story_adapter
    private lateinit var storyList: ArrayList<Story>

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var pickImageButton: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var imageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            imageUri = uri
            uploadStoryToFirebase(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Set up the listener for refresh action
        swipeRefreshLayout.setOnRefreshListener {
            fetchStories()
            fetchUsersWithConversations()
        }

        pickImageButton = findViewById(R.id.pickImageButton)
        pickImageButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }

        // Initialize RecyclerView for stories
        storiesRecyclerView = findViewById(R.id.storiesRecyclerView)
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        storyList = ArrayList()
        storyAdapter = Story_adapter(this, storyList) { selectedStory ->
            val selectedUserId = selectedStory.userId
            val userStories = storyList.filter { it.userId == selectedUserId }
            val intent = Intent(this, FullScreenStoryActivity::class.java)
            intent.putParcelableArrayListExtra("STORY_LIST", ArrayList(userStories))
            startActivity(intent)
        }
        storiesRecyclerView.adapter = storyAdapter

        fetchStories()

        // Initialize RecyclerView for active chats
        activeChatsRecyclerView = findViewById(R.id.usersRecyclerView)
        activeChatsRecyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(userList) { selectedUser ->
            val intent = Intent(this, ChatScreen::class.java)
            intent.putExtra("RECEIVER_USER_ID", selectedUser.uid)
            intent.putExtra("RECEIVER_USERNAME", selectedUser.username)
            intent.putExtra("RECEIVER_PROFILE_IMAGE", selectedUser.imageUri)
            startActivity(intent)
        }
        activeChatsRecyclerView.adapter = userAdapter

        fetchUsersWithConversations()

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.message -> {
                    startActivity(Intent(this, ShowUsersActivity::class.java))
                    true
                }
                R.id.settings -> true
                else -> false
            }
        }
    }

    private fun fetchUsersWithConversations() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val messagesRef = FirebaseDatabase.getInstance().getReference("messages")
        val userIds = mutableSetOf<String>()

        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (conversationSnapshot in snapshot.children) {
                    for (messageSnapshot in conversationSnapshot.children) {
                        val message = messageSnapshot.getValue(Message::class.java) ?: continue
                        if (message.senderId == currentUserId) {
                            userIds.add(message.receiverId)
                        } else if (message.receiverId == currentUserId) {
                            userIds.add(message.senderId)
                        }
                    }
                }
                fetchUsersDetails(userIds.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch messages", error.toException())
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun fetchUsersDetails(userIds: List<String>) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && userIds.contains(user.uid)) {
                        userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch user details", error.toException())
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun uploadStoryToFirebase(storyUri: Uri) {
        val user = auth.currentUser ?: return

        val storyRef = storageReference.child("stories/${System.currentTimeMillis()}.jpg")
        storyRef.putFile(storyUri).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val storyUrl = uri.toString()
                val profilePicUrl = user.photoUrl?.toString() ?: "default_profile_pic_url"
                val username = user.displayName ?: "Anonymous"
                val userId = user.uid
                val timestamp = System.currentTimeMillis()
                val story = Story(storyUrl, profilePicUrl, username, timestamp, userId)

                saveStoryToDatabase(story)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to upload story: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStories() {
        val storiesRef = FirebaseDatabase.getInstance().getReference("stories")
        storiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storyList.clear()
                val uniqueUsers = mutableMapOf<String, Story>() // To hold unique users and one story for display

                for (userStorySnapshot in snapshot.children) {
                    val userId = userStorySnapshot.key ?: continue

                    val userStories = userStorySnapshot.children.mapNotNull { storySnapshot ->
                        storySnapshot.getValue(Story::class.java)
                    }

                    if (userStories.isNotEmpty()) {
                        // Store all stories of the user for later use
                        storyList.addAll(userStories)

                        // Add only one story for this user to show in the RecyclerView
                        uniqueUsers[userId] = userStories.first()
                    }
                }

                // Update the adapter with unique users
                val displayList = uniqueUsers.values.toList()
                storyAdapter = Story_adapter(this@MainActivity, displayList) { selectedStory ->
                    // When a user clicks, filter and pass their stories to FullScreenStoryActivity
                    val selectedUserId = selectedStory.userId
                    val userStories = storyList.filter { it.userId == selectedUserId }
                    val intent = Intent(this@MainActivity, FullScreenStoryActivity::class.java)
                    intent.putParcelableArrayListExtra("STORY_LIST", ArrayList(userStories))
                    startActivity(intent)
                }
                storiesRecyclerView.adapter = storyAdapter
                storyAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch stories", error.toException())
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }



    private fun saveStoryToDatabase(story: Story) {
        val storiesRef = database.child("stories").child(story.userId).push()
        storiesRef.setValue(story).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Story uploaded successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save story to database.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
