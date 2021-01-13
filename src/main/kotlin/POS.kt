import java.io.File

fun main() {
    val trainingCorpus = File("src/data/WSJ_02-21.pos").readLines()

    val vocab = File("src/data/vocab.txt").readLines()
    val vocabMap =
        vocab.mapIndexed { index: Int, s: String -> index to s }.toMap().entries.associate { (k, v) -> v to k }

//    preparing the test data
    val preprocessor = Preprocessor
    val testWordsAndTags = preprocessor.getTestWordsAndTags(vocabMap, File("src/data/WSJ_24.pos"))
    val testWords = testWordsAndTags.map { it.first }
    val testTags = testWordsAndTags.map { it.second }

//   "training" the Hidden Markov Model on the training data
    val hmm = HMM(trainingCorpus, vocabMap)

//    evaluating the HMM model on the test part of the corpus
    println(hmm.score(testWords, testTags))

}