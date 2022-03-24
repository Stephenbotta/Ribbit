package com.ribbit.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class SocialEditText : AppCompatEditText, TextWatcher {
    companion object {
        private const val PREFIX_MENTION = '@'
        private const val PREFIX_HASHTAG = '#'
    }

    private var textChangedListener: OnTextChangedListener? = null
    private var suggestionListener: SuggestionListener? = null

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    private fun initialize() {
        addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) {
        val input = s?.toString() ?: ""
        textChangedListener?.onTextChanged(input)

        // Find the end cursor index and proceed only for valid case
        val cursorEndIndex = selectionEnd
        if (cursorEndIndex == -1) {
            suggestionListener?.onSuggestionQueryCleared()
            return
        }

        /*
        * Valid string will be a sub-string from 0 till then cursor end index.
        *
        * From the valid string, we take the last sub-string after the space " "
        *
        * Received string is then trimmed to remove any unnecessary whitespace
        * */
        val currentWord = input.substring(0, cursorEndIndex).substringAfterLast(" ").trim()

        /*
        * Get the last index of mention and hashtag in our word by checking its prefix.
        *
        * Current word can contain multiple mentions or hashtags, that is why we only use last value of it.
        * */
        val lastMentionIndex = currentWord.lastIndexOf(PREFIX_MENTION)
        val lastHashtagIndex = currentWord.lastIndexOf(PREFIX_HASHTAG)

        when {
            // If last mention index is greater than last hashtag index, then we have a mention at the end of our word
            lastMentionIndex > lastHashtagIndex -> {
                // Get only the mention text by getting the sub-string after the @ prefix
                val lastMention = currentWord.substringAfterLast(PREFIX_MENTION, "")

                if (lastMention.isNotEmpty()) {
                    // Pass the mention text to the listener
                    suggestionListener?.onMentionSuggestionReceived(lastMention)
                } else {
                    // Clear the query if no text exist after @ prefix
                    suggestionListener?.onSuggestionQueryCleared()
                }
            }

            // If last hashtag index is greater than last mention index, then we have a hashtag at the end of our word
            lastHashtagIndex > lastMentionIndex -> {
                // Get only the hashtag text by getting the sub-string after the # prefix
                val lastHashtag = currentWord.substringAfterLast(PREFIX_HASHTAG, "")

                if (lastHashtag.isNotEmpty()) {
                    // Pass the hashtag text to the listener
                    suggestionListener?.onHashtagSuggestionReceived(lastHashtag)
                } else {
                    // Clear the query if no text exist after # prefix
                    suggestionListener?.onSuggestionQueryCleared()
                }
            }

            else -> {
                // Clear the query for any other case
                suggestionListener?.onSuggestionQueryCleared()
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {

    }

    fun setTextChangedListener(listener: OnTextChangedListener) {
        textChangedListener = listener
    }

    fun setSuggestionListener(listener: SuggestionListener) {
        suggestionListener = listener
    }

    fun setTextWithoutTextChangedTrigger(text: CharSequence) {
        removeTextChangedListener(this)
        setText(text)
        addTextChangedListener(this)
    }

    /**
     * Updates the mention before the current cursor index
     *
     * Here "|" is used to denote the cursor
     * e.g. for a sample string "Hello @world| test"
     * if we have "user" as mentionText and our current index is after "world" then,
     * updated text will be "Hello @user| test"
     *
     * @param mentionText Text that needs to be used after the replace
     * */
    fun updateMentionBeforeCursor(mentionText: String) {
        val cursorEndIndex = selectionEnd
        if (cursorEndIndex != -1) {
            val completeText = text ?: ""
            val textEndIndex = cursorEndIndex - 1
            for (index in textEndIndex downTo 0) {
                if (completeText[index] == PREFIX_MENTION) {
                    // Replace after the @ until the end index of cursor
                    val updatedText = completeText.replaceRange(index + 1, cursorEndIndex, mentionText)
                    setTextWithoutTextChangedTrigger(updatedText)

                    // Updated cursor index will be after mention text
                    val updatedCursorIndex = index + mentionText.length + 1
                    setSelection(updatedCursorIndex)    // Set selection at the end of mention
                    break
                }
            }
        }
    }

    interface OnTextChangedListener {
        fun onTextChanged(text: String)
    }

    interface SuggestionListener {
        fun onMentionSuggestionReceived(mentionText: String)
        fun onHashtagSuggestionReceived(hashtagText: String)
        fun onSuggestionQueryCleared()
    }
}