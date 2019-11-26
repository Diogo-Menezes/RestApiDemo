package com.diogomenezes.jetpackarchitcture.ui.main.blog

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.diogomenezes.jetpackarchitcture.R

class ViewBlogFragment : BaseBlogFragment() {

    private val isAuthorized = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        if (isAuthorized) inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (isAuthorized) when (item.itemId) {
            R.id.edit -> {
                navUpdateBlogFragment()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun navUpdateBlogFragment() {
        findNavController().navigate(R.id.action_viewBlogFragment_to_home)
    }
}