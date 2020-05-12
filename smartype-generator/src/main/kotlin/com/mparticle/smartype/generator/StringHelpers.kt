package com.mparticle.smartype.generator

class StringHelpers {
    companion object {
        fun sanitize(key: String): String {
            val sanitizedName = upperFirst(stripChars(key))
            var uppercaseName = ""
            val words = sanitizedName.split(" ", "_", "-")
            if (words.count() == 0) {
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

        private fun upperFirst(key: String): String {
            val firstLetter = key[0].toUpperCase()
            val remainingLetters = key.subSequence(1, key.length)
            return "${firstLetter}${remainingLetters}"
        }

        fun lowerFirst(key: String): String {
            val firstLetter = key[0].toLowerCase()
            val remainingLetters = key.subSequence(1, key.length)
            return "${firstLetter}${remainingLetters}"
        }

        private fun stripChars(key: String): String {
            return key.replace(Regex("[^a-zA-Z0-9 _-]"), "")
        }
    }
}
