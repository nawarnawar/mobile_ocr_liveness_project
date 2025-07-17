package com.regula.documentreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TextDataAdapter : RecyclerView.Adapter<TextDataAdapter.TextDataViewHolder>() {
    
    private var textDataList: List<TextDataItem> = emptyList()
    
    fun updateData(newData: List<TextDataItem>) {
        textDataList = newData
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextDataViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_text_data, parent, false)
        return TextDataViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TextDataViewHolder, position: Int) {
        holder.bind(textDataList[position])
    }
    
    override fun getItemCount(): Int = textDataList.size
    
    class TextDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFieldName: TextView = itemView.findViewById(R.id.tvFieldName)
        private val tvFieldValue: TextView = itemView.findViewById(R.id.tvFieldValue)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        
        fun bind(item: TextDataItem) {
            tvFieldName.text = item.fieldName
            tvFieldValue.text = item.fieldValue
            tvStatus.text = item.status
            
            // Changer la couleur du statut selon la valeur
            when (item.status) {
                "✓" -> tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                "✗" -> tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                else -> tvStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            }
        }
    }
}

