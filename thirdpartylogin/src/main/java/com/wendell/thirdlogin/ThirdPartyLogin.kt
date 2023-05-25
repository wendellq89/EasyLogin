package com.wendell.thirdlogin

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.wendell.thirdlogin.LoginType.FacebookLogin
import com.wendell.thirdlogin.LoginType.GoogleLogin
import com.wendell.thirdlogin.LoginType.LinkedInLogin
import com.wendell.thirdlogin.LoginType.TwitterLogin


/**
 * 把所有登录逻辑一起实现，如果单独使用，自己可以各自单独初始化和调用login方法
 * @author Wendell
 * 2023/5/19 11:23
 */
object ThirdPartyLogin {
    private var googleLoginClient: ThirdPartyLoginClient? = null
    private var facebookLoginClient: ThirdPartyLoginClient? = null
    private var twitterLoginClient: ThirdPartyLoginClient? = null
    private var linkedInLoginClient: ThirdPartyLoginClient? = null

    /**
     * 由于很多第三方需要获取startActivityForResult结果，但是为了不在具体调用方硬编码onActivityResult，这里使用新的Activity Results API来处理，
     * Activity Results API要求协议需要在在OnCreate中定义
     */
    fun initOnCreate(activity: ComponentActivity) {
        googleLoginClient = GoogleLoginClientFactory().create(activity)
        facebookLoginClient = FacebookLoginClientFactory().create(activity)
        twitterLoginClient = TwitterLoginClientFactory().create(activity)
        linkedInLoginClient = LinkedInLoginClientFactory().create(activity)
    }

    fun login(context: Context, loginType: LoginType, loginCallback: LoginCallback) {
        when (loginType) {
            FacebookLogin -> {
                if (isInstalledPackage(context, "com.facebook.katana")) {
                    facebookLoginClient?.login(loginCallback)
                } else {
                    Toast.makeText(context, "Please install the facebook app first.", Toast.LENGTH_SHORT).show()
                }
            }

            GoogleLogin -> {
                if (isGMSAvailable(context)) {
                    googleLoginClient?.login(loginCallback)
                } else {
                    Toast.makeText(context, "Google Play services is not available.", Toast.LENGTH_SHORT).show()
                }

            }

            TwitterLogin -> {
                twitterLoginClient?.login(loginCallback)
            }

            LinkedInLogin -> {
                linkedInLoginClient?.login(loginCallback)
            }
        }

    }

    fun loginFacebook(context: Context, loginCallback: LoginCallback) {

        if (isInstalledPackage(context, "com.facebook.katana")) {
            facebookLoginClient?.login(loginCallback)
        } else {
            Toast.makeText(context, "Please install the Facebook app first", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginGoogle(context: Context, loginCallback: LoginCallback) {
        if (isGMSAvailable(context)) {
            googleLoginClient?.login(loginCallback)
        } else {
            Toast.makeText(context, "Google Play services is not available.", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginLinkedIn(loginCallback: LoginCallback) {
        linkedInLoginClient?.login(loginCallback)
    }

    fun loginTwitter(loginCallback: LoginCallback) {
        twitterLoginClient?.login(loginCallback)
    }

    private fun isInstalledPackage(context: Context, packageName: String): Boolean {
        val packageInfos = context.packageManager.getInstalledPackages(0)
        if (packageInfos.isNullOrEmpty() || packageName.isNullOrEmpty()) {
            return false
        }
        val packageInfo = packageInfos.find { it.packageName.equals(packageName) }
        return packageInfo != null
    }

    private fun isGMSAvailable(context: Context): Boolean {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return code == ConnectionResult.SUCCESS
    }
}