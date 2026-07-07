package com.et.expensetrackorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.et.expensetrackorapp.databinding.FragmentDeleteConfirmBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeleteConfirmFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDeleteConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // read the count Settings passed in (fallback 0)
        val count = arguments?.getInt("count") ?: 0
        val txnWord = if (count == 1) "transaction" else "transactions"

        binding.deleteMessage.text =
            "This will permanently remove all $count $txnWord. " +
                    "Your budget and currency stay. This can't be undone."

        // ✕ and Cancel both just close
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        // Delete all → tell Settings, then close
        binding.btnConfirmDelete.setOnClickListener {
            setFragmentResult("delete_all_result", bundleOf("confirmed" to true))
            dismiss()
        }
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

    // helper so Settings can create it with the count in one line
    companion object {
        fun newInstance(count: Int): DeleteConfirmFragment {
            return DeleteConfirmFragment().apply {
                arguments = bundleOf("count" to count)
            }
        }
    }
}