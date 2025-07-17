package com.regula.documentreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TextDataFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TextDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_text_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        loadTextData()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewTextData)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TextDataAdapter()
        recyclerView.adapter = adapter
    }

    private fun loadTextData() {
        val documentResults = (activity as? OcrResultsActivity)?.documentResults
        val textDataList = mutableListOf<TextDataItem>()

        documentResults?.textResult?.fields?.forEach { field ->
            val fieldName = field.getFieldName(requireContext()) ?: "Champ inconnu"

            field.values.forEach { value ->
                val item = TextDataItem(
                    fieldName = fieldName,
                    fieldValue = value.value ?: "",
                    status = getValidityStatus(value.field.validityList, value.sourceType)
                )
                textDataList.add(item)
            }
        }

        adapter.updateData(textDataList)
    }

    private fun getValidityStatus(
        validityList: List<com.regula.documentreader.api.results.DocumentReaderValidity>,
        sourceType: Int?
    ): String {
        validityList.forEach { validity ->
            if (validity.sourceType == sourceType) {
                return when (validity.status) {
                    com.regula.documentreader.api.enums.eCheckResult.CH_CHECK_OK.value -> "✓"
                    com.regula.documentreader.api.enums.eCheckResult.CH_CHECK_ERROR.value -> "✗"
                    else -> "—"
                }
            }
        }
        return "—"
    }
}