package com.conversify.ui.groups.groupposts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_group_posts.*

class GroupPostsActivity : BaseActivity() {
    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"

        fun start(context: Context, group: GroupDto) {
            context.startActivity(Intent(context, GroupPostsActivity::class.java)
                    .putExtra(EXTRA_GROUP, group))
        }
    }

    private val group by lazy { intent.getParcelableExtra<GroupDto>(EXTRA_GROUP) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_posts)

        tvTitle.text = group.name
        ivFavourite.setImageResource(if (group.isMember == true) {
            R.drawable.ic_star_selected
        } else {
            R.drawable.ic_star_normal
        })

        btnBack.setOnClickListener { onBackPressed() }
    }
}