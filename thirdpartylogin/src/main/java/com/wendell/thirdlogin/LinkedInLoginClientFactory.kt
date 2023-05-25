package com.wendell.thirdlogin

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.wendell.thirdlogin.LoginType.LinkedInLogin

/**
 * LinkedIn 登录授权处理
 * @author Wendell
 * 2023/5/22 10:04
 */
class LinkedInLoginClientFactory : ThirdPartyLoginClientFactory {
    lateinit var linkedInObserver: LinkedInLifecycleObserver
    private var _loginCallback: LoginCallback? = null

    override fun create(activity: ComponentActivity): ThirdPartyLoginClient {
        //注册StartActivityForResult 替代废弃的需要在activity中使用的onActivityResult
        linkedInObserver = LinkedInLifecycleObserver(activity.activityResultRegistry)
        activity.lifecycle.addObserver(linkedInObserver)
        return object : ThirdPartyLoginClient {

            override fun login(loginCallback: LoginCallback) {
                _loginCallback = loginCallback
                val intent = Intent(activity, LinkedInAuthorizationActivity::class.java)
                linkedInObserver.twitterRequest.launch(intent)
            }

        }
    }

    inner class LinkedInLifecycleObserver(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
        //twitter配置
        lateinit var twitterRequest: ActivityResultLauncher<Intent>

        override fun onCreate(owner: LifecycleOwner) {
            //此处代码参考androidx.activity.ComponentActivity.registerForActivityResult的实现，解决没有activity的情况下无法调用registerForActivityResult
            twitterRequest = registry.register("activity_rq#" + "linkedin_login", owner, StartActivityForResult()) { result ->
                result.data?.let {
                    var authorizationInfo = it.getParcelableExtra<AuthorizationInfo>("authorizationInfo")
                    if (authorizationInfo != null) {
                        _loginCallback?.onSuccess(authorizationInfo, LinkedInLogin)
                        return@let
                    }
                    var errorInfo = it.getParcelableExtra<ErrorInfo>("authorizationInfo")
                    if (errorInfo != null) {
                        _loginCallback?.onError(errorInfo)
                    }

                }
            }
        }
    }
}