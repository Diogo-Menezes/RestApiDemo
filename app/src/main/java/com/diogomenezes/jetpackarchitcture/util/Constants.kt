package com.diogomenezes.jetpackarchitcture.util

class Constants {

    companion object {

        //Saved states keys
        const val USER_ACCOUNT = "user"
        const val BLOG_POST = "blog_post"
        const val BLOG_LIST = "blog_list"
        const val BLOG_TITLE = "blog_title"
        const val BLOG_BODY = "blog_body"
        const val BLOG_IMAGE = "blog_image"

        const val LOGOUT = "logout"

        //NETWORK
        const val BASE_URL = "https://open-api.xyz/api/"
        const val PASSWORD_RESET_URL: String = "https://open-api.xyz/password_reset/"
        const val NETWORK_TIMEOUT = 6000L
        const val TESTING_NETWORK_DELAY = 3000L // fake network delay for testing
        const val TESTING_CACHE_DELAY = 0L // fake cache delay for testing
        const val PAGINATION_PAGE_SIZE = 10

        //PERMISSIONS
        const val GALLERY_REQUEST_CODE = 201
        const val PERMISSIONS_REQUEST_READ_STORAGE = 301
        const val CROP_IMAGE_INTENT_CODE = 401
    }
}