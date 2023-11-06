package com.example.mykbcon1


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mykbcon1.databinding.RvMainBinding
import com.kkmcn.kbeaconlib2.KBeacon


class Adapter:RecyclerView.Adapter<Adapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: RvMainBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding=RvMainBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }
    private val differCallback= object :DiffUtil.ItemCallback<KBeacon>(){


        override fun areItemsTheSame(oldItem: KBeacon, newItem: KBeacon): Boolean {
            return oldItem.name==newItem.name
        }

        override fun areContentsTheSame(oldItem: KBeacon, newItem: KBeacon): Boolean {
            return oldItem.mac==newItem.mac
        }

    }
    val differ=AsyncListDiffer(this,differCallback)
//    var kbecons:List<KBeacon>
//        get() = differ.currentList
//        set(value){differ.submitList(value)}

    override fun getItemCount(): Int {
//        return kbecons.size
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val curr=differ.currentList[position]
        holder.binding.apply {
            tvAddress.text=curr.mac
            tvDeviceName.text=curr.name
            holder.itemView.setOnClickListener {
                myListener?.let {
                    it(curr)

                }
            }
        }
    }


    //now make the recycler view item click listener

    private var myListener:((KBeacon)->Unit)?=null
    fun setOnItemClickListener(listener:(KBeacon)->Unit){
        myListener=listener
    }




}