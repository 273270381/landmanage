package com.suchness.landmanage.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import com.gyf.barlibrary.ImmersionBar
import com.gyf.barlibrary.ImmersionFragment
import com.gyf.barlibrary.ImmersionOwner
import com.gyf.barlibrary.SimpleImmersionFragment
import com.suchness.deeplearningapp.app.base.BaseFragment
import com.suchness.landmanage.R
import com.suchness.landmanage.app.App
import com.suchness.landmanage.app.ext.init
import com.suchness.landmanage.app.ext.showMessage
import com.suchness.landmanage.app.utils.CacheUtil
import com.suchness.landmanage.databinding.FragmentLoginBinding
import com.suchness.landmanage.viewmodel.LoginRegisterViewModel
import com.videogo.openapi.EZOpenSDK
import kotlinx.android.synthetic.main.include_toolbar.*
import me.hgj.jetpackmvvm.ext.nav
import me.hgj.jetpackmvvm.ext.navigateAction
import me.hgj.jetpackmvvm.ext.parseState

/**
 * @author: hejunfeng
 * @date: 2021/9/24 0024
 */
class LoginFragment : BaseFragment<LoginRegisterViewModel, FragmentLoginBinding>(){


    override fun layoutId(): Int {
        return R.layout.fragment_login
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.viewmodel = mViewModel;
        mDatabind.click = ProxyClick();

//        var tf = Typeface.createFromAsset(context?.assets,"fonts/PangMenZhengDaoBiaoTiTi-1.ttf")
//        mDatabind.title.setTypeface(tf)

//        toolbar.run {
//            init("登陆")
//        }

//        App.appViewModel?.appColor?.value?.let {
//            //SettingUtil.setShapColor(btn_login, it)
//            toolbar.setBackgroundColor(it)
//        }
        mViewModel.userName.set(CacheUtil.getUserName())
        mViewModel.isRemeberPassword.value = CacheUtil.getRemenberPassword()
        if (CacheUtil.getRemenberPassword()){
            mViewModel.password.set(CacheUtil.getPassWord())
        }
    }

    override fun createObserver() {
        mViewModel.isRemeberPassword.observe(viewLifecycleOwner, Observer {
            CacheUtil.setRemenberPassword(it)
        })

        mViewModel.loginResult.observe(viewLifecycleOwner, Observer { rs ->
            parseState(rs,{
                //登录成功 通知账户数据发生改变鸟
                CacheUtil.setUser(it)
                CacheUtil.setIsLogin(true)
                CacheUtil.setUserName(mViewModel.userName.get())
                CacheUtil.setPassword(mViewModel.password.get())
                App.appViewModel?.userInfo?.value = it
                val intent = Intent()
                intent.action = "com.action.OAUTH_SUCCESS_ACTION"
                activity?.sendBroadcast(intent)
                EZOpenSDK.getInstance().setAccessToken(it.accessToken)
                //跳转主页
                nav().navigateAction(R.id.action_loginFragment_to_homeFragment)
            }, {
                //登录失败
                mViewModel.password.set("")
                showMessage(it.errorMsg)
            }
            )
        })
        super.createObserver()
    }

    inner class ProxyClick {
        fun clear() {
            mViewModel.userName.set("")
        }

        fun login() {
            when {
                mViewModel.userName.get().isEmpty() -> showMessage("请填写账号")
                mViewModel.password.get().isEmpty() -> showMessage("请填写密码")
                else -> mViewModel.loginReq(mViewModel.userName.get(), mViewModel.password.get())
            }
        }

        var onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                mViewModel.isShowPwd.set(isChecked)
            }

        var toggleRemenberPassword = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            mViewModel.isRemeberPassword.postValue(isChecked)
        }
    }



}