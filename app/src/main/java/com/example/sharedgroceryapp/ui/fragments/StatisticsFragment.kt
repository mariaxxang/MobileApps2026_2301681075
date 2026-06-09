package com.example.sharedgroceryapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.sharedgroceryapp.GroceryApplication
import com.example.sharedgroceryapp.R
import com.example.sharedgroceryapp.databinding.FragmentStatisticsBinding
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModel
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroceryViewModel by viewModels {
        GroceryViewModelFactory((requireActivity().application as GroceryApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Toolbar
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeListsFragment, R.id.searchFragment, R.id.statisticsFragment)
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.toolbar.navigationIcon?.setTint(requireContext().getColor(R.color.toolbar_content))

        // Observe ViewModel statistics flow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.statisticsState.collectLatest { state ->
                    binding.tvTotalListsCount.text = state.totalListsCount.toString()
                    binding.tvTotalItemsCount.text = state.totalItemsCount.toString()

                    binding.tvActiveCount.text = "${state.activeItemsCount} pending"
                    binding.tvCompletedCount.text = "${state.completedItemsCount} bought"

                    // Bind to custom chart views
                    binding.donutChartView.setData(state.activeItemsCount, state.completedItemsCount)
                    binding.categoryBarChartView.setData(state.categoryCounts)
                    binding.monthlyListsChartView.setData(state.listsPerMonth)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
