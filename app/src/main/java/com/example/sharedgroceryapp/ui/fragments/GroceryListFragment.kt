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
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedgroceryapp.GroceryApplication
import com.example.sharedgroceryapp.R
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.example.sharedgroceryapp.databinding.FragmentGroceryListBinding
import com.example.sharedgroceryapp.databinding.ItemGroceryBinding
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModel
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModelFactory
import com.example.sharedgroceryapp.utils.GroceryListUtils
import com.example.sharedgroceryapp.utils.QRCodeGenerator
import kotlinx.coroutines.launch

class GroceryListFragment : Fragment() {

    private var _binding: FragmentGroceryListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroceryViewModel by viewModels {
        GroceryViewModelFactory((requireActivity().application as GroceryApplication).repository)
    }

    private lateinit var adapter: GroceryAdapter
    private val args: GroceryListFragmentArgs by navArgs()
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

        listId = args.listId
        listTitle = args.listTitle

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
                val action = GroceryListFragmentDirections.actionGroceryListFragmentToAddEditItemFragment(
                    listId = listId,
                    itemId = item.id,
                    itemBought = item.isBought,
                    itemName = item.name,
                    itemQuantity = item.quantity
                )
                findNavController().navigate(action)
            },
            onItemDelete = { item ->
                viewModel.deleteItem(item)
            }
        )
        binding.rvGroceryList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGroceryList.adapter = adapter
    }

    private fun setupToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.toolbar.navigationIcon?.setTint(requireContext().getColor(R.color.toolbar_content))

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
                    val action = GroceryListFragmentDirections.actionGroceryListFragmentToQrCodeDialogFragment(formattedList)
                    navController.navigate(action)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddItem.setOnClickListener {
            val action = GroceryListFragmentDirections.actionGroceryListFragmentToAddEditItemFragment(
                listId = listId
            )
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getItemsForList(listId).collect { items ->
                    val sortedItems = GroceryListUtils.sortItems(items)
                    currentList = sortedItems
                    adapter.submitList(sortedItems)
                    
                    updateStats(sortedItems)
                    
                    if (sortedItems.isEmpty()) {
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

    private fun updateStats(items: List<GroceryItem>) {
        val total = items.size
        val completed = items.count { it.isBought }
        binding.tvListStats.text = "$total items • $completed completed"
        
        val progress = if (total > 0) (completed * 100) / total else 0
        binding.progressIndicator.setProgress(progress, true)
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
                binding.tvItemQuantity.text = "Qty: ${item.quantity}"
                
                if (item.isBought) {
                    binding.root.alpha = 0.5f
                    binding.viewStatusStrip.setBackgroundColor(binding.root.context.getColor(R.color.grovia_secondary))
                } else {
                    binding.root.alpha = 1.0f
                    binding.viewStatusStrip.setBackgroundColor(binding.root.context.getColor(R.color.grovia_primary))
                }
                
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
