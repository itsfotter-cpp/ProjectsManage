package it.itsfotter.projectsmanage.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.adapters.MemberListItemsAdapter
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import it.itsfotter.projectsmanage.models.Board
import it.itsfotter.projectsmanage.models.User
import it.itsfotter.projectsmanage.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.dialog_search_member.*

class MembersActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board

    private lateinit var mAssignedMembersList: ArrayList<User>

    private var anyChangesMade: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if(intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!

            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
        }

        setupActionBar()

    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_members_activity)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_white_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }

        toolbar_members_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setupMemberList(list: ArrayList<User>) {

        mAssignedMembersList = list
        hideProgressDialog()

        rv_members_list.layoutManager = LinearLayoutManager(this)
        rv_members_list.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        rv_members_list.adapter = adapter

    }

    fun membersDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener {
            val email = dialog.et_email_search_member.text.toString()

            if(email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)
            }
            else {
                Toast.makeText(
                    this@MembersActivity,
                    "Please enter members email address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun membersAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesMade = true
        setupMemberList(mAssignedMembersList)
    }

    override fun onBackPressed() {
        if(anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

}