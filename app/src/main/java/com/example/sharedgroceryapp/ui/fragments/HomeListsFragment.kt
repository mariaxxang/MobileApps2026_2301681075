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
                val bundle = Bundle().apply {
                    putInt("listId", list.id)
                    putString("listTitle", list.title)
                }
                findNavController().navigate(
                    R.id.action_homeListsFragment_to_groceryListFragment,
                    bundle
                )
            },
            onListDelete = { list ->
                showDeleteConfirmationDialog(list)
            }
        )
        binding.rvShoppingLists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvShoppingLists.adapter = adapter
    }

    private fun setupToolbar() {
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
                viewModel.allLists.collect { lists ->
                    adapter.submitList(lists)
                    if (lists.isEmpty()) {
                        binding.rvShoppingLists.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvShoppingLists.visibility = View.VISIBLE
                        binding.layoutEmptyState.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showAddListDialog() {
        val context = requireContext()
        val textInputLayout = com.google.android.material.textfield.TextInputLayout(context).apply {
            hint = "List Name (e.g., Weekly Groceries)"
        }
        val input = com.google.android.material.textfield.TextInputEditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            maxLines = 1
        }
        textInputLayout.addView(input)

        val container = android.widget.FrameLayout(context).apply {
            val params = android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = (16 * resources.displayMetrics.density).toInt()
                rightMargin = (16 * resources.displayMetrics.density).toInt()
                topMargin = (8 * resources.displayMetrics.density).toInt()
                bottomMargin = (8 * resources.displayMetrics.density).toInt()
            }
            layoutParams = params
            addView(textInputLayout)
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle("Create Shopping List")
            .setView(container)
            .setPositiveButton("Create") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) {
                    viewModel.insertList(ShoppingList(title = title))
                } else {
                    Toast.makeText(context, "List name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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

        fun submitList(newItems: List<ShoppingList>) {
            items = newItems
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
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ShoppingListViewHolder(private val binding: ItemShoppingListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            fun bind(list: ShoppingList) {
                binding.tvListTitle.text = list.title
                binding.tvListDate.text = "Created: ${dateFormatter.format(Date(list.createdAt))}"

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
