package ui.myevents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobile_app.R
import com.example.mobile_app.databinding.ItemMyEventCardBinding
import Models.EventWithRSVP
import utils.DateTimeUtils

class MyEventsAdapter(
    private val onEventClick: (EventWithRSVP) -> Unit,
    private val onCancelClick: (String) -> Unit
) : ListAdapter<EventWithRSVP, MyEventsAdapter.MyEventViewHolder>(MyEventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyEventViewHolder {
        val binding = ItemMyEventCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyEventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyEventViewHolder(
        private val binding: ItemMyEventCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(eventWithRSVP: EventWithRSVP) {
            val event = eventWithRSVP.event

            with(binding) {
                // Set event data
                eventTitle.text = event.title
                eventDate.text = event.datetime?.toDate()?.time?.let { DateTimeUtils.formatEventDate(it) }
                eventVenue.text = event.venue

                // Load event image
                Glide.with(root.context)
                    .load(event.posterIDUrl)
                    .placeholder(R.drawable.placeholder_event)
                    .error(R.drawable.placeholder_event)
                    .centerCrop()
                    .into(eventImage)

                // Set status badge
                statusBadge.text = if (event.isUpcoming()) {
                    "RSVP'd"
                } else {
                    "Attended"
                }

                // Set badge background
                statusBadge.setBackgroundResource(
                    if (event.isUpcoming())
                        R.drawable.bg_rsvpd_badge
                    else
                        R.drawable.bg_attended_badge
                )

                // Click listeners
                root.setOnClickListener {
                    onEventClick(eventWithRSVP)
                }

                root.setOnLongClickListener {
                    if (event.isUpcoming()) {
                        // Show cancel option
                        onCancelClick(event.eventID)
                    }
                    true
                }
            }
        }
    }

    private class MyEventDiffCallback : DiffUtil.ItemCallback<EventWithRSVP>() {
        override fun areItemsTheSame(
            oldItem: EventWithRSVP,
            newItem: EventWithRSVP
        ): Boolean {
            return oldItem.event.eventID == newItem.event.eventID
        }

        override fun areContentsTheSame(
            oldItem: EventWithRSVP,
            newItem: EventWithRSVP
        ): Boolean {
            return oldItem == newItem
        }
    }
}