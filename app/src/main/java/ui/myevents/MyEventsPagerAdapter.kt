package ui.myevents

import Models.EventWithRSVP
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyEventsPagerAdapter(
    fragment: Fragment,
    private val onCancelRSVP: (String) -> Unit
) : FragmentStateAdapter(fragment) {

    private var upcomingEvents = listOf<EventWithRSVP>()
    private var pastEvents = listOf<EventWithRSVP>()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EventsTabFragment.newInstance(isUpcoming = true, onCancelRSVP)
            1 -> EventsTabFragment.newInstance(isUpcoming = false, onCancelRSVP)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    fun updateEvents(upcoming: List<EventWithRSVP>, past: List<EventWithRSVP>) {
        upcomingEvents = upcoming
        pastEvents = past
        notifyDataSetChanged()
    }

    fun getUpcomingEvents() = upcomingEvents
    fun getPastEvents() = pastEvents
}