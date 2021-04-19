package com.aportillo.shoppinglistkotlin.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val successLogin: LoggedInUserView? = null,
    val successRegister: LoggedInUserView? = null,
    val error: Int? = null
)