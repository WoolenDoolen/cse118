package edu.ucsc.cse118.assignment2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.ucsc.cse118.assignment2.databinding.ItemMessageBinding

class MessageAdapter(
  private val messages: List<Message>,
  private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
  class ViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val message = messages[position]
    holder.binding.name.text = message.user.name
    holder.binding.date.text = message.formattedDate()
    holder.binding.content.text = message.content
    holder.binding.root.setOnClickListener { onClick(position) }
  }

  override fun getItemCount(): Int = messages.size
}
