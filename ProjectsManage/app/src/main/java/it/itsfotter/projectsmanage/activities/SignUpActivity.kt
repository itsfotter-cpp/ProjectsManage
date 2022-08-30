package it.itsfotter.projectsmanage.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import it.itsfotter.projectsmanage.models.User
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        /*
        Window flag: hide all screen decorations (such as the status bar) while this window
        is displayed. This allows the window to use the entire display space for itself
         */

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this@SignUpActivity,
            "You have succesfully registered",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        /*
            signOut the user and then finish the activity if all of that
            is successful.
         */
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            /*
            ActionBar.setDisplayHomeAsUpEnabled() specifies whether or not the Home button has the
             arrow used for Up Navigation next to it.
             */
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

        }

        /*
        Called when the activity has detected the user's press of the back key.
        The OnBackPressedDispatcher will be given a chance to handle the back button before
         the default behavior of onBackPressed is invoked.
         */
        toolbar_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        btn_sign_up.setOnClickListener {
            registerUser()
        }

    }

    /*
    We are registering the users on two levels:
    1) one is to create the new authentication entry for hom
    2) one is to create a new entry in the collection of users, which is a new document
     */

    private fun registerUser() {

        /*
        .trim() { it <= ' ' }: it is used to clean the empty space and the function in
        the brackets means to clear the empty space at the start and the end of the name.
         */
        val name: String = et_name_sign_up.text.toString().trim() { it <= ' ' }
        val email: String = et_email_sign_up.text.toString().trim() { it <= ' ' }
        val password: String = et_password_sign_up.text.toString().trim() { it <= ' ' }

        if(validateForm(name, email, password)) {
            /*
            Toast.makeText(
                this@SignUpActivity,
                "Now we can register a new user.",
                Toast.LENGTH_SHORT
            ).show()
            */

            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!

                        /*
                        Now we should save the uid, the name and the email in the document of
                        the db of Firebase.
                         */
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            task.exception!!.message, //This is an exception message that could be set as "Registration failed"
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

    }

    private fun validateForm(name: String, email: String, password: String) : Boolean {

        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
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