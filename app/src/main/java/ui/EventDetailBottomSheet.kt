package ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.mobile_app.R
import com.example.mobile_app.databinding.BottomSheetEventDetailBinding
import Models.Event
import utils.DateTimeUtils

class EventDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEventDetailBinding? = null
    private val binding get() = _binding!!

    private var event: Event? = null
    private var hasUserRSVPd: Boolean = false
    private var onRSVPClick: ((Event) -> Unit)? = null
    private var onCancelRSVP: ((Event) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        event?.let { displayEventDetails(it) }
    }

    private fun displayEventDetails(event: Event) {
        with(binding) {
            // Load event image
            Glide.with(requireContext())
                .load(event.posterIDUrl)
                .placeholder(R.drawable.placeholder_event)
                .error(R.drawable.placeholder_event)
                .centerCrop()
                .into(eventImage)

            // Set event details
            eventTitle.text = event.title
            eventDescription.text = event.description
            eventDate.text = event.datetime?.toDate()?.time?.let { DateTimeUtils.formatEventDate(it) }
            eventVenue.text = event.venue
            eventFaculty.text = event.faculty
            eventCapacity.text = "${event.currentAttendees}/${event.maxAttendees} attending"

            // Progress bar for capacity
            val progress = if (event.maxAttendees > 0) {
                (event.currentAttendees.toFloat() / event.maxAttendees.toFloat() * 100).toInt()
            } else 0
            capacityProgressBar.progress = progress

            // Handle RSVP button
            if (hasUserRSVPd) {
                rsvpButton.text = "Cancel RSVP"
                rsvpButton.setBackgroundColor(resources.getColor(R.color.error, null))
                rsvpButton.setOnClickListener {
                    onCancelRSVP?.invoke(event)
                    dismiss()
                }
            } else {
                rsvpButton.text = if (event.isFull()) "Event Full" else "RSVP Now"
                rsvpButton.isEnabled = !event.isFull()
                rsvpButton.setOnClickListener {
                    onRSVPClick?.invoke(event)
                    dismiss()
                }
            }

            // Share button
            shareButton.setOnClickListener {
                shareEvent(event)
            }

            // Close button
            closeButton.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun shareEvent(event: Event) {
        val formattedDate = event.datetime?.toDate()?.time?.let {
            DateTimeUtils.formatEventDate(it)
        } ?: "Date not available"

        val shareText = """
            Check out this event!
            
            ${event.title}
            📅 $formattedDate
            📍 ${event.venue}
            
            ${event.description}
        """.trimIndent()

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(android.content.Intent.createChooser(shareIntent, "Share event via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            event: Event,
            hasUserRSVPd: Boolean,
            onRSVPClick: (Event) -> Unit,
            onCancelRSVP: (Event) -> Unit
        ): EventDetailBottomSheet {
            return EventDetailBottomSheet().apply {
                this.event = event
                this.hasUserRSVPd = hasUserRSVPd
                this.onRSVPClick = onRSVPClick
                this.onCancelRSVP = onCancelRSVP
            }
        }
    }
}