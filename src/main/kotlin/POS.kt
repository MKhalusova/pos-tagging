import java.io.File

fun main() {
    val trainingCorpus = File("src/data/WSJ_02-21.pos").readLines()

    val vocab = File("src/data/vocab.txt").readLines()
    val vocabMap =
        vocab.mapIndexed { index: Int, s: String -> index to s }.toMap().entries.associate { (k, v) -> v to k }

    val preprocessor = Preprocessor
    val testWordsAndTags = preprocessor.getTestWordsAndTags(vocabMap, File("src/data/WSJ_24.pos"))
    val testWords = testWordsAndTags.map {it.first}
    val testTags = testWordsAndTags.map {it.second}


    val hmm = HMM(trainingCorpus, vocabMap)
    println(hmm.score(testWords,testTags))

}