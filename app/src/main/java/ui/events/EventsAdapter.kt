package ui.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobile_app.R
import com.example.mobile_app.databinding.ItemEventCardBinding
import Models.Event
import utils.DateTimeUtils
import viewmodels.EventWithStatus

class EventsAdapter(
    private val onRSVPClick: (Event, Boolean) -> Unit,
    private val onEventClick: (Event) -> Unit
) : ListAdapter<EventWithStatus, EventsAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemEventCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(eventWithStatus: EventWithStatus) {
            val event = eventWithStatus.event

            with(binding) {
                // Set event data
                eventTitle.text = event.title
                eventDate.text = event.datetime?.toDate()?.time?.let { DateTimeUtils.formatEventDate(it) }
                eventVenue.text = event.venue
                eventCapacity.text = "${event.currentAttendees}/${event.maxAttendees} attending"

                // Load event image
                Glide.with(root.context)
                    .load(event.posterIDUrl)
                    .placeholder(R.drawable.placeholder_event)
                    .error(R.drawable.placeholder_event)
                    .centerCrop()
                    .into(eventImage)

                // Handle RSVP state
                if (eventWithStatus.hasUserRSVPd) {
                    rsvpButton.visibility = View.GONE
                    attendingBadge.visibility = View.VISIBLE
                } else {
                    rsvpButton.visibility = View.VISIBLE
                    attendingBadge.visibility = View.GONE

                    // Disable if event is full
                    rsvpButton.isEnabled = !event.isFull() && !eventWithStatus.isProcessing
                    rsvpButton.text = if (event.isFull()) "Full" else "RSVP"
                }

                // Show loading state
                if (eventWithStatus.isProcessing) {
                    rsvpButton.text = "..."
                }

                // Click listeners
                rsvpButton.setOnClickListener {
                    onRSVPClick(event, eventWithStatus.hasUserRSVPd)
                }

                root.setOnClickListener {
                    onEventClick(event)
                }
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<EventWithStatus>() {
        override fun areItemsTheSame(
            oldItem: EventWithStatus,
            newItem: EventWithStatus
        ): Boolean {
            return oldItem.event.eventID == newItem.event.eventID
        }

        override fun areContentsTheSame(
            oldItem: EventWithStatus,
            newItem: EventWithStatus
        ): Boolean {
            return oldItem == newItem
        }
    }
}