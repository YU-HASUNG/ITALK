package leopardcat.studio.chitchat.components.viewmodel

sealed interface ChatEvent {
    object SaveContact: ChatEvent
    data class SetChat(val chat: String, val name: String, val boolean: Boolean): ChatEvent
    data class SetPerson(val name: String): ChatEvent

    data class SetApiLoading(val isLoading: Boolean): ChatEvent

    data class SetHeart(val heart: Int): ChatEvent

    data class SetPersonImageId(val id: Int): ChatEvent

    data class SetVip(val vip: Boolean): ChatEvent
}