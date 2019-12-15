package com.diogomenezes.jetpackarchitcture.ui.main.blog

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.models.BlogPost
import com.diogomenezes.jetpackarchitcture.database.BlogQueryUtils.Companion.BLOG_FILTER_DATE_UPDATED
import com.diogomenezes.jetpackarchitcture.database.BlogQueryUtils.Companion.BLOG_FILTER_USERNAME
import com.diogomenezes.jetpackarchitcture.database.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.diogomenezes.jetpackarchitcture.database.BlogQueryUtils.Companion.BLOG_ORDER_DESC
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogViewState
import com.diogomenezes.jetpackarchitcture.ui.main.blog.viewmodel.*
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling
import com.diogomenezes.jetpackarchitcture.util.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_blog.*


class BlogFragment : BaseBlogFragment(), BlogListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var recyclerAdapter: BlogListAdapter
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        swipe_refresh.setOnRefreshListener(this)

        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        initRecView()
        subscribeObservers()

        if (savedInstanceState == null) viewModel.loadFirstPage()


    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                handlePagination(it)
                stateChangeListener.OnDataStateChange(it)

            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {
                recyclerAdapter.apply {
                    preloadGlideImages(requestManager, viewState.blogFields.blogList)

                    submitList(
                        list = viewState.blogFields.blogList,
                        isQueryExhausted = viewState.blogFields.isQueryExhausted
                    )
                }

            }

        })
    }

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
        }

        // ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                || actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val searchQuery = v.text.toString()

                viewModel.setQuery(searchQuery).let {
                    onBlogSearchOrFilter()
                }
            }
            true
        }

        // SEARCH BUTTON CLICKED (in toolbar)
        val searchButton = searchView.findViewById(R.id.search_go_btn) as View
        searchButton.setOnClickListener {
            val searchQuery = searchPlate.text.toString()

            viewModel.setQuery(searchQuery).let {
                onBlogSearchOrFilter()
            }

        }
    }

    private fun onBlogSearchOrFilter() {
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        blog_post_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }

    private fun handlePagination(dataState: DataState<BlogViewState>) {

        //handle incoming data from dataState
        dataState.data?.let {
            it.data?.let {
                it.getContentIfNotHandled()?.let {
                    viewModel.handleIncomingBlogListDate(it)
                }
            }
        }


        // Check for pagination end (no more results)
        // must do this b/c server will return an ApiErrorResponse if page is not valid,
        // -> meaning there is no more data.

        dataState.error?.let { event ->
            event.peekContent().response.message?.let {
                if (ErrorHandling.isPaginationDone(it)) {
                    // handle the error message event so it doesn't display in UI
                    event.getContentIfNotHandled()

                    // set query exhausted to update RecyclerView with
                    // "No more results..." list item

                    viewModel.setQueryExhausted(true)
                }

            }
        }

    }


    private fun initRecView() {
        blog_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@BlogFragment.context)
            val topSpacingItemDecoration = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingItemDecoration)
            addItemDecoration(topSpacingItemDecoration)


            recyclerAdapter =
                BlogListAdapter(
                    requestManager = requestManager,
                    interaction = this@BlogFragment
                )


            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(
                            "BlogFragment",
                            "onScrolled (line 95): attempting to load next page..."
                        )
                        viewModel.nextPage()
                    }
                    super.onScrolled(recyclerView, dx, dy)
                }
            })

            adapter = recyclerAdapter
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_filter_settings) {
            showFilterOptions()
            return true
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        //clear references (can leak memory)
        blog_post_recyclerview.adapter = null
    }

    override fun onItemSelected(position: Int, item: BlogPost) {
        viewModel.setBlogPost(item)
        findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)
    }

    override fun onRefresh() {
        onBlogSearchOrFilter()
        swipe_refresh.isRefreshing = false

    }


    private fun showFilterOptions() {
        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_blog_filter)
            val view = dialog.getCustomView()

            val filter = viewModel.getFilter()


            if (filter.equals(BLOG_FILTER_DATE_UPDATED)) {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_date)
            } else {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_author)
            }

            val order = viewModel.getOrder()
            if (order.equals(BLOG_ORDER_ASC)) {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.order_asc)
            } else {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.order_desc)
            }
            val initialFilter = dialog.getCustomView().findViewById<RadioButton>(
                dialog.getCustomView().findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
            )
            val initalOrder = dialog.getCustomView().findViewById<RadioButton>(
                dialog.getCustomView().findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
            )
            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                val selectedFilter = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView().findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
                )
                val selectedOrder = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView().findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
                )
                if (initalOrder == selectedOrder && initialFilter == selectedFilter) {
                    dialog.dismiss()
                    return@setOnClickListener
                }

                var filter = BLOG_FILTER_DATE_UPDATED
                if (selectedFilter.text.toString().equals(getString(R.string.filter_author))) {
                    filter = BLOG_FILTER_USERNAME
                }

                var order = BLOG_ORDER_ASC
                if (selectedOrder.text.toString().equals(getString(R.string.filter_desc))) order =
                    BLOG_ORDER_DESC

                viewModel.saveFilterOptions(filter, order).let {
                    viewModel.setBlogOrder(order)
                    viewModel.setBlogFilter(filter)
                    onBlogSearchOrFilter()
                }
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
