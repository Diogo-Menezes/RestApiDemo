package com.diogomenezes.jetpackarchitcture.ui.main.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.models.BlogPost
import com.diogomenezes.jetpackarchitcture.ui.*
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogStateEvent
import com.diogomenezes.jetpackarchitcture.ui.main.blog.viewmodel.getBlogPost
import com.diogomenezes.jetpackarchitcture.ui.main.blog.viewmodel.onBlogPostUpdateSuccess
import com.diogomenezes.jetpackarchitcture.ui.main.blog.viewmodel.setUpdatedBlogFields
import com.diogomenezes.jetpackarchitcture.util.Constants
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_update_blog.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class UpdateBlogFragment : BaseBlogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()

        image_container.setOnClickListener { if (stateChangeListener.isStoragePermissionGranted()) pickFromGallery() }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.OnDataStateChange(dataState)

            dataState.data?.let { data ->
                data.data?.getContentIfNotHandled()?.let { viewState ->
                    viewState.viewBlogFields.blogPost?.let {
                        viewModel.onBlogPostUpdateSuccess(it).let {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.updatedBlogFields?.let {
                setBlogProperties(
                    it.updatedBlogTitle,
                    it.updatedBlogBody,
                    it.updatedImageUri
                )
            }
        })
    }

    private fun setBlogProperties(
        updatedBlogTitle: String?,
        updatedBlogBody: String?,
        updatedImageUri: Uri?
    ) {
        blog_title.setText(updatedBlogTitle)
        blog_body.setText(updatedBlogBody)

        requestManager
            .load(updatedImageUri)
            .into(blog_image)
    }

    private fun setBlogFields(blogPost: BlogPost = viewModel.getBlogPost()) {
        blog_title.setText(blogPost.title)
        blog_body.setText(blogPost.title)

        requestManager
            .load(blogPost.image)
            .into(blog_image)
    }

    private fun saveChanges() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {}

            override fun cancel() {
                return
            }
        }
        uiCommunicationListener.onUIMessageReceived(
            UiMessage(
                getString(R.string.are_you_sure_publish),
                UIMessageType.AreYouSureDialog(callback)
            )
        )


        var multiPartBody: MultipartBody.Part? = null
        viewModel.viewState.value?.updatedBlogFields?.updatedImageUri?.let { imageUri ->
            imageUri.path?.let { filePath ->
                val imageFile = File(filePath)
                Log.d("UpdateBlogFragment", "publishNewBlog : image: $imageFile")

                val requestBody = RequestBody.create(
                    MediaType.parse("image/*"),
                    imageFile
                )
                multiPartBody = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    requestBody
                )
            }
        }
        multiPartBody?.let {
            viewModel.setStateEvent(
                BlogStateEvent.UpdatedBlogPostEvent(
                    blog_title.text.toString(),
                    blog_body.text.toString(),
                    it
                )
            )
            stateChangeListener.hideSoftKeyboard()
        } ?: showErrorDialog(ErrorHandling.ERROR_MUST_SELECT_IMAGE)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                saveChanges()
                return true
            }
        }

        return super.onOptionsItemSelected(item)

    }

    override fun onPause() {
        super.onPause()
        viewModel.setUpdatedBlogFields(
            image = null,
            body = blog_body.text.toString(),
            title = blog_title.text.toString()
        )
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, Constants.GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri?) {
        context?.let {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        stateChangeListener.OnDataStateChange(
            DataState(
                Event(
                    StateError(
                        Response(
                            errorMessage, ResponseType.Dialog()
                        )
                    )
                ), Loading(false),
                Data(Event.dataEvent(null), null)
            )
        )
//        uiCommunicationListener.onUIMessageReceived(UiMessage(errorMessage, UIMessageType.Dialog()))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    } ?: showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d("CreateBlogFragment", "onActivityResult : $resultUri")
                    viewModel.setUpdatedBlogFields(image = resultUri)
                }
                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
