package com.example.remainderapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Defines a custom adapter class that extends RecyclerView.Adapter and is parameterized by its own ViewHolder class.
// It also accepts an ArrayList of Member objects and an OnItemDeleteListener for handling delete actions.
class Adapter(private val memberList: ArrayList<Member>, private val listener: OnItemDeleteListener) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    // Interface for handling delete clicks outside of the adapter.
    interface OnItemDeleteListener {
        fun onDeleteClick(position: Int)
    }

    // Creates new views (invoked by the layout manager).
    // This method inflates the item layout and returns a ViewHolder with the layout and the delete listener.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.remaiders_item, parent, false)
        return ViewHolder(itemView, listener)
    }

    // Returns the size of your dataset (invoked by the layout manager).
    override fun getItemCount(): Int {
        return memberList.size
    }

    // Replaces the contents of a view (invoked by the layout manager).
    // Binds data from the Member object at the given position to the views in the ViewHolder.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = memberList[position]

        // Setting the member's details to the TextViews.
        holder.RemName.text = currentItem.name
        holder.RemAddress.text = currentItem.address
        holder.RemDate.text = currentItem.date
        holder.RemDescription.text = currentItem.description

        // Setting a click listener on the delete button. When clicked, it calls onDeleteClick() of the listener passed to the adapter.
        holder.imagebin.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(adapterPosition)
            }
        }
    }

    // Defines a ViewHolder class that holds references to the views for each data item.
    // These views are reused to display new items when scrolling.
    class ViewHolder(itemView: View, listener: OnItemDeleteListener) : RecyclerView.ViewHolder(itemView) {
        val RemName: TextView = itemView.findViewById(R.id.RemName)
        val RemAddress: TextView = itemView.findViewById(R.id.RemAddress)
        val RemDate: TextView = itemView.findViewById(R.id.RemDate)
        val imagebin: ImageView = itemView.findViewById(R.id.imageViewbin)
        val RemDescription: TextView = itemView.findViewById(R.id.RemDescrip)
    }
}
