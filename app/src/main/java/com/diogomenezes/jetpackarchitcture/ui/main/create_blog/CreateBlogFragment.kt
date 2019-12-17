package com.diogomenezes.jetpackarchitcture.ui.main.create_blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.ui.*
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.state.CreateBlogStateEvent
import com.diogomenezes.jetpackarchitcture.util.Constants.Companion.BLOG_BODY
import com.diogomenezes.jetpackarchitcture.util.Constants.Companion.BLOG_IMAGE
import com.diogomenezes.jetpackarchitcture.util.Constants.Companion.BLOG_TITLE
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
        return inflater.inflate(R.layout.fragment_create_blog, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setClickListener()
        subscribeObservers()
    }

    private fun setClickListener() {
        create_blog_image.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        create_blog_image_add_update_text.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
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
                Log.i("CreateBlogFragment", "subscribeObservers: blogfields called")
                setBlogProperties(
                    newBlogFields.newBlogTitle,
                    newBlogFields.newBlogBody,
                    newBlogFields.newImageUri
                )
            }
        })
    }

    private fun setBlogProperties(title: String?, body: String?, imageUri: Uri?) {
        if (imageUri == Uri.EMPTY || imageUri == null) {
            create_blog_image.setImageDrawable(null)
            create_blog_image_add_update_text.text =
                resources.getString(R.string.touch_to_add_image)
        } else {
            create_blog_image_add_update_text.text =
                resources.getString(R.string.touch_to_change_image)
            requestManager
                .load(imageUri)
                .into(create_blog_image)
        }
        create_blog_title.setText(title)
        create_blog_body.setText(body)


    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
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

    private fun publishNewBlog() {

        if (create_blog_title.text.isNullOrEmpty() && create_blog_body.text.isNullOrEmpty()) return

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
        multiPartBody?.let { image ->
            // TODO("17/12/2019 - Possible crash after process death")
            viewModel.setStateEvent(
                CreateBlogStateEvent.CreateNewBlogEvent(
                    create_blog_title.text.toString(),
                    create_blog_body.text.toString(),
                    image
                )
            )
            stateChangeListener.hideSoftKeyboard()

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


        } ?: showErrorDialog(ERROR_MUST_SELECT_IMAGE)

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
                    viewModel.setNewBlogFields(uri = resultUri)
                }
                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.publish_content_menu -> {
                publishNewBlog()
                true
            }
            R.id.delete_content_menu -> {
                viewModel.clearNewBlogFields()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.setNewBlogFields(
            create_blog_title.text.toString(),
            create_blog_body.text.toString(),
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this, providerFactory).get(CreateBlogViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        if (savedInstanceState != null) {
            var title = ""
            var body = ""
            var uri: Uri? = null
            savedInstanceState[BLOG_TITLE]?.let { title = it as String }
            savedInstanceState[BLOG_BODY]?.let { body = it as String }
            savedInstanceState[BLOG_IMAGE]?.let { uri = it.toString().toUri() }

            viewModel.setNewBlogFields(title, body, uri)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BLOG_TITLE, create_blog_title.text.toString())
        outState.putString(BLOG_BODY, create_blog_body.text.toString())
        outState.putString(
            BLOG_IMAGE,
            viewModel.viewState.value?.blogFields?.newImageUri.toString()
        )
    }
}