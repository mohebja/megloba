package com.global.sms.ui.emoji

import android.content.Context
import android.content.SharedPreferences

data class EmojiCategoryData(
    val name: String,
    val icon: String,
    val emojis: List<String>
)

object EmojiRepository {

    val categories = listOf(
        EmojiCategoryData(
            name = "لبخند و احساسات",
            icon = "😀",
            emojis = listOf(
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹",
                "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗",
                "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓",
                "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕",
                "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😮‍💨"
            )
        ),
        EmojiCategoryData(
            name = "ژست و دست‌ها",
            icon = "👍",
            emojis = listOf(
                "👍", "👎", "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘",
                "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "🫵", "✋", "🤚",
                "🖐️", "🖖", "👋", "🫲", "🫱", "👏", "🙌", "👐", "🤲", "🤝",
                "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂"
            )
        ),
        EmojiCategoryData(
            name = "قلب و عشق",
            icon = "❤️",
            emojis = listOf(
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
                "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
                "🔥", "✨", "🌟", "💫", "💥", "💢", "💦", "💧", "💤", "💬"
            )
        ),
        EmojiCategoryData(
            name = "حیوانات و طبیعت",
            icon = "🐱",
            emojis = listOf(
                "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨",
                "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🙈", "🙉", "🙊", "🐒",
                "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇",
                "🐺", "🐗", "🐴", "🦄", "🐝", "🪲", "🐛", "🦋", "🐌", "🐞"
            )
        ),
        EmojiCategoryData(
            name = "غذا و نوشیدنی",
            icon = "🍕",
            emojis = listOf(
                "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
                "melon", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🥑", "🥦",
                "🍕", "🍔", "🍟", "🌭", "🍿", "🧂", "🥓", "🍳", "🧇", "🥞",
                "🥐", "🍞", "🥖", "🥨", "🧀", "🥗", "🥪", "🌮", "🌯", "🥙"
            )
        ),
        EmojiCategoryData(
            name = "ورزش و فعالیت",
            icon = "⚽",
            emojis = listOf(
                "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
                "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳",
                "🪁", "🏹", "🎣", "🥽", "🥊", "🥋", "🎽", "🛹", "🛼", "🛷"
            )
        ),
        EmojiCategoryData(
            name = "سفر و اشیاء",
            icon = "🚗",
            emojis = listOf(
                "🚗", "🚕", "🚙", "🚌", "🏣", "🚎", "🏎️", "پلیس", "🚑", "🚒",
                "🚐", "🛻", "🚚", "🚛", "🚜", "🦯", "🦽", "🦼", "🛵", "🏍️",
                "🛵", "🚨", "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "🛜"
            )
        )
    )

    private const val PREFS_NAME = "emoji_history_prefs"
    private const val KEY_RECENTS = "recent_emojis"

    fun getRecentEmojis(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECENTS, null) ?: return listOf("😂", "❤️", "👍", "Iran", "🙏", "😊")
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun addRecentEmoji(context: Context, emoji: String) {
        val current = getRecentEmojis(context).toMutableList()
        current.remove(emoji)
        current.add(0, emoji)
        if (current.size > 30) {
            current.subList(30, current.size).clear()
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_RECENTS, current.joinToString(",")).apply()
    }
}
