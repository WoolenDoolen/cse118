package edu.ucsc.cse118.assignment2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.ucsc.cse118.assignment2.databinding.ItemListBinding

class ListAdapter(
  private val rows: List<Pair<String, String>>,
  private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
  class ViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.binding.name.text = rows[position].first
    holder.binding.count.text = rows[position].second
    holder.binding.root.setOnClickListener { onClick(position) }
  }

  override fun getItemCount(): Int = rows.size
}
