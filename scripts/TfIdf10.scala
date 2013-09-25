import com.twitter.scalding._
import com.twitter.scalding.mathematics.Matrix

/*
 * TfIdf10.scala
 *
 * Adapted from "MatrixTutorial6" in the tutorials that come with Scalding.
 *
 * Loads a document to word matrix where a[i,j] = freq of the word j in the document i 
 * computes the Tf-Idf score of each word w.r.t. to each document and keeps the top N words in each document
 * (see http://en.wikipedia.org/wiki/Tf*idf for more info)
 * 
 * You invoke the script like this:
 *   ./run scripts/TfIdf10.scala \
 *     --input data/matrix/docBOW.tsv \
 *     --output output/featSelectedMatrix.tsv \
 *     --nWords 300
 */
class TfIdf10(args : Args) extends Job(args) {
  
  val n = args("nWords").toInt 

  import Matrix._

  val docSchema = ('docId, 'word, 'count)

  val docWordMatrix = Tsv( args("input"), docSchema )
    .read
    .toMatrix[Long,String,Double](docSchema)

  // Compute the overall document frequency of each word.
  // docFreq(i) will be the total count for word i over all docs.
  val docFreq = docWordMatrix.sumRowVectors

  // Compute the inverse document frequency vector.
  // L1 normalize the docFreq: 1/(|a| + |b| + ...)
  // Use 1/log(x), rather than 1/x, for better numerical stability. 
  val invDocFreqVct = 
    docFreq.toMatrix(1).rowL1Normalize.mapValues( x => log2(1/x) )

  // Zip the row vector along the entire document - word matrix.
  val invDocFreqMat = 
    docWordMatrix.zip(invDocFreqVct.getRow(1)).mapValues(_._2)

  // Multiply the term frequency with the inverse document frequency
  // and keep the top N words. "hProd" is the Hadamard product, i.e.,
  // multiplying elementwise, rather than row vector times column vector.
  docWordMatrix.hProd(invDocFreqMat).topRowElems(n).write(Tsv(args("output")))

  def log2(x : Double) = scala.math.log(x)/scala.math.log(2.0)
}

