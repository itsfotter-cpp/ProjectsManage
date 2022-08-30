package it.itsfotter.projectsmanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import it.itsfotter.projectsmanage.models.User
import it.itsfotter.projectsmanage.utils.Constants
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    /*
    When we want to select an image from the device we should save the position of the image
    on the database.
     */
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        /*
            Avrei potuto chiamare direttamente da qua la funzione 'setUserDataUI', solo che per
            il re-use del codice richiamo sempre la funzione in FirestoreClass() in modo che devo
            aggiungere solo un caso 'activity' nel when statement.
         */
        FirestoreClass().loadUserData(this)

        iv_profile_user_image.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                    Constants.showImageChooser(this)
                }
            /*
                If we don't have PERMISSION, we want to do that.
             */
            else {
                ActivityCompat.requestPermissions(
                    this,
                    /* this is the array of the permission that we want to ask for */
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_update.setOnClickListener {
            /* if the image_profile is changed we execute the code to save the new image and
                to change the URI for the image in the Firebase db;
               otherwise we check if they are changed only the name, email or mobile
             */
            if(mSelectedImageFileUri != null) {
                uploadUserImage()
                Log.i("MyProfileActivity","IMAGE PROFILE CHANGED")
            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
                Log.i("MyProfileActivity","IMAGE PROFILE NOT CHANGED")
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO Show Image Chooser
                Constants.showImageChooser(this)
            }
        }
        else {
            Toast.makeText(
                this,
                "Oops, you just denied the permission for storage!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null) {
                /*
                'data' is what we have done with 'startActivityForResult'
                 */
            mSelectedImageFileUri = data.data
            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_profile_user_image)
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_my_profile_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_white_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataUI(user: User) {

        mUserDetails = user

        Glide
            .with(this@MyProfileActivity) //name of the activity where to put the image
            .load(user.image) //url of the image saved in Firebase
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder) //default image if there is not an own image
            .into(iv_profile_user_image) //where to put the image

        et_name.setText(user.name)
        et_email.setText(user.email)
        /*
            We should do this check because we saved as default
            phone number "0".
         */
        if(user.mobile != 0L) {
            et_mobile.setText(user.mobile.toString())
        }
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri != null) {
            /* we want to store our image in the Firebase Storage */
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child("USER_IMAGE" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(this, mSelectedImageFileUri!!))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                /* Notice that we need to download the URL in order to store it in the user data
                        in the image attribute.
                 */

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Downloadable Image URL", uri.toString())

                    mProfileImageURL = uri.toString()

                    // TODO Update the user profile data
                    updateUserProfileData()

                }.addOnFailureListener {
                    exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }

            }
        }

    }

    fun profileUpdateSuccess() {
        /* hide the progress bar and close the update activity in a way that the user
            does not do that manually */
        hideProgressDialog()

        /* It's a sort of signal for the app to say "it's all ok for the result" */
        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        /*
        the new mProfileImageURL has not be empty and has not be equal to the previous URL image,
        because otherwise we don't need an update.

        NOTA CHE QUESTO CONTROLLO POTREBBE SEMBRARE SUPERFLUO MA NON LO E' PERCHE' QUESTA
        FUNZIONE VIENE UTILIZZATA ANCHE QUANDO BISOGNA AGGIORNARE I CAMBI SE E' STATA CAMBIATA
        L'IMMAGINE. [vedi uploadUserImage()]
         */
        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if(et_name.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        if(et_mobile.text.toString().isNotEmpty() && et_mobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
        }
        else if(et_mobile.text.toString().isEmpty()) {
            userHashMap[Constants.MOBILE] = 0L
        }

        FirestoreClass().updateUserProfileData(this, userHashMap)

    }

}