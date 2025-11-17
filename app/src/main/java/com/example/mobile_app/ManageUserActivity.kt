package com.example.mobile_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Models.User
import com.example.mobile_app.databinding.ActivityManageUsersBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageUsersBinding
    private val users = mutableListOf<User>()
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UsersAdapter(users,
            onToggleRoleClick = { user ->
                toggleUserRole(user)
            }
        )

        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        binding.btnBackManageUsers.setOnClickListener {
            finish()
        }

        loadUsers()
    }

    private fun loadUsers() {
        val db = Firebase.firestore

        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                users.clear()

                if (snapshot == null || snapshot.isEmpty) {
                    binding.tvUsersEmptyState.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }

                binding.tvUsersEmptyState.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load users: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.tvUsersEmptyState.visibility = View.VISIBLE
            }
    }

    private fun toggleUserRole(user: User) {
        val db = Firebase.firestore

        val newRole = if (user.role == "admin") "student" else "admin"

        // Update in Firestore using uid as document id
        db.collection("users")
            .document(user.uid)
            .update("role", newRole)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated ${user.name} to $newRole", Toast.LENGTH_SHORT).show()

                // Update locally so UI reflects change
                val index = users.indexOfFirst { it.uid == user.uid }
                if (index != -1) {
                    users[index] = user.copy(role = newRole)
                    adapter.notifyItemChanged(index)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update role: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// -----------------------------------
// Adapter for users list
// -----------------------------------

class UsersAdapter(
    private val users: List<User>,
    private val onToggleRoleClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], onToggleRoleClick)
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        private val tvFaculty: TextView = itemView.findViewById(R.id.tvUserFaculty)
        private val tvRole: TextView = itemView.findViewById(R.id.tvUserRole)
        private val btnToggleRole: Button = itemView.findViewById(R.id.btnToggleRole)

        fun bind(user: User, onToggleRoleClick: (User) -> Unit) {
            tvName.text = user.name
            tvEmail.text = user.email
            tvFaculty.text = user.faculty
            tvRole.text = "Role: ${user.role.ifEmpty { "student" }}"

            btnToggleRole.text = if (user.role == "admin") "Make student" else "Make admin"

            btnToggleRole.setOnClickListener {
                onToggleRoleClick(user)
            }
        }
    }
}
