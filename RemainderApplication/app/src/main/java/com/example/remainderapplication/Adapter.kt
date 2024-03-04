package com.example.remainderapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView

class Adapter(private val memberList: ArrayList<Member>,private val listener: OnItemDeleteListener) : RecyclerView.Adapter<Adapter.ViewHolder>() {


    interface OnItemDeleteListener {
        fun onDeleteClick(position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.remaiders_item,
           parent,false)
        return ViewHolder(itemView,listener)
    }

    override fun getItemCount(): Int {
        return  memberList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curretitem = memberList[position]

        holder.RemName.text=curretitem.name
        holder.RemAddress.text=curretitem.address
        holder.RemDate.text=curretitem.date
        holder.imagebin.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(adapterPosition)
            }
        }





    }

    class ViewHolder(itemView: View,listener: OnItemDeleteListener) : RecyclerView.ViewHolder(itemView){
        val RemName: TextView= itemView.findViewById(R.id.RemName)
        val RemAddress: TextView= itemView.findViewById(R.id.RemAddress)
        val RemDate: TextView= itemView.findViewById(R.id.RemDate)
        val imagebin: ImageView= itemView.findViewById(R.id.imageViewbin)



    }
}