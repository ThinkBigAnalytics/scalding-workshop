import com.twitter.scalding._
import com.twitter.scalding.mathematics.Matrix

/*
 * TfIdf10.scala
 *
 * Adapted from "MatrixTutorial6" in the tutorials that come with Scalding.
 *
 * Loads a document to word matrix where a[i,j] = freq of the word j in the document i 
 * computes the Tf-Idf score of each word w.r.t. to each document and keeps the top nrWords in each document
 * (see http://en.wikipedia.org/wiki/Tf*idf for more info)
 * 
 * You invoke the script like this:
 *   run.rb scripts/TfIdf10.scala \
 *     --input data/matrix/docBOW.tsv \
 *     --output output/featSelectedMatrix.tsv \
 *     --nWords 300
 */
class TfIdf10(args : Args) extends Job(args) {
  
  import Matrix._

  val docWordMatrix = Tsv( args("input"), ('doc, 'word, 'count) )
    .read
    .toMatrix[Long,String,Double]('doc, 'word, 'count)

  // compute the overall document frequency of each row
  val docFreq = docWordMatrix.sumRowVectors

  // compute the inverse document frequency vector
  val invDocFreqVct = docFreq.toMatrix(1).rowL1Normalize.mapValues( x => log2(1/x) )

  // zip the row vector along the entire document - word matrix
  val invDocFreqMat = docWordMatrix.zip(invDocFreqVct.getRow(1)).mapValues( pair => pair._2 )

  // multiply the term frequency with the inverse document frequency and keep the top nrWords
  docWordMatrix.hProd(invDocFreqMat).topRowElems( args("nWords").toInt ).write(Tsv( args("output") ))

  def log2(x : Double) = scala.math.log(x)/scala.math.log(2.0)

}

