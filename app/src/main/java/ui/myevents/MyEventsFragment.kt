package ui.myevents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.example.mobile_app.databinding.FragmentMyEventsBinding
import viewmodels.MyEventsUiState
import viewmodels.MyEventsViewModel
import kotlinx.coroutines.launch

class MyEventsFragment : Fragment() {
    private var _binding: FragmentMyEventsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyEventsViewModel by viewModels()
    private lateinit var pagerAdapter: MyEventsPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        observeViewModel()
    }

    private fun setupViewPager() {
        pagerAdapter = MyEventsPagerAdapter(this) { eventId ->
            // Handle cancel RSVP
            viewModel.cancelRSVP(eventId)
        }

        binding.viewPager.adapter = pagerAdapter

        // Setup tabs
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Upcoming Events"
                1 -> "Past Events"
                else -> ""
            }
        }.attach()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MyEventsUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is MyEventsUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        pagerAdapter.updateEvents(
                            state.upcomingEvents,
                            state.pastEvents
                        )
                    }

                    is MyEventsUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }

                    is MyEventsUiState.Empty -> {
                        binding.progressBar.visibility = View.GONE
                        // Show empty state in adapter
                        pagerAdapter.updateEvents(emptyList(), emptyList())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}