package com.example.sharedgroceryapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.sharedgroceryapp.GroceryApplication
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.example.sharedgroceryapp.databinding.FragmentAddEditItemBinding
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModel
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModelFactory

class AddEditItemFragment : Fragment() {

    private var _binding: FragmentAddEditItemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroceryViewModel by viewModels {
        GroceryViewModelFactory((requireActivity().application as GroceryApplication).repository)
    }

    private val args: AddEditItemFragmentArgs by navArgs()
    private var listId: Int = -1
    private var itemId: Int = -1
    private var isBought: Boolean = false
    private var quantity: Int = 1
    private var selectedCategory: String = "Other"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listId = args.listId
        itemId = args.itemId
        isBought = args.itemBought
        val name = args.itemName
        quantity = args.itemQuantity

        setupCategoryGroup()

        if (itemId != -1) {
            binding.tvHeaderTitle.text = "Edit Item"
            binding.etItemName.setText(name)
            binding.tvQuantity.text = quantity.toString()
        } else {
            binding.tvHeaderTitle.text = "Add Item"
        }

        setupListeners()
    }

    private fun setupCategoryGroup() {
        val context = requireContext()
        val currentCategory = args.itemCategory ?: "Other"
        selectedCategory = currentCategory

        com.example.sharedgroceryapp.data.local.Category.values().forEach { category ->
            val chip = com.google.android.material.chip.Chip(context).apply {
                id = View.generateViewId()
                text = category.displayName
                chipIcon = context.getDrawable(category.iconResId)
                isChipIconVisible = true
                isCheckable = true
                tag = category.name
                chipStrokeWidth = 0f
                
                // Rounded corners 20dp
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
                        selectedCategory = category.name
                    }
                }
            }
            binding.chipGroupCategory.addView(chip)

            if (category.name.equals(currentCategory, ignoreCase = true)) {
                chip.isChecked = true
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDecrement.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }

        binding.btnIncrement.setOnClickListener {
            if (quantity < 99) {
                quantity++
                binding.tvQuantity.text = quantity.toString()
            }
        }

        binding.btnSave.setOnClickListener {
            saveItem()
        }
    }

    private fun saveItem() {
        val name = binding.etItemName.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilItemName.error = "Item name cannot be empty"
            return
        }

        binding.tilItemName.error = null

        if (itemId == -1) {
            // Add Mode
            val item = GroceryItem(listId = listId, name = name, quantity = quantity, isBought = false, category = selectedCategory)
            viewModel.insertItem(item)
            Toast.makeText(requireContext(), "Item added", Toast.LENGTH_SHORT).show()
        } else {
            // Edit Mode
            val item = GroceryItem(id = itemId, listId = listId, name = name, quantity = quantity, isBought = isBought, category = selectedCategory)
            viewModel.updateItem(item)
            Toast.makeText(requireContext(), "Item updated", Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
