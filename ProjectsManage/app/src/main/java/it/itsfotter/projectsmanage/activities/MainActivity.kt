package it.itsfotter.projectsmanage.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView //used to select the item in the Navigation Menu
import com.google.firebase.auth.FirebaseAuth
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.adapters.BoardItemsAdapter
import it.itsfotter.projectsmanage.firebase.FirestoreClass
import it.itsfotter.projectsmanage.models.Board
import it.itsfotter.projectsmanage.models.User
import it.itsfotter.projectsmanage.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        /*
        we put 'this', because this is the class that implement the
        behaviour of the navigation menu.
         */
        nav_view.setNavigationItemSelectedListener(this)

        /*
        In this case it is called again the signInUser function but only for update the name
        and the image user.
         */
        FirestoreClass().loadUserData(this, true)

        /*
        Only with this method the floating button shows the vector icon
         */
        fab_create_board.setImageResource(R.drawable.ic_vector_add_24dp)

        fab_create_board.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            /*
            in this way we put the username variable in the request such that we avoid
            a database request to retrieve the username.
             */
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            // Toggle drawer
            toggleDrawer()
        }

    }

    private fun toggleDrawer() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }

        /* GravityCompat.START: it pushes object to x-axis position at the start of its container,
            not changing its size. */
    }

    /*
        When we click on the back button we should close the navigation menu, so we have to ovveride
        the onBackPressed function
     */
    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            /*
                If the Navigation Menu is closed and we click twice on
                the back button, so the app has to be closed.

                doubleBackToExit() is instanciated in the BaseActivity
             */
            doubleBackToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*
        QUESTO SERVE NEL CASO VIENE FATTO L'UPDATE DI QUALCHE CAMPO DELL'USER PROFILE ALLORA
        LA MAIN ACTIVITY VIENE A CONOSCENZA DEL FATTO CHE DEVE AGGIORNARE I SUOI CAMPI
         */
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardList(this)
        }
        else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.nav_my_profile -> {
                /*
                "startActivityForResult" means that in that activity there could be some actions
                that could update the user profile, therefore the Main Activity has to update
                its details for the user.
                 */
                startActivityForResult(Intent(this@MainActivity,
                    MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                /*
                FLAG_ACTIVITY_CLEAR_TOP ->
                Se impostato e l'attività avviata è già in esecuzione nell'attività corrente,
                 invece di avviare una nuova istanza di tale attività, tutte le altre attività
                 sopra di essa verranno chiuse e questo Intent verrà consegnato al
                 (ora su in alto) vecchia attività come nuovo intento.

                Ad esempio, considera un'attività composta dalle attività: A, B, C, D.
                Se D chiama startActivity() con un Intent che si risolve nel componente
                dell'attività B, allora C e D saranno terminate e B riceverà l'Intent
                specificato , risultando che lo stack ora è: A, B.

                FLAG_ACTIVITY_NEW_TASK ->
                Quando si utilizza questo flag, se un'attività è già in esecuzione per
                 l'attività che si sta avviando, una nuova attività non verrà avviata;
                  invece, l'attività corrente verrà semplicemente portata in primo piano
                   sullo schermo con lo stato in cui si trovava l'ultima volta.
                 */
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardList: Boolean) {

        /*
        we create this variable in a way to 'save' locally the username to avoid another
        database request.
         */
        mUserName = user.name

        //http://bumptech.github.io/glide
        Glide
            .with(this) //name of the activity where to put the image
            .load(user.image) //url of the image saved in Firebase
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder) //default image if there is not an own image
            .into(nav_user_image) //where to put the image

        tv_username.text = user.name

        if(readBoardList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardList(this)
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        if(boardsList.size > 0) {
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            rv_boards_list.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }

            })

        }
        else {
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }


    }

}