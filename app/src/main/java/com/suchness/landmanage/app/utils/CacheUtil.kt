package com.suchness.landmanage.app.utils

import android.text.TextUtils
import com.google.gson.Gson
import com.suchness.landmanage.data.been.UserInfo
import com.tencent.mmkv.MMKV

/**
 * @author: hejunfeng
 * @date: 2021/9/24 0024
 */
object CacheUtil {
    /**
     * 获取保存的账户信息
     */
    fun getUser(): UserInfo? {
        val kv = MMKV.mmkvWithID("app")
        val userStr = kv.decodeString("user")
        return if (TextUtils.isEmpty(userStr)) {
            null
        } else {
            Gson().fromJson(userStr, UserInfo::class.java)
        }
    }

    fun getUserName(): String?{
        val kv = MMKV.mmkvWithID("app")
        var userName = kv.decodeString("userName")
        return if (TextUtils.isEmpty(userName)){
            ""
        }else{
            userName
        }
    }

    fun setUserName(userName: String){
        val kv = MMKV.mmkvWithID("app")
        kv.encode("userName",userName)
    }

    fun getPassWord(): String?{
        val kv = MMKV.mmkvWithID("app")
        var password = kv.decodeString("password")
        return if (TextUtils.isEmpty(password)){
            ""
        }else{
            password
        }
    }

    fun setPassword(password: String){
        val kv = MMKV.mmkvWithID("app")
        kv.encode("password",password)
    }

    fun setRemenberPassword(remenber : Boolean){
        val kv = MMKV.mmkvWithID("app")
        kv.encode("rememberer",remenber)
    }

    fun getRemenberPassword(): Boolean{
        val kv = MMKV.mmkvWithID("app")
        return kv.decodeBool("rememberer",false)
    }

    /**
     * 是否已经登录
     */
    fun isLogin(): Boolean {
        val kv = MMKV.mmkvWithID("app")
        return kv.decodeBool("login", false)
    }

    /**
     * 设置是否已经登录
     */
    fun setIsLogin(isLogin: Boolean) {
        val kv = MMKV.mmkvWithID("app")
        kv.encode("login", isLogin)
    }



    /**
     * 设置账户信息
     */
    fun setUser(userResponse: UserInfo?) {
        val kv = MMKV.mmkvWithID("app")
        if (userResponse == null) {
            kv.encode("user", "")
            setIsLogin(false)
        } else {
            kv.encode("user", Gson().toJson(userResponse))
            setIsLogin(true)
        }

    }



    /**
     * 是否是第一次登陆
     */
    fun isFirst(): Boolean {
        val kv = MMKV.mmkvWithID("app")
        return kv.decodeBool("first", true)
    }
    /**
     * 是否是第一次登陆
     */
    fun setFirst(first:Boolean): Boolean {
        val kv = MMKV.mmkvWithID("app")
        return kv.encode("first", first)
    }

    fun setTifNum(num : Int): Boolean{
        val kv = MMKV.mmkvWithID("tifnum")
        return kv.encode("tif",num)
    }

    fun setFileNum(num : Int): Boolean{
        val kv = MMKV.mmkvWithID("filenum")
        return kv.encode("file",num)
    }

    fun getTifNum():Int{
        val kv = MMKV.mmkvWithID("tifnum")
        val num = kv.decodeInt("tif")
        return num
    }

    fun getFileNum():Int{
        val kv = MMKV.mmkvWithID("filenum")
        val num = kv.decodeInt("file")
        return num
    }
}