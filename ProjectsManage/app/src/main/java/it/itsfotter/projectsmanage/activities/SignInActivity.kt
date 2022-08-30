package it.itsfotter.projectsmanage.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import it.itsfotter.projectsmanage.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }

        setupActionBar()
    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            /*
            'setDisplayHomeAsUpEnabled()': specifies whether or not the Home button has the
                arrow used for Up Navigation next to it.

             'setHomeAsUpIndicator()': Set an alternate drawable to display next to the
                icon/logo/title when DISPLAY_HOME_AS_UP is enabled.
             */
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

        }

        /*
        Called when the activity has detected the user's press of the back key.
        The OnBackPressedDispatcher will be given a chance to handle the back button before
         the default behavior of onBackPressed is invoked.
         */
        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun signInRegisteredUser() {
        val email: String = et_email_sign_in.text.toString().trim { it <= ' ' }
        val password: String = et_password_sign_in.text.toString().trim { it <= ' ' }


        if(validateForm(email, password)) {
            //we want to sign in the user
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        FirestoreClass().loadUserData(this)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in ", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }

    }

    private fun validateForm(email: String, password: String) : Boolean {

        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                return true
            }
        }

    }

}