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

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

abstract class BaseAdapter :
    ListAdapter<BaseAdapterItem, BaseHolder<BaseAdapterItem>>(getItemCallback()) {

    companion object {
        fun <U : BaseAdapterItem> getItemCallback(): DiffUtil.ItemCallback<U> =
            object : DiffUtil.ItemCallback<U>() {
                override fun areItemsTheSame(oldItem: U, newItem: U): Boolean =
                    newItem.isItemTheSame(oldItem)

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: U, newItem: U): Boolean =
                    newItem.getChangePayload(oldItem) == null

                override fun getChangePayload(oldItem: U, newItem: U): Any? =
                    newItem.getChangePayload(oldItem)
            }
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseHolder<BaseAdapterItem> =
        (buildViewHolder(parent, viewType) as? BaseHolder<BaseAdapterItem>)?.also { holder ->
            onViewHolderCreated(holder)
        } ?: error("Wrong view type for ${javaClass.simpleName}: $viewType")

    abstract fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>?

    fun indexOfFirst(predicate: (BaseAdapterItem) -> Boolean): Int =
        currentList.indexOfFirst(predicate)

    open fun onViewHolderCreated(holder: BaseHolder<BaseAdapterItem>) {}

    final override fun onBindViewHolder(holder: BaseHolder<BaseAdapterItem>, position: Int) {
        holder.bind(getItem(position))
    }

    final override fun onBindViewHolder(
        holder: BaseHolder<BaseAdapterItem>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            for (payload in payloads) {
                when (payload) {
                    is List<*> -> payload.forEach { item ->
                        onPayload(
                            holder,
                            position,
                            item as Any
                        )
                    }
                    else -> onPayload(holder, position, payload)
                }
            }
        }
    }

    open fun onPayload(holder: BaseHolder<BaseAdapterItem>, position: Int, payload: Any) {}

    @CallSuper
    override fun onViewRecycled(holder: BaseHolder<BaseAdapterItem>) {
        holder.onViewRecycled()
    }

    final override fun getItemViewType(position: Int): Int = getItem(position).getViewType()

}