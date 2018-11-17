package com.conversify.ui.createvenue

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.hideKeyboard
import com.conversify.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_create_venue.*

class CreateVenueFragment : BaseFragment() {
    companion object {
        const val TAG = "CreateVenueFragment"
        private const val ARGUMENT_CATEGORY = "ARGUMENT_CATEGORY"

        fun newInstance(category: InterestDto): Fragment {
            val fragment = CreateVenueFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_CATEGORY, category)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var category: InterestDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_create_venue

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        category = arguments?.getParcelable(ARGUMENT_CATEGORY) as InterestDto
        tvCategory.text = category.name
        clUploadDocument.setOnClickListener { }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_fragment_create_venue, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuCreateVenue) {
            etVenueTitle.hideKeyboard()
            etVenueTitle.clearFocus()
            etVenueTags.clearFocus()
            etVenueOwnerName.clearFocus()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}