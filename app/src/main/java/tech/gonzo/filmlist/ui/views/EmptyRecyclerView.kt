package tech.gonzo.filmlist.ui.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

/**
 * Copyright (C) 2015 Glowworm Software
 * Copyright (C) 2014 Nizamutdinov Adel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * based on https://gist.github.com/adelnizamutdinov/31c8f054d1af4588dc5c
 */
class EmptyRecyclerView : RecyclerView {

    private var emptyView: View? = null

    // Currently crashing when inflating layout.
    //constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : super(context, attrs, defStyle)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)

        adapter?.registerAdapterDataObserver(observer)
        super.setAdapter(adapter)
        checkIfEmpty()
    }

    override fun swapAdapter(adapter: RecyclerView.Adapter<*>?, removeAndRecycleExistingViews: Boolean) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)

        adapter?.registerAdapterDataObserver(observer)
        super.swapAdapter(adapter, removeAndRecycleExistingViews)
        checkIfEmpty()
    }

    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            checkIfEmpty()
        }
    }

    /**
     * Indicates the view to be shown when the adapter for this object is empty.
     *
     * @param emptyView The View object to be used for the emptyView.
     */
    fun setEmptyView(emptyView: View?) {
        if (this.emptyView != null) {
            this.emptyView!!.visibility = View.GONE
        }

        this.emptyView = emptyView
        checkIfEmpty()
    }

    /**
     * Check adapter item count and toggle visibility of empty view if the adapter is empty
     */
    private fun checkIfEmpty() {
        if (emptyView == null || adapter == null) {
            return
        }

        if (adapter.itemCount > 0) {
            emptyView!!.visibility = View.GONE
        } else {
            emptyView!!.visibility = View.VISIBLE
        }
    }
}