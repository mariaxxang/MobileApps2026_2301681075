package com.example.sharedgroceryapp.ui.fragments

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.example.sharedgroceryapp.databinding.FragmentGroceryListBinding
import com.example.sharedgroceryapp.databinding.ItemGroceryBinding
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModel
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModelFactory
import com.example.sharedgroceryapp.utils.QRCodeGenerator
import kotlinx.coroutines.launch

class GroceryListFragment : Fragment() {

    private var _binding: FragmentGroceryListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroceryViewModel by viewModels {
        GroceryViewModelFactory((requireActivity().application as GroceryApplication).repository)
    }

    private lateinit var adapter: GroceryAdapter
    private var currentList: List<GroceryItem> = emptyList()
    private var listId: Int = -1
    private var listTitle: String = "Shopping List"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroceryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            listId = it.getInt("listId", -1)
            listTitle = it.getString("listTitle", "Shopping List")
        }

        setupRecyclerView()
        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = GroceryAdapter(
            onItemChecked = { item, isChecked ->
                viewModel.updateItem(item.copy(isBought = isChecked))
            },
            onItemEdit = { item ->
                val bundle = Bundle().apply {
                    putInt("listId", listId)
                    putInt("itemId", item.id)
                    putBoolean("itemBought", item.isBought)
                    putString("itemName", item.name)
                    putInt("itemQuantity", item.quantity)
                }
                findNavController().navigate(
                    R.id.action_groceryListFragment_to_addEditItemFragment,
                    bundle
                )
            },
            onItemDelete = { item ->
                viewModel.deleteItem(item)
            }
        )
        binding.rvGroceryList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGroceryList.adapter = adapter
    }

    private fun setupToolbar() {
        binding.toolbar.title = listTitle
        binding.toolbar.inflateMenu(R.menu.menu_grocery_list)
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
                R.id.action_share_qr -> {
                    val formattedList = QRCodeGenerator.formatGroceryList(currentList)
                    val bundle = Bundle().apply {
                        putString("qrContent", formattedList)
                    }
                    findNavController().navigate(
                        R.id.action_groceryListFragment_to_qrCodeDialogFragment,
                        bundle
                    )
                    true
                }
                else -> false
            }
        }
        
        // Setup back navigation
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationIconTint(android.graphics.Color.WHITE)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        binding.fabAddItem.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("listId", listId)
            }
            findNavController().navigate(
                R.id.action_groceryListFragment_to_addEditItemFragment,
                bundle
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getItemsForList(listId).collect { items ->
                    currentList = items
                    adapter.submitList(items)
                    if (items.isEmpty()) {
                        binding.rvGroceryList.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvGroceryList.visibility = View.VISIBLE
                        binding.layoutEmptyState.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // RecyclerView Adapter and ViewHolder
    private class GroceryAdapter(
        private val onItemChecked: (GroceryItem, Boolean) -> Unit,
        private val onItemEdit: (GroceryItem) -> Unit,
        private val onItemDelete: (GroceryItem) -> Unit
    ) : RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

        private var items: List<GroceryItem> = emptyList()

        fun submitList(newItems: List<GroceryItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
            val binding = ItemGroceryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return GroceryViewHolder(binding)
        }

        override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class GroceryViewHolder(private val binding: ItemGroceryBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: GroceryItem) {
                binding.tvItemName.text = item.name
                binding.tvItemQuantity.text = "Quantity: ${item.quantity}"
                
                // Remove existing listener before setting checked status to avoid trigger recursion
                binding.checkboxBought.setOnCheckedChangeListener(null)
                binding.checkboxBought.isChecked = item.isBought

                toggleStrikethrough(binding.tvItemName, item.isBought)

                binding.checkboxBought.setOnCheckedChangeListener { _, isChecked ->
                    onItemChecked(item, isChecked)
                }

                binding.btnEdit.setOnClickListener {
                    onItemEdit(item)
                }

                binding.btnDelete.setOnClickListener {
                    onItemDelete(item)
                }
            }

            private fun toggleStrikethrough(textView: TextView, isBought: Boolean) {
                if (isBought) {
                    textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textView.alpha = 0.5f
                } else {
                    textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    textView.alpha = 1.0f
                }
            }
        }
    }
}
