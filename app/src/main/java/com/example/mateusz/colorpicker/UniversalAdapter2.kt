package com.example.mateusz.colorpicker

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jacekmarchwicki.changesdetector.ChangesDetector
import com.jacekmarchwicki.changesdetector.SimpleDetector
import com.jacekmarchwicki.universaladapter.BaseAdapterItem
import com.jacekmarchwicki.universaladapter.ViewHolderManager


open class UniversalAdapter2(private val managers: List<ViewHolderManager>) : RecyclerView.Adapter<ViewHolderManager.BaseViewHolder<BaseAdapterItem>>(), ChangesDetector.ChangesAdapter {
    private val changesDetector = ChangesDetector(SimpleDetector<BaseAdapterItem>())
    private var items: List<BaseAdapterItem> = emptyList()

    fun accept(baseAdapterItems: List<BaseAdapterItem>) {
        items = baseAdapterItems
        changesDetector.newData(this, items, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderManager.BaseViewHolder<BaseAdapterItem> {
        val manager = managers[viewType]
        return manager.createViewHolder(parent, LayoutInflater.from(parent.context))
    }

    override fun getItemViewType(position: Int): Int {
        val baseAdapterItem = items[position]
        for (i in managers.indices) {
            val manager = managers[i]
            if (manager.matches(baseAdapterItem)) {
                return i
            }
        }
        throw RuntimeException("Unsupported item type: " + baseAdapterItem)
    }

    override fun onBindViewHolder(holder: ViewHolderManager.BaseViewHolder<BaseAdapterItem>, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].adapterId()
    }

    /**
     * Return item at position
     *
     *
     * Tip: Should not be used in reactive code because it's not a function
     * Tip: Need to be called from UIThread - because it can change
     *
     * @param position of item on the list
     * @return item at position
     * @throws IndexOutOfBoundsException if the position is out of range
     * (<tt>position &lt; 0 || index &gt;= getItemCount()</tt>)
     */
    fun getItemAtPosition(position: Int): BaseAdapterItem {
        return items[position]
    }

    override fun onFailedToRecycleView(holder: ViewHolderManager.BaseViewHolder<BaseAdapterItem>): Boolean {
        return holder.onFailedToRecycleView()
    }

    override fun onViewAttachedToWindow(holder: ViewHolderManager.BaseViewHolder<BaseAdapterItem>) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolderManager.BaseViewHolder<BaseAdapterItem>) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
    }

    override fun onViewRecycled(holder: ViewHolderManager.BaseViewHolder<BaseAdapterItem>) {
        holder.onViewRecycled()
        super.onViewRecycled(holder)
    }
}