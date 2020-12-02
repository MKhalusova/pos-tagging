import kotlin.math.ln
import org.jetbrains.multik.api.empty
import org.jetbrains.multik.api.mk
import org.jetbrains.multik.ndarray.data.D2
import org.jetbrains.multik.ndarray.data.Ndarray
import org.jetbrains.multik.ndarray.data.get
import org.jetbrains.multik.ndarray.data.set

class HMM() {
    //    transition counts - map of counts of <tag,tag> combinations
    private val transitionCounts = mutableMapOf<Pair<String, String>, Int>()
    //    emission counts - map of counts of <tag,word> combinations
    private val emissionCounts = mutableMapOf<Pair<String, String>, Int>()
    //  tag -> number of times this tag appeared in the training data
    private val tagCounts = mutableMapOf<String, Int>()

    fun calculateCounts(trainingCorpus: List<String>, vocab: Map<String, Int>) {
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
    ):Ndarray<Double, D2>
    {
//      Get a sorted list of unique POS tags
        val tags = tagCounts.keys.toList().sorted()

//      Count the number of unique POS tags
        val numberOfTags = tags.size
        val transitionMatrix = mk.empty<Double, D2>(numberOfTags, numberOfTags)

//      Go through each row and column of the transition matrix A
        for (i in 0 until numberOfTags) {
            for (j in 0 until numberOfTags) {
//              Define the tuple (prev POS, current POS)
                val key = Pair(tags[i], tags[j])
//              If the (prev POS, current POS) exists in the transition counts dictionary, change the count, otherwise don't
                val count = transitionCounts.getOrDefault(key, 0)
//              Get the count of the previous tag (index position i) from tag counts
                val countPrevTag = tagCounts[tags[i]]
//              Apply smoothing using count of the tuple, alpha,
                transitionMatrix[i, j] = (count + alpha) / (alpha * numberOfTags + countPrevTag!!)
            }
        }
        return transitionMatrix
    }

    fun createEmissionProbsMatrix(
        vocab: Map<String, Int>,
        alpha: Double = 0.001
    ): Ndarray<Double, D2> {
        val numberOfTags = tagCounts.size
        val tags = tagCounts.keys.toList().sorted()
        val numberOfWords = vocab.size

        val emissionProbsMatrix = mk.empty<Double, D2>(numberOfTags, numberOfWords)

        for (i in 0 until numberOfTags) {
            for (j in 0 until numberOfWords) {
                val key = Pair(tags[i], vocab.keys.toList()[j])
                val count = emissionCounts.getOrDefault(key, 0)
                val countTag = tagCounts[tags[i]]
                emissionProbsMatrix[i,j] = (count + alpha) / (alpha * numberOfWords + countTag!!)
            }
        }
        return emissionProbsMatrix
    }

    fun initializeViterbiMatrices(
        tagCounts: Map<String, Int>,
        transitionMatrix:Ndarray<Double, D2>,
        emissionMatrix: Ndarray<Double, D2>,
        sentence: List<String>,
        vocab: Map<String, Int>
    ): Pair<Ndarray<Double, D2>, Ndarray<Double, D2>> {
//        will return two matrices: C = best probabilities (num of states x num of words in sentence) and
//        D = best paths (num of states x num of words in sentence)

        val states = tagCounts.keys.toList().sorted()
        val bestProbs = mk.empty<Double, D2>(states.size, sentence.size)
        val bestPaths = mk.empty<Double, D2>(states.size, sentence.size)

        val startIdx = states.indexOf("--s--")
//        to initialize the matrices, we need to fill in the first column
        for (i in 0 until states.size) {
            if (transitionMatrix[0,i] == 0.0) {
                bestProbs[i,0] = Double.NEGATIVE_INFINITY
            }
            else {
                bestProbs[i,0] = ln(transitionMatrix[startIdx, i]) + ln(emissionMatrix[i, vocab[sentence[0]]!!])
            }
        }
        return Pair(bestProbs, bestPaths)
    }

//    fun forwardPass(){
//
//    }

    fun train(trainingCorpus: List<String>, vocab: Map<String, Int>) {
        calculateCounts(trainingCorpus, vocab)
        val transitionMatrix = createTransitionMatrix()
        val emissionMatrix = createEmissionProbsMatrix(vocab)
    }

//    fun predictPOS(sentence: List<String>){
//
//    }

}

