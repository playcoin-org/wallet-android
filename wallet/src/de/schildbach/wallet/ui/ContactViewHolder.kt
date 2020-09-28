/*
 * Copyright 2020 Dash Core Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.ui

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import de.schildbach.wallet.data.UsernameSearchResult
import de.schildbach.wallet.livedata.Resource
import de.schildbach.wallet_test.R
import kotlinx.android.synthetic.main.dashpay_contact_row.view.*

class ContactViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.dashpay_contact_row, parent, false)) {

    interface OnItemClickListener {
        fun onItemClicked(view: View, usernameSearchResult: UsernameSearchResult)
    }

    interface OnContactRequestButtonClickListener {
        fun onAcceptRequest(usernameSearchResult: UsernameSearchResult, position: Int)
        fun onIgnoreRequest(usernameSearchResult: UsernameSearchResult, position: Int)
    }

    fun bind(usernameSearchResult: UsernameSearchResult, sendContactRequestWorkState: Resource<WorkInfo>?, listener: OnItemClickListener?, contactRequestButtonClickListener: OnContactRequestButtonClickListener?) {
        val defaultAvatar = UserAvatarPlaceholderDrawable.getDrawable(itemView.context,
                usernameSearchResult.username[0])

        val dashPayProfile = usernameSearchResult.dashPayProfile
        if (dashPayProfile.displayName.isEmpty()) {
            itemView.display_name.text = dashPayProfile.username
            itemView.username.text = ""
        } else {
            itemView.display_name.text = dashPayProfile.displayName
            itemView.username.text = usernameSearchResult.username
        }

        if (dashPayProfile.avatarUrl.isNotEmpty()) {
            Glide.with(itemView.avatar).load(dashPayProfile.avatarUrl).circleCrop()
                    .placeholder(defaultAvatar).into(itemView.avatar)
        } else {
            itemView.avatar.background = defaultAvatar
        }

        itemView.setOnClickListener {
            listener?.onItemClicked(itemView, usernameSearchResult)
        }

        ContactRelation.process(usernameSearchResult.type, sendContactRequestWorkState, object : ContactRelation.RelationshipCallback {

            override fun none() {
                itemView.relation_state.displayedChild = 4
            }

            override fun inviting() {
                itemView.relation_state.displayedChild = 3
                itemView.pending_work_text.setText(R.string.sending_contact_request_short)
                (itemView.pending_work_icon.drawable as AnimationDrawable).start()
            }

            override fun invited() {
                itemView.relation_state.displayedChild = 1
            }

            override fun inviteReceived() {
                itemView.relation_state.displayedChild = 2
                itemView.accept_contact_request.setOnClickListener {
                    contactRequestButtonClickListener?.onAcceptRequest(usernameSearchResult, adapterPosition)
                }
                itemView.ignore_contact_request.setOnClickListener {
                    contactRequestButtonClickListener?.onIgnoreRequest(usernameSearchResult, adapterPosition)
                }
            }

            override fun acceptingInvite() {
                itemView.relation_state.displayedChild = 3
                itemView.pending_work_text.setText(R.string.accepting_contact_request_short)
                (itemView.pending_work_icon.drawable as AnimationDrawable).start()
            }

            override fun friends() {
                itemView.relation_state.displayedChild = 0
            }
        })
    }

    fun setBackgroundResource(@DrawableRes resId: Int) {
        itemView.setBackgroundResource(resId)
    }

    fun setBackgroundColor(@ColorInt color: Int) {
        itemView.setBackgroundColor(color)
    }

    fun setForegroundResource(resId: Int) {
        (itemView as FrameLayout).foreground = ResourcesCompat.getDrawable(itemView.resources, resId, null)
    }

    fun setMarginsDp(start: Int, top: Int, end: Int, bottom: Int) {
        (itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = dpToPx(start)
            topMargin = dpToPx(top)
            marginEnd = dpToPx(end)
            bottomMargin = dpToPx(bottom)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}