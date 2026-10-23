package com.example.keyboard

object KeyboardLayouts {

    val numberRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    val qwertyRow1Lower = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val qwertyRow1Upper = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")

    val qwertyRow2Lower = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val qwertyRow2Upper = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")

    val qwertyRow3Lower = listOf("z", "x", "c", "v", "b", "n", "m")
    val qwertyRow3Upper = listOf("Z", "X", "C", "V", "B", "N", "M")

    // Bangla Layout (Phonetic / Avro-style key mappings)
    val banglaRow1 = listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও")
    val banglaRow2 = listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ")
    val banglaRow3 = listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন")
    val banglaRow4 = listOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ", "স", "হ")

    val symbolsRow1 = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")
    val symbolsRow2 = listOf("+", "=", "-", "_", "/", "\\", "{", "}", "[", "]")
    val symbolsRow3 = listOf("<", ">", "?", ";", ":", "\"", "'", "৳", "€", "£")

    val emojisList = listOf(
        "❤️", "😍", "😊", "🥰", "😂", "🔥", "👍", "✨", "💕", "🙏",
        "😎", "🥺", "🎉", "💯", "💖", "😄", "🙌", "🤩", "🌟", "🌹",
        "🤔", "😅", "👏", "💪", "🚀", "👌", "😘", "😜", "🙈", "🤝"
    )

    val stickersList = listOf(
        "❤️ I Love You",
        "Bengali: আমি তোমাকে ভালোবাসি",
        "😊 Good Morning! শুভ সকাল",
        "🙏 Thank You! ধন্যবাদ",
        "🔥 Awesome! দারুণ!",
        "🎉 Congratulations! অভিনন্দন!",
        "☕ Take a Coffee Break",
        "💖 Stay Safe & Take Care"
    )

    val asciiArtList = listOf(
        "(⁠◕⁠ᴗ⁠◕⁠✿⁠)",
        "(⁠•⁠_⁠•⁠)",
        "(⁠ϋ⁠)⁠/⁠♩",
        "(⁠⌐⁠■⁠-⁠■⁠)",
        "ʕ⁠·⁠ᴥ⁠·⁠ʔ",
        "(⁠≧⁠▽⁠≦⁠)",
        "¯\\_(ツ)_/¯",
        "(⁠T⁠T⁠)"
    )
}
