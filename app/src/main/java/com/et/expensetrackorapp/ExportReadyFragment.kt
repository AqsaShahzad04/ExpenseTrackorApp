package com.et.expensetrackorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.et.expensetrackorapp.databinding.FragmentExportReadyBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ExportReadyFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentExportReadyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportReadyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // read the values ExportFragment passed in (fallbacks are safe defaults)
        val format = arguments?.getString("format") ?: "PDF"
        val count = arguments?.getInt("count") ?: 0
        val range = arguments?.getString("range") ?: ""

        val txnWord = if (count == 1) "transaction" else "transactions"

        // "CSV downloaded" / "PDF downloaded"
        binding.textDownloaded.text = "$format downloaded"
        // "8 transactions · This month"
        binding.textSubtitle.text = "$count $txnWord · $range"

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnDone.setOnClickListener { dismiss() }
    }

    override fun getTheme(): Int = R.style.RoundedBottomSheet

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // helper so ExportFragment can create it in one line
    companion object {
        fun newInstance(format: String, count: Int, range: String): ExportReadyFragment {
            return ExportReadyFragment().apply {
                arguments = bundleOf(
                    "format" to format,
                    "count" to count,
                    "range" to range
                )
            }
        }
    }
}