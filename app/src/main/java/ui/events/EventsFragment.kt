package ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_app.databinding.FragmentEventsBinding
import viewmodels.EventsUiState
import viewmodels.EventsViewModel
import kotlinx.coroutines.launch

class EventsFragment : Fragment() {
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventsViewModel by viewModels()
    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter(
            onRSVPClick = { event, hasRSVPd ->
                if (hasRSVPd) {
                    viewModel.cancelRSVP(event)
                } else {
                    viewModel.rsvpToEvent(event)
                }
            },
            onEventClick = { event ->
                // Navigate to event details
                // findNavController().navigate(...)
            }
        )

        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventsAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadEvents()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is EventsUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                    }

                    is EventsUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false

                        if (state.events.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                            binding.eventsRecyclerView.visibility = View.GONE
                        } else {
                            binding.emptyState.visibility = View.GONE
                            binding.eventsRecyclerView.visibility = View.VISIBLE
                            eventsAdapter.submitList(state.events)
                        }
                    }

                    is EventsUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Observe RSVP success messages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rsvpSuccess.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearRSVPMessage()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}