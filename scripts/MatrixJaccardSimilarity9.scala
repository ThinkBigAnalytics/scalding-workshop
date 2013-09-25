import com.twitter.scalding._
import com.twitter.scalding.mathematics.Matrix

/*
 * MatrixJaccardSimilarity9.scala
 *
 * Adapted from "MatrixTutorial5" in the tutorials that come with Scalding.
 * We've added lots of explanatory comments, for example...
 *
 * Loads a directed graph adjacency matrix where a[i,j] = 1 if there is an 
 * edge from a[i] to b[j] and computes the jaccard similarity between any 
 * two pairs of vectors.
 * 
 * You invoke the script like this:
 *   ./run scripts/MatrixJaccardSimilarity9.scala \
 *     --input data/matrix/graph.tsv \
 *     --output output/jaccardSim.tsv
 */

class MatrixJaccardSimilarity9(args : Args) extends Job(args) {
  
  import Matrix._

  val adjacencyMatrix = Tsv(args("input"), ('user1, 'user2, 'rel))
    .read
    .toMatrix[Long,Long,Double]('user1, 'user2, 'rel)

  // Convert all nonzero elements to 1.0 double.
  val aBinary = adjacencyMatrix.binarizeAs[Double]
 
  // intersectMat holds the size of the intersection of row(a)_i n row (b)_j
  val intersectMat = aBinary * aBinary.transpose

  // The terminology is confusing, but sumColVectors returns a single
  // column vector where the each row is summed into a single element.
  // What's it mean? Element i in the output will be the total number
  // of nonzero directed edges from user i to other users.
  val colVectOfSums = aBinary.sumColVectors
  // ... sumRowVectors returns a single row vector where the columns 
  // are summed. Element i in the output will be the total number
  // of nonzero directed edges to user i from other users.
  val rowVectOfSums = aBinary.sumRowVectors

  // Use zip to repeat the row and column vectors values on the right hand
  // for all non-zeroes on the left hand matrix. The "non-zeroes" implies
  // that we should use (0,0) for the pair if the entry if the intersectMat
  // cell is 0, but actually, it appears to yield the same result if we use
  // (0,N), due to how the final multiplications and divisions work out.
  val xMat = intersectMat.zip(colVectOfSums).mapValues( pair => pair._2 )
  val yMat = intersectMat.zip(rowVectOfSums).mapValues( pair => pair._2 )
  
  // Compute the union: The following former subtracts the intersection to 
  // correct for over-counting in xMat + yMat.
  val unionMat = xMat + yMat - intersectMat

  // This comment in the twitter tutorail example doesn't make much sense:
  // "We are guaranteed to have Double both in the intersection and in the union matrix"
  // In fact, there are some issues here, as some union values are 0 while the
  // the intersections aren't, resulting in infinities.  
  intersectMat.zip(unionMat)
              .mapValues(pair => pair._1 / pair._2)
              .write(Tsv(args("output")))

}

