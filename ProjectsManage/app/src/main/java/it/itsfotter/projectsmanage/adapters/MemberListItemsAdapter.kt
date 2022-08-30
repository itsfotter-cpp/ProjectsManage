package it.itsfotter.projectsmanage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.models.User
import it.itsfotter.projectsmanage.utils.Constants
import kotlinx.android.synthetic.main.item_member.view.*

open class MemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_member, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder) {
            Glide
                .with(context) //context is the class in which the RV is set
                .load(model.image) //model is the board selected for that position
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder) //default image if there is not an own image
                .into(holder.itemView.iv_member_image) //where to put the image

            holder.itemView.tv_member_name.text = model.name
            holder.itemView.tv_member_email.text = model.email

            if(model.selected) {
                holder.itemView.iv_selected_member.visibility = View.VISIBLE
            }
            else {
                holder.itemView.iv_selected_member.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    if(model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    }
                    else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }

        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {

        this.onClickListener = onClickListener


    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }
}