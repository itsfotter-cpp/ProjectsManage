package it.itsfotter.projectsmanage.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import it.itsfotter.projectsmanage.activities.*
import it.itsfotter.projectsmanage.models.Board
import it.itsfotter.projectsmanage.models.User
import it.itsfotter.projectsmanage.utils.Constants

/*
    This class is used to contain all the method that we need for our FireStore Database.
    Because eventually if the we want to change the database method, or if we should
    change some deprecated function or something else, in this way it is simple.
 */

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    /*
    Notice that "collection" is different than "document".
    "Collection" is the top-level, then there is the document, i.e. "collection" is similar
     to "table" in SQL, instead the "document" is the record.
     */
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        //We create a new collection called "users"
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()) //we want to create a document for every user that we have
            .set(userInfo, SetOptions.merge()) // set() means that we want add something
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener{
                Log.e(activity.javaClass.simpleName, "Error on registerUser ($it)")
            }
        /*
        This is the way to create a collection.
        Notice that we can create that collection also with the Firebase Interafce on the web,
        but this is how creating the collection manually by code.
         */
    }

    fun getCurrentUserId(): String {
        /*
        Now we are doing the auto-login for already logged users.
         */
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if(currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID

        /*
        Only with this "return" we don't make the auto-login
         */
        //return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()) //we want to create a document for every user that we have
            .get() //get() means that we want gather something
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)

                when(activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser!!)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser!!, readBoardList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataUI(loggedInUser!!)
                    }
                }
            }.addOnFailureListener{
                when(activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }

                }
                Log.e("SignInUser", "Error on registerUser ($it)")
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()) //we want to create a document for every user that we have
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data updated")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener{
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                "Error while creating a board.",
                e)
                Toast.makeText(activity, "Error when updating the profile!", Toast.LENGTH_SHORT).show()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board created successfully.")

                Toast.makeText(activity,
                "Board created successfully",
                Toast.LENGTH_SHORT).show()

                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Board created successfully.",
                    exception)
            }
    }

    fun getBoardList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for(i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }

                activity.populateBoardsListToUI(boardList)

            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating the board list.")
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId) //each board has a unique documentId and we want get that
            .get()
            .addOnSuccessListener {
                    document ->
                    Log.i(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                    activity.boardDetails(board)
                    //we want that document as an object of type board
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating the board list.")
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "TaskList updated successfully")
                if(activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                }
                else if(activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener {
                exception ->
                if(activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                else if(activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board", exception)
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS) //"users"
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()
                for(i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if(activity is MembersActivity) {
                    activity.setupMemberList(usersList)
                } else if(activity is TaskListActivity) {
                    activity.boardMembersDetailsList(usersList)
                }
            }
            .addOnFailureListener {
                    exception ->
                    if(activity is MembersActivity) {
                        activity.hideProgressDialog()
                    } else if(activity is TaskListActivity) {
                        activity.hideProgressDialog()
                    }

                    Log.e(activity.javaClass.simpleName, "Error while loading members", exception)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document->

                if(document.documents.size > 0) {
                    /* Supposing that no one email address can have two account, this means that
                        we can use documents[0] to take the first entry of the query.
                     */
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.membersDetails(user)
                }
                else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
            .addOnFailureListener {
                    exception ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while getting user details", exception)
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.membersAssignSuccess(user)
            }
            .addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating assigned to", exception)
            }
    }

}