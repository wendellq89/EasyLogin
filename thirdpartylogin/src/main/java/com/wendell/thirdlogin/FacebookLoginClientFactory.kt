package com.wendell.thirdlogin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.GraphRequest.GraphJSONObjectCallback
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.wendell.thirdlogin.LoginType.FacebookLogin
import org.json.JSONObject


/**
 * Facebook 登录授权处理
 * @author Wendell
 * 2023/5/17 14:33
 */
class FacebookLoginClientFactory : ThirdPartyLoginClientFactory {
    lateinit var facebookObserver: FacebookLifecycleObserver
    private var _loginCallback: LoginCallback? = null

    override fun create(activity: ComponentActivity): ThirdPartyLoginClient {
        FacebookSdk.sdkInitialize(activity.applicationContext)
        facebookObserver = FacebookLifecycleObserver()
        activity.lifecycle.addObserver(facebookObserver)


        return object : ThirdPartyLoginClient {
            override fun login(loginCallback: LoginCallback) {
                _loginCallback = loginCallback
                LoginManager.getInstance().logInWithReadPermissions(activity, facebookObserver.callbackManager, listOf("email", "public_profile"))
            }
        }
    }

    inner class FacebookLifecycleObserver : DefaultLifecycleObserver {
        //facebook配置
        lateinit var callbackManager: CallbackManager

        override fun onCreate(owner: LifecycleOwner) {
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.d("facebookLogin", "onCancel")
                }

                override fun onError(e: FacebookException) {
                    Log.d("facebookLogin", e.toString())
                    _loginCallback?.onError(error = ErrorInfo("", e.message ?: ""))
                }

                override fun onSuccess(result: LoginResult) {
                    Log.d("facebookLogin", result.toString())
                    // https://developers.facebook.com/docs/graph-api/overview/#fields  存取凭证
                    AccessToken.setCurrentAccessToken(result.accessToken)
                    // https://developers.facebook.com/docs/android/graph 进一步获取其他信息
                    val request = GraphRequest.newMeRequest(
                        result.accessToken,
                        object : GraphJSONObjectCallback {

                            override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
                                //具体返回数据https://developers.facebook.com/docs/graph-api/reference/user
                                obj?.let {
                                    val id = it.getString("id")
                                    val email = it.getString("email")
                                    val firstName = it.getString("first_name")
                                    val lastName = it.getString("last_name")
                                    Log.d(
                                        "authorizationInfo", AuthorizationInfo(
                                            id ?: "", email ?: "",
                                            firstName ?: "", lastName ?: ""
                                        )
                                            .toString()
                                    )
                                    _loginCallback?.onSuccess(
                                        AuthorizationInfo(
                                            id ?: "", email ?: "",
                                            firstName ?: "", lastName ?: ""
                                        ), FacebookLogin
                                    )

                                }


                            }
                        })
                    val parameters = Bundle()
                    parameters.putString("fields", "id,first_name,last_name,email")
                    request.parameters = parameters
                    request.executeAsync()
                }
            })

        }


    }
}