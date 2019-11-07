package de.tu.st.rssiscanner

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class RecyclerAdapter(private val myDataset: MutableList<BluetoothDevice>) :
    RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        val deviceRSSI: TextView = itemView.findViewById(R.id.deviceRSSI)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RecyclerAdapter.MyViewHolder {
        // create a new view
        val myView = LayoutInflater.from(parent.context)
            .inflate(R.layout.bluetooth_item, parent, false) as View
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(myView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.deviceName.text = myDataset[position].name
        holder.deviceRSSI.text = "RSSI"
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}