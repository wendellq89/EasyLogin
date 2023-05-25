package com.wendell.thirdlogin

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.wendell.thirdlogin.LoginType.TwitterLogin

/**
 * Twitter 登录授权处理
 * @author Wendell
 * 2023/5/18 11:12
 */
class TwitterLoginClientFactory : ThirdPartyLoginClientFactory {
    lateinit var twitterObserver: TwitterLifecycleObserver
    private var _loginCallback: LoginCallback? = null

    override fun create(activity: ComponentActivity): ThirdPartyLoginClient {
        //注册StartActivityForResult 替代废弃的需要在activity中使用的onActivityResult
        twitterObserver = TwitterLifecycleObserver(activity.activityResultRegistry)
        activity.lifecycle.addObserver(twitterObserver)
        return object : ThirdPartyLoginClient {

            override fun login(loginCallback: LoginCallback) {
                _loginCallback = loginCallback
                val intent = Intent(activity, TwitterAuthorizationActivity::class.java)
                twitterObserver.twitterRequest.launch(intent)
            }

        }
    }

    inner class TwitterLifecycleObserver(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
        //twitter配置
        lateinit var twitterRequest: ActivityResultLauncher<Intent>

        override fun onCreate(owner: LifecycleOwner) {
            //此处代码参考androidx.activity.ComponentActivity.registerForActivityResult的实现，解决没有activity的情况下无法调用registerForActivityResult
            twitterRequest = registry.register("activity_rq#" + "twitter_login", owner, StartActivityForResult()) { result ->
                result.data?.let {
                    var authorizationInfo = it.getParcelableExtra<AuthorizationInfo>("authorizationInfo")
                    if (authorizationInfo != null) {
                        _loginCallback?.onSuccess(authorizationInfo, TwitterLogin)
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