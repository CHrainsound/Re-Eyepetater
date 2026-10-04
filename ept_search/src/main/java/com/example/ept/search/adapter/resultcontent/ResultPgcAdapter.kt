package com.example.ept.search.adapter.resultcontent

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
 * 类名称：ResultPgcAdapter
 * 类描述：TODO
 * 创建人：韦西波
 * 创建时间：2026-07-16 15:01
 *
 */
class ResultPgcAdapter :
    ListAdapter<MetroData, ResultPgcAdapter.PgcViewHolder>(PgcDiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PgcViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemview_pgc, parent, false)
        return PgcViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PgcViewHolder,
        position: Int
    ) {
        holder.bindData(getItem(position))
    }

    inner class PgcViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPgcProfileItem: ImageView =
            itemView.findViewById(R.id.iv_pgc_profile_item)
        val tvPgcNameItem: TextView =
            itemView.findViewById(R.id.tv_pgc_name_item)
        val tvPgcDescItem: TextView =
            itemView.findViewById(R.id.tv_pgc_desc_item)
        val ivPgcAddItem: ImageView = itemView.findViewById(R.id.iv_pgc_add_item)
        fun bindData(metroData: MetroData) {
            itemView.setOnClickListener {
                TheRouter.build(NavPaths.PERSON)
                    .withString("uid",metroData.uid.toString())
                    .navigation()
            }
            Glide.with(ivPgcProfileItem.context)
                .load(metroData.avatar?.url)
                .placeholder(R.drawable.eyepetater)
                .error(R.drawable.eyepetater)
                .into(ivPgcProfileItem)
            tvPgcNameItem.text = metroData.nick
            tvPgcDescItem.text = metroData.description
        }
    }
}

object PgcDiffCallback : DiffUtil.ItemCallback<MetroData>() {
    override fun areItemsTheSame(p0: MetroData, p1: MetroData): Boolean {
        return p0.uid == p1.uid
    }

    override fun areContentsTheSame(p0: MetroData, p1: MetroData): Boolean {
        return p0.link == p1.link
    }
}