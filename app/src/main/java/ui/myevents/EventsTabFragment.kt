package ui.myevents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_app.databinding.FragmentEventsTabBinding
import viewmodels.MyEventsUiState
import viewmodels.MyEventsViewModel
import kotlinx.coroutines.launch

class EventsTabFragment : Fragment() {
    private var _binding: FragmentEventsTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyEventsViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: MyEventsAdapter
    private var isUpcoming: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isUpcoming = arguments?.getBoolean(ARG_IS_UPCOMING) ?: true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MyEventsAdapter(
            onEventClick = { eventWithRSVP ->
                // Navigate to event details
            },
            onCancelClick = { eventId ->
                // Show confirmation dialog
                showCancelDialog(eventId)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@EventsTabFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MyEventsUiState.Success -> {
                        val events = if (isUpcoming) {
                            state.upcomingEvents
                        } else {
                            state.pastEvents
                        }

                        if (events.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                            binding.emptyMessage.text = if (isUpcoming) {
                                "No upcoming events"
                            } else {
                                "No past events"
                            }
                        } else {
                            binding.emptyState.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            adapter.submitList(events)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showCancelDialog(eventId: String) {
        // Show AlertDialog to confirm cancellation
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel RSVP")
            .setMessage("Are you sure you want to cancel your RSVP?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.cancelRSVP(eventId)
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_IS_UPCOMING = "is_upcoming"
        private const val ARG_ON_CANCEL = "on_cancel"

        fun newInstance(isUpcoming: Boolean, onCancelRSVP: (String) -> Unit): EventsTabFragment {
            return EventsTabFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_UPCOMING, isUpcoming)
                }
            }
        }
    }
}