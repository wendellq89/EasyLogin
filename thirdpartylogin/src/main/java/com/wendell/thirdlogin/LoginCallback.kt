package com.wendell.thirdlogin

/**
 *
 * @author Wendell
 * 2023/5/19 13:47
 */
interface LoginCallback {

    fun onSuccess(result: AuthorizationInfo, type: LoginType)

    fun onError(error: ErrorInfo)
}