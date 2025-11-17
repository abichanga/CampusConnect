package com.example.mobile_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_app.databinding.ActivityAdminEventsBinding
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminEvent(
    val id: String = "",
    val title: String = "",
    val venue: String = "",
    val dateTimeMillis: Long = 0L
)

class AdminEventsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminEventsBinding
    private val events = mutableListOf<AdminEvent>()
    private lateinit var adapter: AdminEventsAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdminEventsAdapter(events)
        binding.rvAdminEvents.layoutManager = LinearLayoutManager(this)
        binding.rvAdminEvents.adapter = adapter

        binding.btnBackAdminEvents.setOnClickListener {
            finish()
        }


        loadEvents()
    }

    private fun loadEvents() {
        val db = Firebase.firestore

        db.collection("events")
            .orderBy("dateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = "Failed to load events."
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    events.clear()
                    adapter.notifyDataSetChanged()
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = "No events found."
                    return@addSnapshotListener
                }

                events.clear()

                for (doc in snapshot.documents) {
                    val title = doc.getString("title") ?: ""
                    val venue = doc.getString("venue") ?: ""
                    val dateTimeMillis = doc.getLong("dateTime") ?: 0L

                    events.add(
                        AdminEvent(
                            id = doc.id,
                            title = title,
                            venue = venue,
                            dateTimeMillis = dateTimeMillis
                        )
                    )
                }

                adapter.notifyDataSetChanged()
                binding.tvEmptyState.visibility = View.GONE
            }
    }
}

class AdminEventsAdapter(
    private val events: List<AdminEvent>
) : RecyclerView.Adapter<AdminEventsAdapter.AdminEventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminEventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_event, parent, false)
        return AdminEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminEventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    class AdminEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        private val tvVenue: TextView = itemView.findViewById(R.id.tvEventVenue)

        fun bind(event: AdminEvent) {
            tvTitle.text = event.title
            tvVenue.text = event.venue

            val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val text = if (event.dateTimeMillis > 0L) {
                fmt.format(Date(event.dateTimeMillis))
            } else {
                "No date/time"
            }
            tvDateTime.text = text
        }
    }
}
