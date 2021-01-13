import org.jetbrains.kotlinx.multik.api.empty
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.Ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import kotlin.math.ln

class HMM(val trainingCorpus: List<String>, val vocab: Map<String, Int>) {

    private val transitionCounts = mutableMapOf<Pair<String, String>, Int>()
    private val emissionCounts = mutableMapOf<Pair<String, String>, Int>()
    private val tagCounts = mutableMapOf<String, Int>()

    private var NUMBER_OF_TAGS = 0
    val NUMBER_OF_WORDS = vocab.size

    lateinit var transitionMatrix: Ndarray<Double, D2>
    lateinit var emissionProbsMatrix: Ndarray<Double, D2>

    init {
        calculateCounts()
        NUMBER_OF_TAGS = tagCounts.size
        createTransitionMatrix()
        createEmissionProbsMatrix()
    }

    private fun calculateCounts() {
        val preprocessor = Preprocessor
        var previousTag = "--s--"
        for (line in trainingCorpus) {
            val (word, tag) = preprocessor.getWordAndTagFromLine(line, vocab)
            transitionCounts[Pair(previousTag, tag)] = transitionCounts.getOrDefault(Pair(previousTag, tag), 0) + 1
            emissionCounts[Pair(tag, word)] = emissionCounts.getOrDefault(Pair(tag, word), 0) + 1
            tagCounts[tag] = tagCounts.getOrDefault(tag, 0) + 1
            previousTag = tag
        }
    }

    fun createTransitionMatrix(
        alpha: Double = 0.001,
    ) {
//      Get a sorted list of unique POS tags
        val tags = tagCounts.keys.toList().sorted()

//      Count the number of unique POS tags
        transitionMatrix = mk.empty(NUMBER_OF_TAGS, NUMBER_OF_TAGS)

//      Go through each row and column of the transition matrix A
        for (i in 0 until NUMBER_OF_TAGS) for (j in 0 until NUMBER_OF_TAGS) {
//              Define the tuple (prev POS, current POS)
            val key = Pair(tags[i], tags[j])
//              If the (prev POS, current POS) exists in the transition counts dictionary, change the count, otherwise don't
            val count = transitionCounts.getOrDefault(key, 0)
//              Get the count of the previous tag (index position i) from tag counts
            val countPrevTag = tagCounts[tags[i]]
//              Apply smoothing using count of the tuple, alpha,
            transitionMatrix[i, j] = (count + alpha) / (alpha * NUMBER_OF_TAGS + countPrevTag!!)
        }
    }

    fun createEmissionProbsMatrix(
        alpha: Double = 0.001
    ) {
        val tags = tagCounts.keys.toList().sorted()

        emissionProbsMatrix = mk.empty(NUMBER_OF_TAGS, NUMBER_OF_WORDS)
        val reversedVocab = vocab.entries.associate { (k, v) -> v to k }

        for (i in 0 until NUMBER_OF_TAGS) for (j in 0 until NUMBER_OF_WORDS) {

            val key = Pair(tags[i], reversedVocab[j])
            val count = emissionCounts.getOrDefault(key, 0)
            val countTag = tagCounts[tags[i]]
            emissionProbsMatrix[i, j] = (count + alpha) / (alpha * NUMBER_OF_WORDS + countTag!!)
        }
    }

    fun initializeViterbiMatrices(
        sentence: List<String>,
    ): Pair<Ndarray<Double, D2>, Ndarray<Int, D2>> {
//        will return two matrices: C = best probabilities (num of states x num of words in sentence) and
//        D = best paths (num of states x num of words in sentence)

        val tags = tagCounts.keys.toList().sorted()
        val bestProbs = mk.empty<Double, D2>(NUMBER_OF_TAGS, sentence.size)
        val bestPaths = mk.empty<Int, D2>(NUMBER_OF_TAGS, sentence.size)

        val startIdx = tags.indexOf("--s--")
//        to initialize the matrices, we need to fill in the first column
        for (i in 0 until NUMBER_OF_TAGS) {
            if (transitionMatrix[0, i] == 0.0) {
                bestProbs[i, 0] = Double.NEGATIVE_INFINITY
            } else {
                bestProbs[i, 0] = ln(transitionMatrix[startIdx, i]) + ln(emissionProbsMatrix[i, vocab[sentence[0]]!!])
            }
        }
        return Pair(bestProbs, bestPaths)
    }

    fun viterbiForward(
        sentence: List<String>,
        bestProbs: Ndarray<Double, D2>,
        bestPaths: Ndarray<Int, D2>
    ): Pair<Ndarray<Double, D2>, Ndarray<Int, D2>> {

        val updatedProbs = bestProbs
        val updatedPaths = bestPaths

        for (i in 1 until sentence.size) for (j in 0 until NUMBER_OF_TAGS) {

            var bestProbabilityToGetToWordIFromTagJ = Double.NEGATIVE_INFINITY
            var bestPathToWordI = 0


            for (k in 0 until NUMBER_OF_TAGS) {

                val temp_prob =
                    updatedProbs[k, i - 1] + ln(transitionMatrix[k, j]) + ln(emissionProbsMatrix[j, vocab[sentence[i]]!!])

                if (temp_prob > bestProbabilityToGetToWordIFromTagJ) {
                    bestProbabilityToGetToWordIFromTagJ = temp_prob
                    bestPathToWordI = k
                }
            }
            updatedProbs[j, i] = bestProbabilityToGetToWordIFromTagJ
            updatedPaths[j, i] = bestPathToWordI
        }
        return Pair(updatedProbs, updatedPaths)
    }

    fun viterbiBackward(
        sentence: List<String>,
        bestProbs: Ndarray<Double, D2>,
        bestPaths: Ndarray<Int, D2>
    ): List<String> {
        val m = sentence.size
        val z = IntArray(m)
        var bestProbForLastWord = Double.NEGATIVE_INFINITY
        val tags = tagCounts.keys.toList().sorted()

        val posPredictions = mutableListOf<String>()

        for (k in 0 until NUMBER_OF_TAGS) {
//            finding the index of the maximum probability in the last column of the bestProbs
            if (bestProbs[k, m - 1] > bestProbForLastWord) {
                bestProbForLastWord = bestProbs[k, m - 1]
                z[m - 1] = k
            }
        }
        posPredictions.add(tags[z[m - 1]])

        for (i in m - 1 downTo 1) {
            val tagForWordI = bestPaths[z[i], i]
            z[i - 1] = tagForWordI
            posPredictions.add(tags[tagForWordI])
        }
        return posPredictions.toList().reversed()
    }

    fun predictPOSSequence(sentence: List<String>): List<String> {
        val (initialBestProbs, initialBestPaths) = initializeViterbiMatrices(sentence)
        val (updatedBestProbs, updatedBestPaths) = viterbiForward(sentence, initialBestProbs, initialBestPaths)
        return viterbiBackward(sentence, updatedBestProbs, updatedBestPaths)
    }


//    for score(test corpus):
//    split test_corpus into (word, tag) pairs
//    predictPOSSequence for words
//    compare the word list and the tag list


    fun score(testWords: List<String>, testTags: List<String>): Double {
        val predictions = this.predictPOSSequence(testWords)
        val numberOfCorrectPredictions = predictions.zip(testTags).count { it.first == it.second }

        return numberOfCorrectPredictions.toDouble() / predictions.size
    }

}

