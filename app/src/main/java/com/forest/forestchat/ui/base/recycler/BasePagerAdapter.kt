/*
 * Copyright (C) 2021 Matthieu Bouquet <matthieu@forestchat.org>
 *
 * This file is part of ForestChat.
 *
 * ForestChat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ForestChat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ForestChat.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.forest.forestchat.ui.base.recycler

import android.os.Parcelable
import android.util.SparseArray
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.StatefulAdapter
import kotlinx.android.parcel.Parcelize

abstract class BasePagerAdapter : BaseAdapter(), StatefulAdapter {

    protected var holders = mutableListOf<BaseHolder<BaseItem>>()
    private var pendingStates = PagerSavedState()

    override fun onViewHolderCreated(holder: BaseHolder<BaseItem>) {
        holders.add(holder)
    }

    @CallSuper
    override fun onViewRecycled(holder: BaseHolder<BaseItem>) {
        super.onViewRecycled(holder)

        holders.remove(holder)
    }

    @CallSuper
    override fun onViewAttachedToWindow(holder: BaseHolder<BaseItem>) {
        super.onViewAttachedToWindow(holder)

        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            pendingStates.get(position)?.let { state ->
                pendingStates.remove(position)
                holder.itemView.restoreHierarchyState(state)
            }
        }
    }

    override fun saveState(): Parcelable {
        holders.forEach { holder ->
            val container = SparseArray<Parcelable>()
            holder.itemView.saveHierarchyState(container)

            pendingStates.put(holder.adapterPosition, container)
        }

        return pendingStates
    }

    override fun restoreState(savedState: Parcelable) {
        if (savedState is PagerSavedState) {
            pendingStates = savedState
        }
    }

    @Parcelize
    data class PagerSavedState(val savedState: SparseArray<PageState> = SparseArray()) :
        Parcelable {
        fun get(index: Int): SparseArray<Parcelable>? = savedState[index]?.state

        fun put(index: Int, state: SparseArray<Parcelable>) {
            savedState.put(index, PageState(state))
        }

        fun remove(index: Int) {
            savedState.remove(index)
        }
    }

    @Parcelize
    data class PageState(val state: SparseArray<Parcelable>) : Parcelable

}