import java.io.File

object Preprocessor {
    private val nounSuffix = listOf("action", "age", "ance", "cy", "dom", "ee", "ence", "er", "hood",
        "ion", "ism", "ist", "ity", "ling", "ment", "ness", "or", "ry", "scape", "ship", "ty")
    private val verbSuffix = listOf("ate", "ify", "ise", "ize")
    private val adjSuffix = listOf("able", "ese", "ful", "i", "ian", "ible", "ic", "ish", "ive",
        "less", "ly", "ous")
    private val advSuffix = listOf("ward", "wards", "wise")


    fun assignUnk(word: String): String {
        when {
            word.contains(Regex("[0-9]")) -> return "--unk_digit--"
            word.contains(Regex("[^A-Za-z0-9 ]")) -> return "--unk_punct--"
            word.contains(Regex("[A-Z]")) -> return "--unk_upper--"
            nounSuffix.any { word.endsWith(it) } -> return "--unk_noun--"
            verbSuffix.any { word.endsWith(it) } -> return "--unk_verb--"
            adjSuffix.any { word.endsWith(it) } -> return "--unk_adj--"
            advSuffix.any { word.endsWith(it) } -> return "--unk_adv--"
            else -> return "--unk--"
        }
    }


    fun getWordAndTagFromLine(line: String, vocab: Map<String, Int>): Pair<String, String> {
        if (line.split('\t').isNotEmpty() && line.split('\t').size == 2) {
            var word = line.split('\t')[0]
            val tag = line.split('\t')[1]
            if (word !in vocab.keys) {
                word = this.assignUnk(word)
            }
            return Pair(word, tag)
        } else return Pair("--n--", "--s--")
    }

    fun getTestWordsAndTags(vocab: Map<String, Int>, testDataFile: File): List<Pair<String, String>> {
        val wordsAndTags = mutableListOf<Pair<String, String>>()
        val lines = testDataFile.readLines()

        for (line in lines) {
            val (word, tag) = getWordAndTagFromLine(line, vocab)
            if (word == "") {
                wordsAndTags.add(Pair("--n--", "--s--"))
            } else if (!vocab.containsKey(word)) {
                wordsAndTags.add(Pair(assignUnk(word), tag))
            } else wordsAndTags.add(Pair(word, tag))
        }

        return wordsAndTags
    }

}