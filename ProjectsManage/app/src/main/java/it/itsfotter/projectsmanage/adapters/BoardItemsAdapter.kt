package it.itsfotter.projectsmanage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.models.Board
import kotlinx.android.synthetic.main.item_board.view.*

open class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_board, parent, false)
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
                .into(holder.itemView.iv_board_image) //where to put the image

            holder.itemView.tv_name.text = model.name
            holder.itemView.tv_created_by.text = "Created by: ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

}