package com.mparticle.smartype.generator

class StringHelpers {
    companion object {
        fun sanitize(key: String): String? {
            val sanitizedName = stripChars(key)?.let { upperFirst(it) }
            var uppercaseName = ""
            val words = sanitizedName?.split(" ", "_", "-")
            if (words == null || words.count() == 0) {
                return sanitizedName
            }
            var isFirst = true
            for (word in words) {
                if (isFirst) {
                    isFirst = false
                    uppercaseName = "${uppercaseName}${word}"
                    continue
                }
                val upperWord = upperFirst(word)
                uppercaseName = "${uppercaseName}${upperWord}"
            }
            return uppercaseName
        }

        private fun upperFirst(key: String?): String? {
            if (key == null || key.isEmpty()) {
                return null
            }
            val firstLetter = key[0].toUpperCase()
            val remainingLetters = key.subSequence(1, key.length)
            return "${firstLetter}${remainingLetters}"
        }

        fun lowerFirst(key: String?): String? {
            if (key == null || key.isEmpty()) {
                return null
            }
            val firstLetter = key[0].toLowerCase()
            val remainingLetters = key.subSequence(1, key.length)
            return "${firstLetter}${remainingLetters}"
        }

        private fun stripChars(key: String?): String? {
            if (key == null || key.isEmpty()) {
                return null
            }
            return key.replace(Regex("[^a-zA-Z0-9 _-]"), "")
        }
    }
}
