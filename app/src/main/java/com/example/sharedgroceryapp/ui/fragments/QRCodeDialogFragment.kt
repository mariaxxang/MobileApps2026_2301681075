package com.example.sharedgroceryapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sharedgroceryapp.databinding.DialogQrCodeBinding
import com.example.sharedgroceryapp.utils.QRCodeGenerator

class QRCodeDialogFragment : DialogFragment() {

    private var _binding: DialogQrCodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogQrCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val qrContent = arguments?.getString("qrContent") ?: "Empty List"

        // Generate QR code in background or on UI thread (small enough for main thread)
        val qrBitmap = QRCodeGenerator.generateQRCode(qrContent)
        if (qrBitmap != null) {
            binding.ivQRCode.setImageBitmap(qrBitmap)
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
