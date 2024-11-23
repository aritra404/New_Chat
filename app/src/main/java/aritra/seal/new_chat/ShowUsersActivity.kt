package aritra.seal.new_chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ShowUsersActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(aritra.seal.new_chat.R.layout.activity_users)

        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(userList){ selectedUser ->
            // Handle user click and navigate to ChatScreen
            val intent = Intent(this, ChatScreen::class.java)
            intent.putExtra("RECEIVER_USER_ID", selectedUser.uid) // Pass selected user ID
            intent.putExtra("RECEIVER_USERNAME", selectedUser.username)
            intent.putExtra("RECEIVER_PROFILE_IMAGE", selectedUser.imageUri)
            startActivity(intent)
        }
        usersRecyclerView.adapter = userAdapter

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("users")

        // Fetch all users from Firebase
        fetchUsersFromDatabase()
    }

    private fun fetchUsersFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear() // Clear the list before adding new data
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged() // Notify adapter about data changes
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch users", error.toException())
            }
        })
    }
}
