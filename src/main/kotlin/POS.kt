import java.io.File

fun main() {
    val trainingCorpus = File("src/data/WSJ_02-21.pos").readLines()

    val vocab = File("src/data/vocab.txt").readLines()
    val vocabMap =
        vocab.mapIndexed { index: Int, s: String -> index + 1 to s }.toMap().entries.associate { (k, v) -> v to k }

//    val testCorpus = File("src/data/WSJ_24.pos").readLines()
//    TODO: implement accuracy score

//    Extracting just the words, no tags, from the test data - for later testing purposes
    val preprocessor = Preprocessor
    val listOfTestWords = preprocessor.getTestWords(vocabMap, File("src/data/WSJ_24.pos"))

    val s = listOfTestWords.slice(0..8)
    val hmm = HMM(trainingCorpus, vocabMap)
    val posPrediction = hmm.predictPOSSequence(s)
    println(posPrediction)

}