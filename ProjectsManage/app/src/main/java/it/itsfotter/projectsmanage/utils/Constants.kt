package it.itsfotter.projectsmanage.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import it.itsfotter.projectsmanage.activities.MyProfileActivity

object Constants {

    // Constant for the name of 'users document' in the db
    const val USERS: String = "users"

    // Constant for the name of 'boards document' in the db
    const val BOARDS: String = "boards"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO: String = "assignedTo"
    const val EMAIL: String = "email"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val DOCUMENT_ID: String = "documentId"

    const val TASK_LIST: String = "taskList"

    const val BOARD_DETAIL: String = "board_detail"

    const val ID: String = "id"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val BOARD_MEMBER_LIST: String = "board_member_list"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"

    fun showImageChooser(activity: Activity) {
        /*
        This Intent opens up the functionality for the user to select an image from his
        own storage.
         */
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri): String? {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}