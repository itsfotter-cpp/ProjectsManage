package it.itsfotter.projectsmanage.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /*
        Window flag: hide all screen decorations (such as the status bar) while this window is
         displayed. This allows the window to use the entire display space for itself --
         the status bar will be hidden when an app window with this flag set is on the top layer.
         */
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        /*
        This allows us to use a custom font.
        We should download the file of the font, then we create a new folder "assets" in the
        main root and we put the file in that folder.
         */
        val typeFace: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        tv_app_name.typeface = typeFace

        Handler().postDelayed({

            var currentUserID = FirestoreClass().getCurrentUserId()

            if(currentUserID.isNotEmpty()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }

            finish()
            /*
            This means that afte 2.5 seconds the user will be in the Intro Activity,
            but since that this activity "finish()", even with the back button
            the user cannot return to this activity.
             */
        }, 2500)
    }
}