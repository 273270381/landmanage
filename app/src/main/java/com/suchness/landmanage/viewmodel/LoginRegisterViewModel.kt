package com.suchness.landmanage.viewmodel

import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.suchness.deeplearningapp.app.network.apiService
import com.suchness.landmanage.data.been.LoginParams
import com.suchness.landmanage.data.been.UserInfo
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel
import me.hgj.jetpackmvvm.callback.databind.BooleanObservableField
import me.hgj.jetpackmvvm.callback.databind.StringObservableField
import me.hgj.jetpackmvvm.callback.livedata.BooleanLiveData
import me.hgj.jetpackmvvm.ext.request
import me.hgj.jetpackmvvm.state.ResultState

/**
 * @author: hejunfeng
 * @date: 2021/10/8 0008
 */
class LoginRegisterViewModel : BaseViewModel() {

    var userName = StringObservableField()

    var password = StringObservableField()

    var isShowPwd = BooleanObservableField()

    var isRemeberPassword = BooleanLiveData()

    var loginResult = MutableLiveData<ResultState<UserInfo>>()

    var clearVisible = object : ObservableInt(userName){
        override fun get(): Int {
            return if (userName.get().isEmpty()){
                View.GONE
            }else{
                View.VISIBLE
            }
        }
    }

    var passwordVisible = object :ObservableInt(password){
        override fun get(): Int {
            return if(password.get().isEmpty()){
                View.GONE
            }else{
                View.VISIBLE
            }
        }
    }

    fun loginReq(userName: String, password: String){
        request(
            { apiService.login(userName,password)},loginResult,true,"正在登录中..."
        )
    }
}