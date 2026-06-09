package com.example.sharedgroceryapp.ui.fragments

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
import com.example.sharedgroceryapp.GroceryApplication
import com.example.sharedgroceryapp.R
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.example.sharedgroceryapp.databinding.FragmentListDetailBinding
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModel
import com.example.sharedgroceryapp.ui.viewmodel.GroceryViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListDetailFragment : Fragment() {

    private var _binding: FragmentListDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroceryViewModel by viewModels {
        GroceryViewModelFactory((requireActivity().application as GroceryApplication).repository)
    }

    private val args: ListDetailFragmentArgs by navArgs()
    private var listId: Long = -1
    private var listName: String = "List Details"
    private var itemsList: List<GroceryItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listId = args.listId
        listName = args.listName

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.toolbar.title = listName
        binding.toolbar.navigationIcon?.setTint(requireContext().getColor(R.color.toolbar_content))
    }

    private fun setupListeners() {
        binding.btnViewItems.setOnClickListener {
            val action = ListDetailFragmentDirections.actionListDetailFragmentToGroceryListFragment(
                listId = listId.toInt(),
                listTitle = listName
            )
            findNavController().navigate(action)
        }

        binding.btnDuplicateList.setOnClickListener {
            showDuplicateListDialog()
        }

        binding.btnShareList.setOnClickListener {
            shareList()
        }

        binding.btnClearCompleted.setOnClickListener {
            viewModel.deleteCompletedItems(listId.toInt())
            Toast.makeText(requireContext(), "Completed items cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDuplicateListDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_list, null)
        dialog.setContentView(dialogView)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val etListName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etListName)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
        val btnCreate = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreate)

        tvTitle.text = "Duplicate Shopping List"
        etListName.setText("$listName (Copy)")
        btnCreate.text = "Duplicate"

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnCreate.setOnClickListener {
            val newTitle = etListName.text.toString().trim()
            if (newTitle.isNotEmpty()) {
                viewModel.duplicateList(newTitle, itemsList)
                Toast.makeText(requireContext(), "List duplicated successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                etListName.error = "Name cannot be empty"
            }
        }

        dialog.show()
    }

    private fun shareList() {
        val shareBuilder = StringBuilder()
        shareBuilder.append("Shopping List: $listName\n\n")

        if (itemsList.isEmpty()) {
            shareBuilder.append("(No items)")
        } else {
            itemsList.forEach { item ->
                val status = if (item.isBought) "[x]" else "[ ]"
                shareBuilder.append("$status ${item.name} (Qty: ${item.quantity}) - ${item.category}\n")
            }
        }

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Shopping List: $listName")
            putExtra(android.content.Intent.EXTRA_TEXT, shareBuilder.toString())
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share List"))
    }

    private fun observeViewModel() {
        // Observe lists to get details like creation time
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allLists.collect { lists ->
                    val list = lists.firstOrNull { it.id == listId.toInt() }
                    if (list != null) {
                        listName = list.title
                        binding.toolbar.title = listName
                        binding.tvDetailListName.text = list.title

                        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                        val dateStr = dateFormat.format(Date(list.createdAt))
                        binding.tvDetailListCreated.text = "Created on: $dateStr"
                    }
                }
            }
        }

        // Observe items in this list for statistics
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getItemsForList(listId.toInt()).collect { items ->
                    itemsList = items
                    updateStatsUI(items)
                }
            }
        }
    }

    private fun updateStatsUI(items: List<GroceryItem>) {
        val total = items.size
        val completed = items.count { it.isBought }
        val active = total - completed
        val totalQty = items.sumOf { it.quantity }

        binding.tvDetailListStats.text = "$total items total • $completed completed"
        binding.tvActiveItemsCount.text = active.toString()
        binding.tvCompletedItemsCount.text = completed.toString()
        binding.tvTotalQuantityCount.text = totalQty.toString()

        val progress = if (total > 0) (completed * 100) / total else 0
        binding.progressIndicatorDetail.setProgress(progress, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
