package it.itsfotter.projectsmanage.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.adapters.TaskListItemsAdapter
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import it.itsfotter.projectsmanage.models.Board
import it.itsfotter.projectsmanage.models.Card
import it.itsfotter.projectsmanage.models.Task
import it.itsfotter.projectsmanage.models.User
import it.itsfotter.projectsmanage.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    private lateinit var mBoardDocumentId: String
    lateinit var mAssignedMemberDetailsList: ArrayList<User>

    /*
    companion object {
         used for getting ActivityForResult in a way to load the TaskListActivity when
            a member is added to that task. that does not serve with the new startActivityForResult

        const val MEMBER_REQUEST_CODE: Int = 13
    }
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDocumentId)

    }

    private fun setupActionBar(title: String) {
        setSupportActionBar(toolbar_task_list_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_white_back_24dp)
            actionBar.title = title
        }

        toolbar_task_list_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /*
    The following two methods are used for the Members Menu on that Activity
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            /* code run when the action_members menu is pressed */
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                /* Notice that we are able to insert the 'mBoardDetails' because we have set
                    the Board class Parcelable in a way that the Object is treated as a String.
                 */
                getResult.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getBoardDetails(this, mBoardDocumentId)
            }
            else {
                Log.e("Cancelled", "Cancelled")
            }
        }

    /*

    SINCE THAT THIS IS DEPRECATED I HAVE PROVED THE NEW startActivityForResult!!!

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            /* code run when the action_members menu is pressed */
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                /* Notice that we are able to insert the 'mBoardDetails' because we have set
                    the Board class Parcelable in a way that the Object is treated as a String.
                 */
                startActivityForResult(intent, MEMBER_REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == Activity.RESULT_OK && requestCode == MEMBER_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this, mBoardDocumentId)
        }
        else {
            Log.e("Cancelled", "Cancelled")
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
     */

    fun boardDetails(board: Board) {

        mBoardDetails = board

        hideProgressDialog()
        setupActionBar(board.name)

        /*
        val addTaskList = Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        rv_task_list.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        rv_task_list.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, board.taskList)
        rv_task_list.adapter = adapter
         */

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        /* è solo un modo per non tenere troppo visibile il caricamento dello spinner per due cose
        differenti seppur in contemporanea.
         */
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId)

    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0, task)
        Log.i("ADD 1 BoardTaskList", "$mBoardDetails")
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        Log.i("ADD 2 BoardTaskList", "$mBoardDetails")
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {

        val task = Task(listName, model.createdBy)

        Log.i("UPDATE 1 BoardTaskList", "$mBoardDetails")
        mBoardDetails.taskList[position] = task
        Log.i("UPDATE 2 BoardTaskList", "$mBoardDetails")
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        Log.i("UPDATE 3 BoardTaskList", "$mBoardDetails")


        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)

    }

    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        Log.i("DELETE 1 BoardTaskList", "$mBoardDetails")

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        Log.i("DELETE 2 BoardTaskList", "$mBoardDetails")


        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        val cardAssignedUserList: ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FirestoreClass().getCurrentUserId())

        val card = Card(cardName, FirestoreClass().getCurrentUserId(), cardAssignedUserList)

        val cardList = mBoardDetails.taskList[position].cards
        /* Notice that the taskList consist of a list of Task, and that Task consist of a list
            of cards.
         */
        cardList.add(card)

        val task = Task(
                        mBoardDetails.taskList[position].title,
                        mBoardDetails.taskList[position].createdBy,
                        cardList)

        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)

    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        var intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBER_LIST, mAssignedMemberDetailsList)

        getResult.launch(intent)
    }

    fun boardMembersDetailsList(list: ArrayList<User>) {

        mAssignedMemberDetailsList = list
        //Log.i("AssignedMember", "$mAssignedMemberDetailsList")
        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        rv_task_list.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, mBoardDetails.taskList)
        rv_task_list.adapter = adapter

    }

}