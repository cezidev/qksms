package com.moez.QKSMS.presentation.conversations

import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.MarkAllSeen
import com.moez.QKSMS.presentation.Navigator
import com.moez.QKSMS.presentation.base.QkViewModel
import io.realm.RealmResults
import javax.inject.Inject

class ConversationsViewModel : QkViewModel<ConversationsView, ConversationsState>(ConversationsState()) {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markAllSeen: MarkAllSeen

    private val conversations: RealmResults<Message>

    init {
        AppComponentManager.appComponent.inject(this)

        conversations = messageRepo.getConversationMessagesAsync()
        newState { it.copy(conversations = conversations) }

        markAllSeen.execute(Unit)
    }

    override fun bindIntents(view: ConversationsView) {
        super.bindIntents(view)

        view.composeIntent.subscribe()
        view.archivedIntent.subscribe()
        view.scheduledIntent.subscribe()
        view.blockedIntent.subscribe()
        view.settingsIntent.subscribe { navigator.showSettings() }
    }

    override fun onCleared() {
        super.onCleared()
        markAllSeen.dispose()
    }

}