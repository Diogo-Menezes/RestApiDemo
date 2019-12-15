package com.diogomenezes.jetpackarchitcture.ui.main.create_blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.ui.*
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.state.CreateBlogStateEvent
import com.diogomenezes.jetpackarchitcture.util.Constants
import com.diogomenezes.jetpackarchitcture.util.Constants.Companion.GALLERY_REQUEST_CODE
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling.Companion.ERROR_MUST_SELECT_IMAGE
import com.diogomenezes.jetpackarchitcture.util.SuccessHandling
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_blog.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class CreateBlogFragment : BaseCreateBlogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        blog_image.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        update_textview.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.OnDataStateChange(dataState)

            dataState.data?.let { data ->
                data.response?.let { event ->
                    event.peekContent().let { response ->
                        response.message?.let { message ->
                            if (message.equals(SuccessHandling.SUCCESS_BLOG_CREATED)) {
                                viewModel.clearNewBlogFields()

                            }
                        }
                    }
                }
            }

        })
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.blogFields.let { newBlogFields ->
                setBlogProperties(
                    newBlogFields.newBlogTitle,
                    newBlogFields.newBlogBody,
                    newBlogFields.newImageUri
                )
            }
        })
    }

    private fun setBlogProperties(title: String?, body: String?, imageUri: Uri?) {
        imageUri?.let {
            requestManager
                .load(imageUri)
                .into(blog_image)
        }
        blog_title.setText(title)
        blog_body.setText(body)
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
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    } ?: showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d("CreateBlogFragment", "onActivityResult : $resultUri")
                    viewModel.setNewBlogFields(uri = resultUri)
                }
                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }




    private fun publishNewBlog() {

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
        viewModel.getNewImageUri()?.let { imageUri ->
            imageUri.path?.let { filePath ->
                val imageFile = File(filePath)
                Log.d("CreateBlogFragment", "publishNewBlog : image: $imageFile")

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
                CreateBlogStateEvent.CreateNewBlogEvent(
                    blog_title.text.toString(),
                    blog_body.text.toString(),
                    it
                )
            )
            stateChangeListener.hideSoftKeyboard()
        } ?: showErrorDialog(ERROR_MUST_SELECT_IMAGE)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.publish) {
            publishNewBlog()
            return true
        } else return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()

        viewModel.setNewBlogFields(
            blog_title.text.toString(),
            blog_body.text.toString(),
            null
        )
    }
}