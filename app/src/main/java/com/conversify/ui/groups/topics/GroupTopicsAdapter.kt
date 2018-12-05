package com.conversify.ui.groups.topics

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.inflate
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_group_topic.view.*

class GroupTopicsAdapter(private val glide: GlideRequests,
                         private val callback: (InterestDto) -> Unit) : RecyclerView.Adapter<GroupTopicsAdapter.ViewHolderTopic>() {
    private val topics = mutableListOf<InterestDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTopic {
        return ViewHolderTopic(parent.inflate(R.layout.item_group_topic), glide, callback)
    }

    override fun getItemCount(): Int = topics.size

    override fun onBindViewHolder(holder: ViewHolderTopic, position: Int) {
        holder.bind(topics[position])
    }

    fun displayTopics(topics: List<InterestDto>) {
        this.topics.addAll(topics)
        notifyDataSetChanged()
    }

    class ViewHolderTopic(itemView: View,
                          private val glide: GlideRequests,
                          callback: (InterestDto) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private lateinit var topic: InterestDto

        init {
            itemView.setOnClickListener { callback(topic) }
        }

        fun bind(topic: InterestDto) {
            this.topic = topic

            glide.load(topic.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivTopic)
            itemView.tvTopic.text = topic.name
        }
    }
}