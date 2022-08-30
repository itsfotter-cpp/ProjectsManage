package it.itsfotter.projectsmanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import it.itsfotter.projectsmanage.models.Board
import it.itsfotter.projectsmanage.utils.Constants
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null

    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        /*
        This is a way to retrieve the username without doing another database request
         */
        if(intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME).toString()

        }

        iv_board_image.setOnClickListener {

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

        btn_create.setOnClickListener {
            if(mSelectedImageFileUri != null) {
                uploadBoardImage()
            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }

    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_create_board_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_white_back_24dp)
        }

        toolbar_create_board_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        iv_board_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                    .with(this@CreateBoardActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(iv_board_image)
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun createBoard() {
        /* Assign the current user to the new board, for the moment */
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        var board = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        FirestoreClass().createBoard(this, board)

    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri != null) {
            /* we want to store our image in the Firebase Storage */
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child("BOARD_IMAGE" +
                        System.currentTimeMillis() + "." + Constants.getFileExtension(this, mSelectedImageFileUri!!))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i(
                    "Board Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                /* Notice that we need to download the URL in order to store it in the user data
                        in the image attribute.
                 */

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.i("Downloadable Image URL", uri.toString())

                    mBoardImageURL = uri.toString()

                    // TODO Update the user profile data
                    createBoard()

                }.addOnFailureListener {
                        exception ->
                    Toast.makeText(
                        this@CreateBoardActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }

            }
        }
    }

}