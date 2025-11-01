package com.lexur.yumo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import kotlin.text.any

object EmojiUtils {

    /**
     * Validates if the input is a single emoji (no text, no multiple emojis)
     */


    /**
     * Counts grapheme clusters (user-perceived characters)
     * This handles complex emojis like 👨‍👩‍👧 (family) or 👍🏽 (thumbs up with skin tone)
     */
    private fun countGraphemeClusters(text: String): Int {
        if (text.isEmpty()) return 0

        var count = 0
        var i = 0

        while (i < text.length) {
            val codePoint = text.codePointAt(i)

            // Skip if it's just a whitespace
            if (Character.isWhitespace(codePoint)) {
                i += Character.charCount(codePoint)
                continue
            }

            // We found a character, increment count
            count++
            i += Character.charCount(codePoint)

            // Skip over any combining characters, variation selectors, ZWJ sequences, etc.
            while (i < text.length) {
                val nextCodePoint = text.codePointAt(i)

                if (isModifier(nextCodePoint) ||
                    isVariationSelector(nextCodePoint) ||
                    isZeroWidthJoiner(nextCodePoint) ||
                    isCombiningCharacter(nextCodePoint)) {
                    i += Character.charCount(nextCodePoint)
                } else {
                    break
                }
            }
        }

        return count
    }

    /**
     * Checks if a codepoint is an emoji modifier (skin tone)
     */
    private fun isModifier(codePoint: Int): Boolean {
        return codePoint in 0x1F3FB..0x1F3FF
    }

    /**
     * Checks if a codepoint is a variation selector
     */
    private fun isVariationSelector(codePoint: Int): Boolean {
        return codePoint in 0xFE00..0xFE0F || codePoint == 0xFE0E || codePoint == 0xFE0F
    }

    /**
     * Checks if a codepoint is Zero Width Joiner (used in compound emojis)
     */
    private fun isZeroWidthJoiner(codePoint: Int): Boolean {
        return codePoint == 0x200D
    }

    /**
     * Checks if a codepoint is a combining character
     */
    private fun isCombiningCharacter(codePoint: Int): Boolean {
        return Character.getType(codePoint) == Character.COMBINING_SPACING_MARK.toInt() ||
                Character.getType(codePoint) == Character.NON_SPACING_MARK.toInt() ||
                Character.getType(codePoint) == Character.ENCLOSING_MARK.toInt()
    }

    /**
     * Alternative simple validation (use this if the above is too strict)
     */
    /**
     * Validates if the input is a single emoji (no text, no multiple emojis)
     */
    fun isValidSingleEmoji(input: String): Boolean {
        if (input.isBlank()) return false

        // Remove any whitespace
        val trimmed = input.trim()

        // Check if string contains any regular text characters (a-z, A-Z, 0-9, etc.)
        val hasRegularText = trimmed.any { char ->
            char.isLetterOrDigit() || char in '!'..'/' || char in ':'..'@'
        }

        if (hasRegularText) return false

        // Count grapheme clusters (what users see as "one emoji")
        val graphemeCount = countGraphemeClusters(trimmed)

        // Must be exactly one grapheme cluster
        return graphemeCount == 1
    }

    /**
     * Alternative simple validation (use this if the above is too strict)
     */
    fun isValidSingleEmojiSimple(input: String): Boolean {
        if (input.isBlank()) return false

        val trimmed = input.trim()

        // Check if it contains any ASCII letters or numbers
        val hasAsciiText = trimmed.any { char ->
            char.code in 32..126 && (char.isLetterOrDigit() || char in "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~")
        }
        if (hasAsciiText) return false

        // Check length - emojis can be 1-20+ characters due to ZWJ sequences
        // But we want to limit to reasonable length
        if (trimmed.length > 50) return false // Very long = probably multiple emojis

        // Simple check: if it's short and has no ASCII, likely a single emoji
        if (trimmed.length <= 15) return true

        // For longer sequences, do more careful checking
        return countGraphemeClusters(trimmed) == 1
    }

    /**
     * Converts an emoji string to a bitmap with transparent background
     * @param emoji The emoji string to convert
     * @param size The size of the bitmap in pixels
     * @return Bitmap with the emoji rendered on transparent background
     */
    fun emojiToBitmap(context: Context, emoji: String, size: Int = 512): Bitmap {
        // Create bitmap with transparent background
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Create paint for emoji
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size * 0.75f // 75% of canvas size for better fitting
            typeface = Typeface.DEFAULT
            color = Color.BLACK // This doesn't matter much for emojis as they have their own colors
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFilterBitmap = true
        }

        // Measure the text to center it properly
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(emoji, 0, emoji.length, textBounds)

        // Calculate position to center emoji
        val x = size / 2f
        val y = size / 2f - textBounds.exactCenterY()

        // Draw emoji
        canvas.drawText(emoji, x, y, paint)

        return bitmap
    }

    /**
     * Saves emoji bitmap to file and returns the file path
     */
    fun saveEmojiToFile(context: Context, emoji: String): String {
        val bitmap = emojiToBitmap(context, emoji)
        val fileName = "emoji_char_${System.currentTimeMillis()}.png"

        val directory = java.io.File(context.filesDir, "custom_characters")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = java.io.File(directory, fileName)
        java.io.FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        bitmap.recycle()
        return file.absolutePath
    }
}