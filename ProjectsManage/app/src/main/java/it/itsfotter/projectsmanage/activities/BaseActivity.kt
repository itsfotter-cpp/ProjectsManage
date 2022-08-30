package it.itsfotter.projectsmanage.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import it.itsfotter.projectsmanage.R
import kotlinx.android.synthetic.main.dialog_progress.*

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    /*
    The idea behind this Dialog is that we want to display a progress dialog to the user each time
    that something is loading, a sort of loading spinner.
     */
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        /*
        Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views
        to the screen.
         */
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.tv_progress_text.text = text

        /*
        Start the dialog and display it on screen.
         */
        mProgressDialog.show()
    }

    /*
    function to hide the progress dialog
     */
    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

    /*
    We use Firebase to get the current unique user ID (uid)
     */
    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    /*
    This is a function used for the back button.
    If it is pressed once there is a Toast message, instead if it is pressed twice
    the user return back.
     */
    fun doubleBackToExit() {
        if(doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        /*
        If the user clicks the back button once, we want to have a little delay before
        to restart everything.

        So the variable doubleBackToExitPressedOnce becomes false after 2 seconds.

        Handler(Looper.getMainLooper()) uses this because only 'Handler()' is deprecated.
         */

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)

    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG )

        //'android.R.id.content': gives you the root element of a view.

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(
            this,
            R.color.snackbar_error_color)
        )

        snackBar.show()
    }

}