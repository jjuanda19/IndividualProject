package com.example.remainderapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView

class Adapter(private val memberList: ArrayList<Member>) : RecyclerView.Adapter<Adapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.remaiders_item,
           parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return  memberList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curretitem = memberList[position]

        holder.RemName.text=curretitem.name
        holder.RemAddress.text=curretitem.address
        holder.RemDate.text=curretitem.date


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val RemName: TextView= itemView.findViewById(R.id.RemName)
        val RemAddress: TextView= itemView.findViewById(R.id.RemAddress)
        val RemDate: TextView= itemView.findViewById(R.id.RemDate)



    }
}