package com.mparticle.smartype.generator

class StringHelpers {
    companion object {
        fun sanitize(key: String, includeUnderscores: Boolean = false): String? {
            val sanitizedName = stripChars(key)?.let { upperFirst(it) }
            var uppercaseName = ""
            val words = sanitizedName?.split(" ", "_", "-")
            if (words == null || words.count() == 0) {
                return sanitizedName
            }
            var isFirst = true
            for (word in words) {
                if (word == "") {
                    continue
                }
                val upperWord = upperFirst(word)
                if (isFirst) {
                    isFirst = false
                    uppercaseName = "${uppercaseName}${upperWord}"
                    continue
                }
                if (includeUnderscores) {
                    uppercaseName = "${uppercaseName}_${upperWord}"
                } else {
                    uppercaseName = "${uppercaseName}${upperWord}"
                }
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
            var result = ""
            val length = key.length
            val firstLetter = key[0]
            if (firstLetter.isLowerCase()) {
                val remainingLetters = key.subSequence(1, key.length)
                return "${firstLetter}${remainingLetters}"
            }
            result += firstLetter.toLowerCase()
            var i = 1
            while (i < length) {
                val currentLetter = key[i]
                if (currentLetter.isLowerCase()) {
                    if (result.length > 2) {
                        val resultBegin = result.subSequence(0, result.length - 1)
                        val resultEnd = result[result.length - 1].toUpperCase()
                        result = "$resultBegin$resultEnd"
                    }
                    result += key.subSequence(i, key.length)
                    break
                } else {
                    result += key[i].toLowerCase()
                }
                i += 1
            }

            return result
        }

        private fun stripChars(key: String?): String? {
            if (key == null || key.isEmpty()) {
                return null
            }
            return key.replace(Regex("[^a-zA-Z0-9 _-]"), "")
        }
    }
}
