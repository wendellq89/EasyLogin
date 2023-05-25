package com.wendell.thirdlogin

import androidx.activity.ComponentActivity

/**
 *
 * @author Wendell
 * 2023/5/17 11:32
 */
interface ThirdPartyLoginClientFactory {
    /**
     * 在Activity中的OnCreate中处理
     */
    fun create(activity: ComponentActivity): ThirdPartyLoginClient
}