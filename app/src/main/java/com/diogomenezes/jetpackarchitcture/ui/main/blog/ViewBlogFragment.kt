package com.diogomenezes.jetpackarchitcture.ui.main.blog

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.models.BlogPost
import com.diogomenezes.jetpackarchitcture.ui.AreYouSureCallback
import com.diogomenezes.jetpackarchitcture.ui.UIMessageType
import com.diogomenezes.jetpackarchitcture.ui.UiMessage
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogStateEvent
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogStateEvent.CheckAuthorBlogPost
import com.diogomenezes.jetpackarchitcture.ui.main.blog.viewmodel.*
import com.diogomenezes.jetpackarchitcture.util.DateUtil
import com.diogomenezes.jetpackarchitcture.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.android.synthetic.main.fragment_view_blog.*

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
        subscribeObservers()
        checkIsAuthorOfBlogPost()
        stateChangeListener.expandAppBar()
        delete_button.setOnClickListener { confirmDeleteRequest() }
    }

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                deleteBlogPost()
            }

            override fun cancel() {}
        }
        uiCommunicationListener.onUIMessageReceived(
            UiMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    private fun deleteBlogPost() {
        viewModel.setStateEvent(BlogStateEvent.DeleteBlogPostEvent())
    }


    private fun checkIsAuthorOfBlogPost() {
        viewModel.setIsAuthorOfBlogPost(false)//reset

        viewModel.setStateEvent(CheckAuthorBlogPost())
    }


    fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.OnDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.getContentIfNotHandled()?.let { viewState ->
                    viewModel.setIsAuthorOfBlogPost(
                        viewState.viewBlogFields.isAuthorOfBlogPost
                    )
                }
                data.response?.peekContent()?.let { response ->
                    if (response.message.equals(SUCCESS_BLOG_DELETED)) {
                        viewModel.removeDeletedBlogPost()
                        findNavController().popBackStack()
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer
        {
            it.viewBlogFields.blogPost?.let { setBlogProperties(it) }
            if (it.viewBlogFields.isAuthorOfBlogPost) {
                adaptViewToAuthorMode()
            }


        })

    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        delete_button.visibility = View.VISIBLE
    }

    private fun setBlogProperties(blogPost: BlogPost) {
        blog_title.text = blogPost.title
        blog_author.text = blogPost.username

        var body = blogPost.body
        if (!body.isNullOrEmpty()) blog_body.text = blogPost.body

        blog_update_date.text =
            "Last update: ${DateUtil.convertLongToStringDate(blogPost.date_updated)}"

        requestManager.load(blogPost.image)
            .into(blog_image)


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        if (viewModel.isAuthorOfBlogPost())
            inflater.inflate(
                R.menu.edit_view_menu,
                menu
            )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.isAuthorOfBlogPost())
            when (item.itemId) {
                R.id.edit -> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        return super.onOptionsItemSelected(item)
    }

    fun navUpdateBlogFragment() {
        try {
            viewModel.setUpdatedBlogFields(
                viewModel.getBlogPost().title,
                viewModel.getBlogPost().body,
                viewModel.getBlogPost().image.toUri()
            )
            findNavController ().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
        } catch (e: Exception) {
            Log.e("ViewBlogFragment", "navUpdateBlogFragment :Exception: ${e.message}")
        }

    }
}