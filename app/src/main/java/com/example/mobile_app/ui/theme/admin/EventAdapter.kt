package com.example.mobile_app.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_app.data.EventEntity
import com.example.mobile_app.databinding.ItemEventBinding

class EventAdapter(
    private var items: List<EventEntity>
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEventBinding.inflate(inflater, parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = items[position]

        holder.binding.textTitle.text = event.title
        holder.binding.textFaculty.text = event.faculty
        holder.binding.textDateTime.text = "${event.date} at ${event.time}"
        holder.binding.textLocation.text = event.location
        holder.binding.textDescription.text = event.description
    }

    fun submitList(newItems: List<EventEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
