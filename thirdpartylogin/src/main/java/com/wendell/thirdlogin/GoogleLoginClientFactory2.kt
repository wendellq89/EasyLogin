package com.wendell.thirdlogin

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.wendell.thirdlogin.LoginType.GoogleLogin
import com.wendell.thirdpartylogin.R


/**
 * 旧GoogleSignIn方案，可以在部分不支持的设备上唤起谷歌账号，安全性较低
 * @author Wendell
 * 2023/5/31 9:56
 */
class GoogleLoginClientFactory2 : ThirdPartyLoginClientFactory {
    lateinit var googleObserver: GoogleLifecycleObserver
    private var _loginCallback: LoginCallback? = null

    private lateinit var signInRequest: GoogleSignInClient
    private lateinit var _activity: Activity

    override fun create(activity: ComponentActivity): ThirdPartyLoginClient {
        _activity = activity
        //注册StartActivityForResult 替代废弃的需要在activity中使用的onActivityResult
        googleObserver = GoogleLifecycleObserver(activity.activityResultRegistry)
        activity.lifecycle.addObserver(googleObserver)
        val gso = Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(activity.getString(R.string.google_client_id))
            .requestEmail()
            .build()
        signInRequest = GoogleSignIn.getClient(activity, gso);


        return object : ThirdPartyLoginClient {

            override fun login(loginCallback: LoginCallback) {
                _loginCallback = loginCallback
                val signInIntent: Intent = signInRequest.getSignInIntent()
                googleObserver.googleRequest.launch(signInIntent)
            }

        }
    }

    inner class GoogleLifecycleObserver(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
        //google配置
        lateinit var googleRequest: ActivityResultLauncher<Intent>

        override fun onCreate(owner: LifecycleOwner) {
            //此处代码学习androidx.activity.ComponentActivity.registerForActivityResult的实现，解决没有activity的情况下无法调用registerForActivityResult
            //注意Google有些特别，官方demo是使用startIntentSenderForResult
            googleRequest = registry.register("activity_rq#" + "google_login", owner, StartActivityForResult()) { result ->
                result.data?.let {
                    val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it)
                    handleResult(task)
                }

            }
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {

        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            account?.let {
                val authorizationInfo = AuthorizationInfo(
                    it.id ?: "", it.email ?: "",
                    it.givenName ?: "", it.familyName ?: ""
                )
                Log.d("authorizationInfo", authorizationInfo.toString())
                _loginCallback?.onSuccess(
                    authorizationInfo, GoogleLogin
                )
            }
        } catch (e: ApiException) {
            // 登录失败
            Log.d("googleLogin", "signInResult:failed code=" + e.statusCode)
            _loginCallback?.onError(error = ErrorInfo(e.statusCode.toString(), e.message ?: ""))
        }
    }


}