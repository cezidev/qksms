/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package feature.main

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import common.Navigator
import common.base.QkAdapter
import common.base.QkViewHolder
import common.util.Colors
import common.util.DateFormatter
import common.util.extensions.setVisible
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.search_list_item.view.*
import model.SearchResult
import javax.inject.Inject

class SearchAdapter @Inject constructor(
        private val context: Context,
        private val colors: Colors,
        private val dateFormatter: DateFormatter,
        private val navigator: Navigator
) : QkAdapter<SearchResult>() {

    private var highlightColor: Int = 0

    private val disposables = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.search_list_item, parent, false)
        return QkViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val previous = data.getOrNull(position - 1)
        val result = getItem(position)
        val view = viewHolder.itemView

        view.clicks().subscribe {
            navigator.showConversation(result.conversation.id, result.query.takeIf { result.messages > 0 })
        }

        view.resultsHeader.setVisible(result.messages > 0 && previous?.messages == 0)

        val query = result.query
        val title = SpannableString(result.conversation.getTitle())
        var index = title.indexOf(query, ignoreCase = true)

        while (index >= 0) {
            title.setSpan(BackgroundColorSpan(highlightColor), index, index + query.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            index = title.indexOf(query, index + query.length, true)
        }
        view.title.text = title

        view.avatars.contacts = result.conversation.recipients

        when (result.messages == 0) {
            true -> {
                view.date.setVisible(true)
                view.date.text = dateFormatter.getConversationTimestamp(result.conversation.date)
                view.snippet.text = when (result.conversation.me) {
                    true -> context.getString(R.string.main_sender_you, result.conversation.snippet)
                    false -> result.conversation.snippet
                }
            }

            false -> {
                view.date.setVisible(false)
                view.snippet.text = context.getString(R.string.main_message_results, result.messages)
            }
        }
    }

    override fun areItemsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.conversation.id == new.conversation.id && old.messages > 0 == new.messages > 0
    }

    override fun areContentsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.query == new.query && // Queries are the same
                old.conversation.id == new.conversation.id // Conversation id is the same
                && old.messages == new.messages // Result count is the same
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        disposables += colors.highlightColor
                .doOnNext { color -> highlightColor = color }
                .subscribe { notifyDataSetChanged() }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        disposables.dispose()
    }
}