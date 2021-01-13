import java.io.File

fun createVocabulary(trainingDataFile: File): List<String> {
//    Creates vocabulary from the training data file.
//    The vocabulary includes all the words that are encountered more than once.
//    This is done so that unknown words would have some representation in the the training data as well.
//    The vocabulary is also appended with the list of various types of "unknown", and special "word" indicating
//    beginning of the sentence - "--n--"

    val lineList = trainingDataFile.readLines().filter { it != "" }

    val words = lineList.map { getWord(it) }

    val wordFrequences = words.groupingBy { it }.eachCount()
    val vocabFromFile = wordFrequences.filter { (_, value) -> value > 1 }.keys.toList().sorted()
    val unknowns = listOf(
        "--n--", "--unk--", "--unk_adj--", "--unk_adv--", "--unk_digit--", "--unk_noun--",
        "--unk_punct--", "--unk_upper--", "--unk_verb--"
    )

    return (vocabFromFile + unknowns).sorted()
}

private fun getWord(line: String): String {
    var word = "--n--"
    if (line.split('\t').isNotEmpty()) {
        word = line.split('\t')[0]
    }
    return word
}

fun main() {
    val trainingData = File("src/data/WSJ_02-21.pos")
    val vocabulary = createVocabulary(trainingData)

    File("src/data/vocab.txt").writeText(vocabulary.joinToString("\n"))

}