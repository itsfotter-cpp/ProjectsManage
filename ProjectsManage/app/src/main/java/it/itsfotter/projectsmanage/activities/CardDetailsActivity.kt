package it.itsfotter.projectsmanage.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.adapters.CardMemberListItemsAdapter
import it.itsfotter.projectsmanage.dialogs.LabelColorListDialog
import it.itsfotter.projectsmanage.dialogs.MemberListDialog
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import it.itsfotter.projectsmanage.models.*
import it.itsfotter.projectsmanage.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_members.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private var mTaskListPosition: Int = -1
    private var mCardPosition: Int = -1
    private var mSelectedColor: String = ""
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setupActionBar()

        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        /* the setSelection method sets the focus on the et_name_card_details when the activity is
            called and put the focus at the end of the text in the EditText
         */
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()) {
            setColor()
        }

        btn_update_card_details.setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty()) {
                updateCardDetails()
            }
            else{
                Toast.makeText(
                    this@CardDetailsActivity,
                    "Enter a card name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition]
                                            .cards[mCardPosition]
                                            .dueDate
        /*
        piece of code for formatting the due date if it is already saved in the card db.
         */
        if(mSelectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            tv_select_due_date.text = selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDataPicker()
        }

    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_card_details_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_white_back_24dp)
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }

        toolbar_card_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getIntentData() {
        if(intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBER_LIST)!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            /* code run when the action_members menu is pressed */
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        //Log.e("LOGCard", "$mBoardDetails")

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        //Log.e("LOGCard", "$mBoardDetails")

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun deleteCard() {
        val cardList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardList.removeAt(mCardPosition)
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage(
            resources.getString(R.string.confirmation_message_to_delete_card, cardName)
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            deleteCard()
        }

        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#0022F8")

        return colorsList

    }

    private fun setColor() {
        tv_select_label_color.text = ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorsListDialog() {
        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object: LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun membersListDialog() {

        var cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition]
                                            .cards[mCardPosition].assignTo

        /*
            First, we check if there are some members in the list.
            Then we go through all the members in the list.
            Then we check if the member is equal to the member of the card, if they are equal we set
            the variable selected to TRUE.

            cardAssignedMembersList: contains the id of the "assignTo" of a Card;
            mMembersDetailList: contains the list of the member of the Board.

        */

        Log.i("MemberListDialogTAG", "$cardAssignedMembersList")
        Log.i("MemberListDialogTAG", "$mMembersDetailList")

        if(cardAssignedMembersList.size > 0) {
            for(i in mMembersDetailList.indices) {
                for(j in cardAssignedMembersList) {
                    if(mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }
        else {
            for(i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false

            }
        }

        val listDialog = object : MemberListDialog(
            this@CardDetailsActivity,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT) {
                    if(!mBoardDetails.taskList[mTaskListPosition]
                            .cards[mCardPosition].assignTo.contains(user.id)) {
                        mBoardDetails.taskList[mTaskListPosition]
                            .cards[mCardPosition].assignTo.add(user.id)
                    }
                }
                else {
                    mBoardDetails.taskList[mTaskListPosition]
                        .cards[mCardPosition].assignTo.remove(user.id)

                    for(i in mMembersDetailList.indices) {
                        if(mMembersDetailList[i].id == user.id) {
                            mMembersDetailList[i].selected = false
                        }
                    }
                }

                setupSelectedMembersList()
            }
        }

        listDialog.show()

    }

    private fun setupSelectedMembersList() {

        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition]
                                            .cards[mCardPosition].assignTo
        val selectedMemberList: ArrayList<SelectedMembers> = ArrayList()

        for(i in mMembersDetailList.indices) {
            for(j in cardAssignedMembersList) {
                if(mMembersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMemberList.add(selectedMember)
                }
            }
        }

        if(selectedMemberList.size > 0) {
            selectedMemberList.add(SelectedMembers("", ""))
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            rv_selected_members_list.layoutManager = GridLayoutManager(
                this@CardDetailsActivity,
                6
            )

            val adapter = CardMemberListItemsAdapter(this@CardDetailsActivity, selectedMemberList, true)

            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(
                object : CardMemberListItemsAdapter.OnClickListener{
                    override fun onClick() {
                        membersListDialog()
                    }
                })
        }
        else {
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }
    }

    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener{ view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                tv_select_due_date.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()
    }
}

