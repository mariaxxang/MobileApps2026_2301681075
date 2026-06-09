package com.example.sharedgroceryapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedgroceryapp.GroceryApplication
import com.example.sharedgroceryapp.R
import com.example.sharedgroceryapp.data.local.Category
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.example.sharedgroceryapp.data.local.ShoppingList
import com.example.sharedgroceryapp.databinding.FragmentSearchBinding
import com.example.sharedgroceryapp.databinding.ItemSearchResultBinding
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModel
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroceryViewModel by viewModels {
        GroceryViewModelFactory((requireActivity().application as GroceryApplication).repository)
    }

    private lateinit var searchAdapter: SearchResultAdapter
    private var allLists: List<ShoppingList> = emptyList()

    private val PREFS_NAME = "search_prefs"
    private val KEY_RECENT_SEARCHES = "recent_searches"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadRecentSearches()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchResultAdapter(
            listsProvider = { allLists },
            onItemClick = { item ->
                // Save query to history
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    saveRecentSearch(query)
                }

                // Navigate to List Detail
                val sourceList = allLists.firstOrNull { it.id == item.listId }
                val listName = sourceList?.title ?: "Shopping List"
                val action = SearchFragmentDirections.actionSearchFragmentToListDetailFragment(
                    listId = item.listId.toLong(),
                    listName = listName
                )
                findNavController().navigate(action)
            }
        )
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupListeners() {
        binding.etSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = textView.text.toString().trim()
                if (query.isNotEmpty()) {
                    saveRecentSearch(query)
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {
                    binding.rvSearchResults.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.ivEmptyIcon.setImageResource(R.drawable.ic_search)
                    binding.tvEmptyTitle.text = "Search Groceries"
                    binding.tvEmptySubtitle.text = "Search for items across all lists"
                    loadRecentSearches()
                } else {
                    performSearch(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tvClearRecent.setOnClickListener {
            clearRecentSearches()
        }
    }

    private fun performSearch(query: String) {
        // Hide recent searches when active search is running
        binding.layoutRecentSearches.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchItems(query).collectLatest { results ->
                searchAdapter.submitList(results)
                
                if (results.isEmpty()) {
                    binding.rvSearchResults.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.ivEmptyIcon.setImageResource(R.drawable.ic_filter)
                    binding.tvEmptyTitle.text = "No results found"
                    binding.tvEmptySubtitle.text = "Try checking your spelling or search for something else"
                } else {
                    binding.rvSearchResults.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allLists.collect { lists ->
                    allLists = lists
                }
            }
        }
    }

    private fun loadRecentSearches() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedString = prefs.getString(KEY_RECENT_SEARCHES, "") ?: ""
        
        binding.chipGroupRecent.removeAllViews()
        
        if (savedString.isEmpty()) {
            binding.layoutRecentSearches.visibility = View.GONE
        } else {
            binding.layoutRecentSearches.visibility = View.VISIBLE
            val list = savedString.split(",").filter { it.isNotEmpty() }
            
            list.forEach { query ->
                val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                    text = query
                    isCheckable = false
                    isClickable = true
                    
                    // Outlined style
                    chipStrokeWidth = 1f
                    chipStrokeColor = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.grovia_outline))
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.screen_background))
                    setTextColor(requireContext().getColor(R.color.grovia_text_primary))
                    
                    setOnClickListener {
                        binding.etSearch.setText(query)
                        binding.etSearch.setSelection(query.length)
                        performSearch(query)
                    }
                }
                binding.chipGroupRecent.addView(chip)
            }
        }
    }

    private fun saveRecentSearch(query: String) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedString = prefs.getString(KEY_RECENT_SEARCHES, "") ?: ""
        val list = savedString.split(",").filter { it.isNotEmpty() }.toMutableList()
        
        // Remove duplicate if exists, then add to front
        list.remove(query)
        list.add(0, query)
        
        // Limit to 5 entries
        if (list.size > 5) {
            list.removeAt(list.size - 1)
        }
        
        prefs.edit().putString(KEY_RECENT_SEARCHES, list.joinToString(",")).apply()
    }

    private fun clearRecentSearches() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
        loadRecentSearches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Recycler Adapter for Search Results
    private class SearchResultAdapter(
        private val listsProvider: () -> List<ShoppingList>,
        private val onItemClick: (GroceryItem) -> Unit
    ) : RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder>() {

        private var items: List<GroceryItem> = emptyList()

        fun submitList(newItems: List<GroceryItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
            val binding = ItemSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class SearchViewHolder(private val binding: ItemSearchResultBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: GroceryItem) {
                binding.tvItemName.text = item.name
                binding.tvItemQuantity.text = "Qty: ${item.quantity}"

                // Find category to display chip
                val category = Category.fromString(item.category)
                binding.tvCategoryChip.text = category.displayName
                
                val color = android.graphics.Color.parseColor(category.colorHex)
                binding.tvCategoryChip.backgroundTintList = android.content.res.ColorStateList.valueOf(color)

                // Find source list
                val lists = listsProvider()
                val sourceList = lists.firstOrNull { it.id == item.listId }
                binding.tvListName.text = "In list: ${sourceList?.title ?: "Shopping List"}"

                binding.root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
}
