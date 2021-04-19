package com.aportillo.shoppinglistkotlin.data

import android.content.Intent
import android.util.Log
import com.aportillo.shoppinglistkotlin.data.model.LoggedInUser
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import java.io.IOException


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private var mAuth: FirebaseAuth?=null

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {

            mAuth=FirebaseAuth.getInstance()


           val mAuthResult = mAuth!!.signInWithEmailAndPassword(username, password)
            mAuthResult.addOnCompleteListener(OnCompleteListener<AuthResult> { task ->
                run {
                    if (task.isSuccessful) {
                        Log.i("Autenticacion", "success")
                        val currentUser = mAuth?.currentUser
                        Result.Success(LoggedInUser(username, password))
                    } else {
                        Log.i("Autenticacion", "Fail")
                        Result.Error(IOException("Error logging in", task.exception))
                    }
                }
            })

            return Result.Success(LoggedInUser(username, password))
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun register(username: String, password: String): Result<LoggedInUser> {
        try {

            mAuth=FirebaseAuth.getInstance()


            val mAuthResult =  mAuth!!.createUserWithEmailAndPassword(username, password)
            mAuthResult.addOnCompleteListener(OnCompleteListener<AuthResult> { task ->
                run {
                    if (task.isSuccessful) {
                        Log.i("Register", "success")
                        val currentUser = mAuth?.currentUser
                        Result.Success(LoggedInUser(username, password))
                    } else {
                        Log.i("Register", "Fail")
                        Result.Error(IOException("Error logging in", task.exception))
                    }
                }
            })
            return Result.Success(LoggedInUser(username, password))
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}