import org.jetbrains.multik.ndarray.data.get
import java.io.File

fun main(){
    val trainingCorpus = File("src/data/WSJ_02-21.pos").readLines()

    val vocab = File("src/data/vocab.txt").readLines()
    val vocabMap = vocab.mapIndexed { index: Int, s: String -> index + 1 to s }.toMap().entries.associate{(k,v)-> v to k}

    val testCorpus = File("src/data/WSJ_24.pos").readLines()
//    TODO: probably need to save the labels too

//    Extracting just the words, no tags, from the test data - for later testing purposes
    val preprocessor = Preprocessor
    val listOfTestWords = Preprocessor.getTestWords(vocabMap,File("src/data/WSJ_24.pos"))


    val hmm = HMM(trainingCorpus,vocabMap)

    val A = hmm.transitionMatrix
    val B = hmm.emissionProbsMatrix
//    val (bestProbs, bestPath) = hmm.initializeViterbiMatrices(A, B, listOf("I", "love", "to", "sleep"), vocabMap)


    println(A[0,0])
//    println(B[3,1])

}