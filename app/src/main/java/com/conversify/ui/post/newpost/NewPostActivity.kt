package com.conversify.ui.post.newpost

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.extensions.hideKeyboard
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.AppConstants
import kotlinx.android.synthetic.main.activity_new_post.*

class NewPostActivity : BaseActivity() {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        private const val EXTRA_GROUP_POST = "EXTRA_GROUP_POST"
        private const val EXTRA_GROUP = "EXTRA_GROUP"

        fun getStartIntent(context: Context, group: GroupDto, flag: Int): Intent {
            return Intent(context, NewPostActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
                    .putExtra(EXTRA_GROUP, group)
        }


        fun getStartIntentForEdit(context: Context, groupPost: GroupPostDto, flag: Int): Intent {
            return Intent(context, NewPostActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
                    .putExtra(EXTRA_GROUP_POST, groupPost)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

        btnBack.setOnClickListener {
            it.hideKeyboard()
            onBackPressed()
        }

        val flag = intent.getIntExtra(EXTRA_FLAG, 0)
        when (flag) {
            AppConstants.REQ_CODE_CREATE_NEW_POST -> {
                val group = intent.getParcelableExtra<GroupDto>(EXTRA_GROUP)
                navigateToNewPostFragment(group)
            }

            AppConstants.REQ_CODE_EDIT_POST -> {
                val groupId = intent.getParcelableExtra<GroupPostDto>(EXTRA_GROUP_POST)
                navigateToEditPostFragment(groupId)
            }

            else -> {
                if (savedInstanceState == null) {
                    // If group count is 0, then directly show new post fragment otherwise show choose group fragment.
                    if (UserManager.getGroupCount() == 0) {
                        supportFragmentManager.beginTransaction()
                                .add(R.id.flContainer, NewPostFragment(), NewPostFragment.TAG)
                                .commit()
                    } else {
                        changeBackButtonText(getString(R.string.home))
                        supportFragmentManager.beginTransaction()
                                .add(R.id.flContainer, ChooseGroupFragment(), ChooseGroupFragment.TAG)
                                .commit()
                    }
                }
            }
        }
    }

    private fun navigateToNewPostFragment(group: GroupDto?) {
        supportFragmentManager.beginTransaction()
                .add(R.id.flContainer, NewPostFragment.newInstance(group), NewPostFragment.TAG)
                .commit()
    }

    private fun navigateToEditPostFragment(groupId: GroupPostDto) {
        supportFragmentManager.beginTransaction()
                .add(R.id.flContainer, NewPostFragment.newInstance(groupId), NewPostFragment.TAG)
                .commit()
    }

    fun changeBackButtonText(text: String) {
        btnBack.text = text
    }
}