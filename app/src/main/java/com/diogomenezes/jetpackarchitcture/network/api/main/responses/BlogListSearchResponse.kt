package com.diogomenezes.jetpackarchitcture.network.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BlogListSearchResponse(

    @SerializedName("results")
    @Expose
    var result: List<BlogSearchResponse>,

    @SerializedName("detail")
    @Expose
    var detail: String


) {
    override fun toString(): String {
        return "BlogListSearchResponse(result=$result, detail='$detail')"
    }
}