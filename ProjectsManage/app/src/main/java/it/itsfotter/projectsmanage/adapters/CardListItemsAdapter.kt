package it.itsfotter.projectsmanage.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.itsfotter.projectsmanage.R
import it.itsfotter.projectsmanage.activities.TaskListActivity
import it.itsfotter.projectsmanage.models.Card
import it.itsfotter.projectsmanage.models.SelectedMembers
import kotlinx.android.synthetic.main.item_card.view.*

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
    ):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]

        if(holder is MyViewHolder) {

            if(model.labelColor.isNotEmpty()) {
                holder.itemView.view_label_color.visibility = View.VISIBLE
                holder.itemView.view_label_color.setBackgroundColor(Color.parseColor(model.labelColor))
            }
            else {
                holder.itemView.view_label_color.visibility = View.GONE
            }

            holder.itemView.tv_card_name.text = model.name

            if((context as TaskListActivity).mAssignedMemberDetailsList.size > 0) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                for(i in context.mAssignedMemberDetailsList.indices) {
                    for(j in model.assignTo) {
                        if(context.mAssignedMemberDetailsList[i].id == j) {
                            val selectedMembers = SelectedMembers(
                                context.mAssignedMemberDetailsList[i].id,
                                context.mAssignedMemberDetailsList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }

                if(selectedMembersList.size > 0) {
                    /* It is useless to see the image only if there is only the creator of the LIST,
                    so the images are shown only if there are multiple assignment.
                     */
                    if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                    }
                    else {
                        holder.itemView.rv_card_selected_members_list.visibility = View.VISIBLE
                        holder.itemView.rv_card_selected_members_list.layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemsAdapter(
                            context,
                            selectedMembersList,
                            false
                        )
                        holder.itemView.rv_card_selected_members_list.adapter = adapter

                        adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if(onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                }
                else {
                    holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnClickListener {
        fun onClick(position: Int)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

}