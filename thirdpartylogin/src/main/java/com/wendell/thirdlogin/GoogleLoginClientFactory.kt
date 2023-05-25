package com.wendell.thirdlogin

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.wendell.thirdlogin.LoginType.GoogleLogin
import com.wendell.thirdpartylogin.R
import getFirstAndLastName


/**
 * https://firebase.google.com/docs/auth/android/google-signin?hl=zh-cn
 * https://developers.google.com/identity/one-tap/android/get-saved-credentials?hl=zh-cn
 * Google登录授权处理
 * @author Wendell
 * 2023/5/17 11:32
 */
class GoogleLoginClientFactory : ThirdPartyLoginClientFactory {
    lateinit var googleObserver: GoogleLifecycleObserver
    private var _loginCallback: LoginCallback? = null

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var _activity: Activity
    private lateinit var auth: FirebaseAuth

    override fun create(activity: ComponentActivity): ThirdPartyLoginClient {
        _activity = activity
        //注册StartActivityForResult 替代废弃的需要在activity中使用的onActivityResult
        googleObserver = GoogleLifecycleObserver(activity.activityResultRegistry)
        activity.lifecycle.addObserver(googleObserver)

        oneTapClient = Identity.getSignInClient(activity)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(activity.getString(R.string.google_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
//            .setAutoSelectEnabled(true)
            .build()

        return object : ThirdPartyLoginClient {

            override fun login(loginCallback: LoginCallback) {
                _loginCallback = loginCallback
                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(activity) { result ->
                        //注意，官方的demo是使用旧的startIntentSenderForResult，而result只返回了pendingIntent，需要自己用IntentSenderRequest包装一下
                        try {
                            googleObserver.googleRequest.launch(
                                IntentSenderRequest
                                    .Builder(result.pendingIntent)
                                    .build()
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.d("googleLogin", "Couldn't start One Tap UI: ${e.localizedMessage}")
                        }
                    }
                    .addOnFailureListener(activity) { e ->
                        Log.d("googleLogin", e.localizedMessage)
                        _loginCallback?.onError(error = ErrorInfo("", e.message ?: ""))
                    }
            }

        }
    }

    inner class GoogleLifecycleObserver(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
        //google配置
        lateinit var googleRequest: ActivityResultLauncher<IntentSenderRequest>

        override fun onCreate(owner: LifecycleOwner) {
            //此处代码学习androidx.activity.ComponentActivity.registerForActivityResult的实现，解决没有activity的情况下无法调用registerForActivityResult
            //注意Google有些特别，官方demo是使用startIntentSenderForResult
            googleRequest = registry.register("activity_rq#" + "google_login", owner, StartIntentSenderForResult()) { result ->
                result.data?.let {
                    handleResult(it)
                }

            }
            auth = Firebase.auth
        }
    }

    private fun handleResult(data: Intent) {
        try {

            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            when {
                idToken != null -> {

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(_activity) { task ->
                            if (task.isSuccessful) {

                                auth.currentUser?.let {
                                    val names = it.displayName?.getFirstAndLastName() ?: Pair("", "")
                                    val authorizationInfo = AuthorizationInfo(
                                        it.uid, it.email ?: "",
                                        names.first, names.second
                                    )
                                    _loginCallback?.onSuccess(
                                        authorizationInfo, GoogleLogin
                                    )
                                }


                            }
                        }
                }

                else -> {
                    Log.d("googleLogin", "No ID token!")
                }
            }
        } catch (e: ApiException) {
            Log.w("googleLogin", "signInResult:failed message=" + e.message)
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    Log.d("googleLogin", "One-tap dialog was closed.")
                }

                CommonStatusCodes.NETWORK_ERROR -> {
                    Log.d("googleLogin", "One-tap encountered a network error.")
                    _loginCallback?.onError(error = ErrorInfo(e.statusCode.toString(), e.message ?: ""))
                }

                else -> {
                    _loginCallback?.onError(error = ErrorInfo(e.statusCode.toString(), e.message ?: ""))
                }
            }

        }
    }


}