package com.example.sharedgroceryapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.sharedgroceryapp.data.local.ShoppingList
import com.example.sharedgroceryapp.databinding.FragmentHomeListsBinding
import com.example.sharedgroceryapp.databinding.ItemShoppingListBinding
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModel
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeListsFragment : Fragment() {

    private var _binding: FragmentHomeListsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroceryViewModel by viewModels {
        GroceryViewModelFactory((requireActivity().application as GroceryApplication).repository)
    }

    private lateinit var adapter: ShoppingListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ShoppingListAdapter(
            onListClick = { list ->
                val action = HomeListsFragmentDirections.actionHomeListsFragmentToGroceryListFragment(list.id, list.title)
                findNavController().navigate(action)
            },
            onListDelete = { list ->
                showDeleteConfirmationDialog(list)
            }
        )
        binding.rvShoppingLists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvShoppingLists.adapter = adapter
    }

    private fun setupToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.toolbar.inflateMenu(R.menu.menu_home_lists)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_toggle_theme -> {
                    val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                    if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                        )
                    } else {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                        )
                    }
                    true
                }
                R.id.action_settings -> {
                    val action = HomeListsFragmentDirections.actionHomeListsFragmentToSettingsFragment()
                    navController.navigate(action)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddList.setOnClickListener {
            showAddListDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allLists.collect { lists ->
                        updateUi(lists, viewModel.allGroceryItems.value)
                    }
                }
                launch {
                    viewModel.allGroceryItems.collect { items ->
                        updateUi(viewModel.allLists.value, items)
                    }
                }
            }
        }
    }

    private fun updateUi(lists: List<ShoppingList>, items: List<GroceryItem>) {
        val totalItems = items.size
        binding.tvSummaryStats.text = "${lists.size} lists • $totalItems items total"

        val itemCounts = items.groupBy { it.listId }.mapValues { it.value.size }
        adapter.submitList(lists, itemCounts)

        if (lists.isEmpty()) {
            binding.rvShoppingLists.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
            binding.ivEmptyPencil.startAnimation(pulseAnimation)
        } else {
            binding.rvShoppingLists.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            binding.ivEmptyPencil.clearAnimation()
        }
    }

    private fun showAddListDialog() {
        val context = requireContext()
        val dialog = BottomSheetDialog(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_list, null)
        dialog.setContentView(dialogView)

        val etListName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etListName)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
        val btnCreate = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreate)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnCreate.setOnClickListener {
            val title = etListName.text.toString().trim()
            if (title.isNotEmpty()) {
                viewModel.insertList(ShoppingList(title = title))
                dialog.dismiss()
            } else {
                Toast.makeText(context, "List name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(list: ShoppingList) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete List?")
            .setMessage("Are you sure you want to delete '${list.title}'? All items in this list will be permanently removed.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteList(list)
                Toast.makeText(requireContext(), "List deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // RecyclerView Adapter
    private class ShoppingListAdapter(
        private val onListClick: (ShoppingList) -> Unit,
        private val onListDelete: (ShoppingList) -> Unit
    ) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>() {

        private var items: List<ShoppingList> = emptyList()
        private var itemCounts: Map<Int, Int> = emptyMap()

        fun submitList(newItems: List<ShoppingList>, newCounts: Map<Int, Int>) {
            items = newItems
            itemCounts = newCounts
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
            val binding = ItemShoppingListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ShoppingListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {
            val list = items[position]
            val count = itemCounts[list.id] ?: 0
            holder.bind(list, count)
        }

        override fun getItemCount(): Int = items.size

        inner class ShoppingListViewHolder(private val binding: ItemShoppingListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            fun bind(list: ShoppingList, itemCount: Int) {
                binding.tvListTitle.text = list.title
                binding.tvListDate.text = "Created: ${dateFormatter.format(Date(list.createdAt))}"
                binding.tvItemCount.text = itemCount.toString()

                binding.root.setOnClickListener {
                    onListClick(list)
                }

                binding.btnDelete.setOnClickListener {
                    onListDelete(list)
                }
            }
        }
    }
}
