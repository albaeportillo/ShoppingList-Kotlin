package com.aportillo.shoppinglistkotlin.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aportillo.shoppinglistkotlin.R
import com.aportillo.shoppinglistkotlin.ShoppinListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    //Creating member variables of FirebaseAuth
    private lateinit var mAuth: FirebaseAuth
    //Creating member variables of FirebaseDatabase and DatabaseReference
 /*   private var mFirebaseDatabaseInstances: FirebaseDatabase?=null
    private var mFirebaseDatabase: DatabaseReference?=null */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        //Get Firebase Instances
       mAuth = Firebase.auth

        //Get instance of FirebaseDatabase
     //   mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()


        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val register = findViewById<Button>(R.id.register)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid
            register.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.successLogin != null) {
                updateUiWithUser(loginResult.successLogin,loginResult)
            }
            if (loginResult.successRegister != null) {
                updateUiWithUser(loginResult.successRegister,loginResult)
            }
            //Complete and destroy login activity once successful

            if (mAuth.currentUser != null) {
                setResult(Activity.RESULT_OK)

                val intent = Intent(this, ShoppinListActivity::class.java)
                intent.putExtra("userId", mAuth.currentUser?.email)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "User doesn't exist", Toast.LENGTH_LONG)
            }

        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }
            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }

            register.setOnClickListener {
                loading.visibility = View.VISIBLE
                if(username.text.toString().isNullOrEmpty() || password.text.toString().isNullOrEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        R.string.complete_fields,
                        Toast.LENGTH_LONG
                    ).show()
                   register.isEnabled = false
                    loading.visibility = View.INVISIBLE
                }
            }

        }


    }



    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this@LoginActivity, ShoppinListActivity::class.java)
            intent.putExtra("userId", mAuth.currentUser?.email)
            startActivity(intent)
        } else {
            //returns to login
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView, loginResult: LoginResult) {

        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        if(mAuth.currentUser != null) {
            if (model == loginResult.successRegister) {
                val welcome = getString(R.string.register)
                Toast.makeText(
                    applicationContext,
                    "$welcome $displayName",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val welcome = getString(R.string.welcome)
                Toast.makeText(
                    applicationContext,
                    "$welcome $displayName",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })



}