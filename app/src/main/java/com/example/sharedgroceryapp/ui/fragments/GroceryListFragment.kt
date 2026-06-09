package com.example.sharedgroceryapp.ui.fragments

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
import com.example.sharedgroceryapp.data.local.Category
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

    sealed class ListItem {
        data class Header(val category: Category, val count: Int) : ListItem()
        data class Item(val groceryItem: GroceryItem) : ListItem()
    }

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
                    itemQuantity = item.quantity,
                    itemCategory = item.category
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

        binding.fabFilter.setOnClickListener {
            showFilterBottomSheet()
        }
    }

    private fun showFilterBottomSheet() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val dialogBinding = com.example.sharedgroceryapp.databinding.DialogFilterCategoriesBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val currentSelected = viewModel.selectedCategories.value.toMutableSet()

        // Populate chips
        Category.values().forEach { category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                id = View.generateViewId()
                text = requireContext().getString(category.displayNameResId)
                chipIcon = requireContext().getDrawable(category.iconResId)
                isChipIconVisible = true
                isCheckable = true
                isChecked = currentSelected.contains(category.name)
                tag = category.name
                chipStrokeWidth = 0f
                
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(20 * resources.displayMetrics.density)
                    .build()

                val color = android.graphics.Color.parseColor(category.colorHex)
                val colorAlpha = android.graphics.Color.parseColor("#33" + category.colorHex.removePrefix("#"))

                val bgStates = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val bgColors = intArrayOf(
                    color,      // checked
                    colorAlpha  // unchecked
                )
                chipBackgroundColor = android.content.res.ColorStateList(bgStates, bgColors)

                val textColors = intArrayOf(
                    android.graphics.Color.WHITE, // checked
                    color                        // unchecked
                )
                setTextColor(android.content.res.ColorStateList(bgStates, textColors))
                chipIconTint = android.content.res.ColorStateList(bgStates, textColors)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        currentSelected.add(category.name)
                    } else {
                        currentSelected.remove(category.name)
                    }
                    viewModel.setSelectedCategories(currentSelected)
                }
            }
            dialogBinding.chipGroupFilter.addView(chip)
        }

        dialogBinding.btnShowAll.setOnClickListener {
            viewModel.setSelectedCategories(emptySet())
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        // Collect full items for stats and QR share
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getItemsForList(listId).collect { items ->
                    currentList = items
                    updateStats(items)
                }
            }
        }

        // Collect filtered items for adapter list display
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getFilteredItemsForList(listId).collect { items ->
                    // Group and map to ListItems
                    val grouped = items.groupBy { Category.fromString(it.category) }
                    val displayList = mutableListOf<ListItem>()
                    
                    Category.values().forEach { category ->
                        val categoryItems = grouped[category]
                        if (!categoryItems.isNullOrEmpty()) {
                            val sortedCategoryItems = categoryItems.sortedWith(
                                compareBy<GroceryItem> { it.isBought }
                                    .thenByDescending { it.id }
                            )
                            displayList.add(ListItem.Header(category, sortedCategoryItems.size))
                            sortedCategoryItems.forEach { item ->
                                displayList.add(ListItem.Item(item))
                            }
                        }
                    }
                    
                    adapter.submitList(displayList)
                    
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

    private fun updateStats(items: List<GroceryItem>) {
        val total = items.size
        val completed = items.count { it.isBought }
        binding.tvListStats.text = getString(R.string.list_stats_format, total, completed)
        
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
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items: List<ListItem> = emptyList()

        companion object {
            private const val TYPE_HEADER = 0
            private const val TYPE_ITEM = 1
        }

        fun submitList(newItems: List<ListItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is ListItem.Header -> TYPE_HEADER
                is ListItem.Item -> TYPE_ITEM
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_HEADER) {
                val binding = com.example.sharedgroceryapp.databinding.ItemCategoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            } else {
                val binding = ItemGroceryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                GroceryViewHolder(binding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
                is ListItem.Item -> (holder as GroceryViewHolder).bind(item.groceryItem)
            }
        }

        override fun getItemCount(): Int = items.size

        inner class HeaderViewHolder(private val binding: com.example.sharedgroceryapp.databinding.ItemCategoryHeaderBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(header: ListItem.Header) {
                val category = header.category
                val displayName = binding.root.context.getString(category.displayNameResId)
                binding.tvHeaderName.text = displayName.uppercase()
                binding.ivHeaderIcon.setImageResource(category.iconResId)
                binding.tvHeaderCount.text = header.count.toString()

                val color = android.graphics.Color.parseColor(category.colorHex)
                binding.tvHeaderName.setTextColor(color)
                binding.ivHeaderIcon.imageTintList = android.content.res.ColorStateList.valueOf(color)
                binding.tvHeaderCount.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            }
        }

        inner class GroceryViewHolder(private val binding: ItemGroceryBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: GroceryItem) {
                binding.tvItemName.text = item.name
                binding.tvItemQuantity.text = binding.root.context.getString(R.string.item_qty_format, item.quantity)
                
                if (item.isBought) {
                    binding.root.alpha = 0.5f
                    binding.viewStatusStrip.setBackgroundColor(binding.root.context.getColor(R.color.grovia_secondary))
                } else {
                    binding.root.alpha = 1.0f
                    binding.viewStatusStrip.setBackgroundColor(binding.root.context.getColor(R.color.grovia_primary))
                }
                
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
