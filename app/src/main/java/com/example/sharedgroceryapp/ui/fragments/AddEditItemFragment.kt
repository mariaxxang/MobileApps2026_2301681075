package com.example.sharedgroceryapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

    private var listId: Int = -1
    private var itemId: Int = -1
    private var isBought: Boolean = false
    private var quantity: Int = 1

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

        // Read arguments from Navigation Bundle
        arguments?.let {
            listId = it.getInt("listId", -1)
            itemId = it.getInt("itemId", -1)
            isBought = it.getBoolean("itemBought", false)
            val name = it.getString("itemName")
            quantity = it.getInt("itemQuantity", 1)

            if (itemId != -1) {
                binding.toolbar.title = "Edit Item"
                binding.etItemName.setText(name)
                binding.tvQuantity.text = quantity.toString()
            }
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
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
            val item = GroceryItem(listId = listId, name = name, quantity = quantity, isBought = false)
            viewModel.insertItem(item)
            Toast.makeText(requireContext(), "Item added", Toast.LENGTH_SHORT).show()
        } else {
            // Edit Mode
            val item = GroceryItem(id = itemId, listId = listId, name = name, quantity = quantity, isBought = isBought)
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
