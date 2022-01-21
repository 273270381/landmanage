package com.suchness.landmanage.data.been

import me.hgj.jetpackmvvm.network.BaseResponse

/**
 * @author: hejunfeng
 * @date: 2021/10/8 0008
 */
data class ApiResponse<T> (val code: Int,val msg: String,val data: T) : BaseResponse<T>(){
    override fun isSucces(): Boolean {
        return code == 200
    }

    override fun getResponseData(): T {
        return data
    }

    override fun getResponseCode(): Int {
        return code
    }

    override fun getResponseMsg(): String {
        return msg
    }

}