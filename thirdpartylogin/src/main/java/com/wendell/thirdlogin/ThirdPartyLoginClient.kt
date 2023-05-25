package com.wendell.thirdlogin

/**
 *
 * @author Wendell
 * 2023/5/17 11:32
 */
interface ThirdPartyLoginClient {
    fun login(loginCallback: LoginCallback)
}