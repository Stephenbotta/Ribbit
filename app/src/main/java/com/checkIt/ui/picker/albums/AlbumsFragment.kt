package com.checkIt.ui.picker.albums

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.checkIt.R
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.picker.PickerViewModel
import com.checkIt.ui.picker.media.MediaFragment
import com.checkIt.ui.picker.models.PickerAlbum
import kotlinx.android.synthetic.main.fragment_albums.*

class AlbumsFragment : BaseFragment(), AlbumsAdapter.Callback {
    companion object {
        const val TAG = "AlbumsFragment"
    }

    private lateinit var albumsAdapter: AlbumsAdapter

    override fun getFragmentLayoutResId(): Int {
        return R.layout.fragment_albums
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProviders.of(requireActivity())[PickerViewModel::class.java]
        albumsAdapter = AlbumsAdapter(view.context, Glide.with(this), this)
        rvAlbums.setHasFixedSize(true)
        rvAlbums.adapter = albumsAdapter

        viewModel.getAlbumsLiveData().observe(this, Observer { albums ->
            if (albums == null || albums.isEmpty()) {
                viewSwitcher.displayedChild = 1
            } else {
                viewSwitcher.displayedChild = 2
                albumsAdapter.displayAlbums(albums)
            }
        })
    }

    override fun onAlbumClicked(album: PickerAlbum) {
        val fragmentManager = fragmentManager
        if (fragmentManager != null && fragmentManager.findFragmentByTag(MediaFragment.TAG) == null) {
            val fragment = MediaFragment.newInstance(album.bucketId)
            fragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, fragment, MediaFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }
    }
}