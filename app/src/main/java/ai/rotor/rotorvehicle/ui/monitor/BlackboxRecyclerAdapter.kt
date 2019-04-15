package ai.rotor.rotorvehicle.ui.monitor

import ai.rotor.rotorvehicle.R
import ai.rotor.rotorvehicle.data.Blackbox
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife

class BlackboxRecyclerAdapter(private val source: Blackbox) : RecyclerView.Adapter<BlackboxRecyclerAdapter.BlackboxViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlackboxViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.blackbox_view_holder, parent, false)
        return BlackboxViewHolder(view)
    }

    override fun getItemCount(): Int = source.getLogs().count()

    override fun onBindViewHolder(holder: BlackboxViewHolder, position: Int) {
        holder.loglineTextView.text = "ungabunga"
    }

    inner class BlackboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.BlackboxViewHolderText)
        lateinit var loglineTextView: TextView

        init {
            ButterKnife.bind(this, itemView)
        }
    }

}
