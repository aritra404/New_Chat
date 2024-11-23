package aritra.seal.new_chat

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase

class ChatScreen : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: MutableList<Message>
    private lateinit var recyclerView: RecyclerView
    private lateinit var sendMessageButton: AppCompatImageButton
    private lateinit var messageInput: EditText
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // Retrieves current user ID
    private lateinit var receiverUserId: String
    private lateinit var sendfileButton: AppCompatImageButton
    private lateinit var username: TextView
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)

        receiverUserId = intent.getStringExtra("RECEIVER_USER_ID") ?: ""
        username = findViewById<TextView>(R.id.user_name_screen)
        profileImage = findViewById<ImageView>(R.id.user_profile_image)

        // Initialize views
        recyclerView = findViewById<RecyclerView>(R.id.chat_recycler_view)
        sendMessageButton = findViewById<AppCompatImageButton>(R.id.send_button)
        messageInput = findViewById<EditText>(R.id.message_input)
        sendfileButton = findViewById<AppCompatImageButton>(R.id.choose_file_button)

        // Set up RecyclerView
        messageList = mutableListOf()
        messageAdapter = MessageAdapter(messageList, currentUserId)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Send message on button click
        sendMessageButton.setOnClickListener {
            val messageText = messageInput.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            }
        }
        // Show username
        username.text = intent.getStringExtra("RECEIVER_USERNAME")

        val profileImageUrl = intent.getStringExtra("RECEIVER_PROFILE_IMAGE")

// Load the image using Glide
        Glide.with(this)
            .load(profileImageUrl)
            .into(profileImage)

        // Listen for new messages
        receiveMessages()
    }



    private fun sendMessage(messageText: String) {
        // Create Message object with no-argument constructor and set fields
        val message = Message().apply {
            senderId = currentUserId
            receiverId = receiverUserId
            text = messageText
            seen = false
        }

        // Define conversation path
        val conversationId = getConversationId(currentUserId, receiverUserId)
        val messageId =
            FirebaseDatabase.getInstance().getReference("messages/$conversationId").push().key

        // Save message to Firebase
        messageId?.let {
            FirebaseDatabase.getInstance().getReference("messages/$conversationId/$it")
                .setValue(message)
                .addOnSuccessListener {
                    Log.d("ChatScreen", "Message sent successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatScreen", "Failed to send message", e)
                }
        }
    }
    private fun receiveMessages() {
        // Define conversation path
        val conversationId = getConversationId(currentUserId, receiverUserId)
        val messageReference = FirebaseDatabase.getInstance().getReference("messages/$conversationId")

        // Listen for new messages
        messageReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    messageList.add(message)
                    messageAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.scrollToPosition(messageList.size - 1)

                    // Mark the message as seen when it is displayed
                    if (!message.seen && message.receiverId == currentUserId) {
                        snapshot.key?.let { markMessagesAsSeen(it) }
                    }
                }
                 else {
                    Log.e("ChatScreen", "Received a null message")
                }
            }


            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun getConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "$userId1$userId2"
        } else {
            "$userId2$userId1"
        }
    }
    private fun markMessagesAsSeen(messageId : String) {
        messageId.let {
            val conversationId = getConversationId(currentUserId, receiverUserId)
            val messageReference =
                FirebaseDatabase.getInstance().getReference("messages/$conversationId/$it")
                    .child("seen")
                    .setValue(true)
                    .addOnSuccessListener {
                        Log.d("ChatScreen", "Message seen successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatScreen", "Failed to mark message as seen", e)
                    }
        }
    }
}
