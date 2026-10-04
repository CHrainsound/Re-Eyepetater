package com.example.ept.search.adapter.resultcontent

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.core.model.MetroData
import com.example.core.model.NavPaths
import com.example.ept.search.R
import com.therouter.TheRouter

/**   
 * 包名称： com.example.ept.search.adapter
 * 类名称：ResultUgcFragment
 * 类描述：TODO
 * 创建人：韦西波
 * 创建时间：2026-07-16 15:01
 *
 */
class ResultUgcAdapter :
    ListAdapter<MetroData, ResultUgcAdapter.UgcViewHolder>(UgcDiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UgcViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemview_ugc, parent, false)
        return UgcViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UgcViewHolder,
        position: Int
    ) {
        holder.bindData(getItem(position))
    }

    inner class UgcViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUgcProfileItem: ImageView =
            itemView.findViewById(R.id.iv_ugc_profile_item)
        val tvUgcNameItem: TextView =
            itemView.findViewById(R.id.tv_ugc_name_item)
        val tvUgcDescItem: TextView =
            itemView.findViewById(R.id.tv_ugc_desc_item)

        fun bindData(metroData: MetroData) {
            itemView.setOnClickListener {
                TheRouter.build(NavPaths.PERSON)
                    .withString("uid",metroData.uid.toString())
                    .navigation()
            }
            Glide.with(ivUgcProfileItem.context)
                .load(metroData.avatar?.url)
                .placeholder(R.drawable.eyepetater)
                .error(R.drawable.eyepetater)
                .into(ivUgcProfileItem)
            tvUgcNameItem.text = metroData.nick
            tvUgcDescItem.text = metroData.description
        }

    }

}

object UgcDiffCallback : DiffUtil.ItemCallback<MetroData>() {
    override fun areItemsTheSame(p0: MetroData, p1: MetroData): Boolean {
        return p0.uid == p1.uid
    }

    override fun areContentsTheSame(p0: MetroData, p1: MetroData): Boolean {
        return p0.link == p1.link
    }
}