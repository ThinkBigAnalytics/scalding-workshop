package workshop

import com.twitter.scalding._
import cascading.tuple.Fields

/*
 * Scalding has a built-in Tsv type for tab-separated values, but not a 
 * corresponding Csv type for comma-separated values. So, we'll create one
 * that is declared in a way similar to Tsv...
 */

case class Csv(p: String, f: Fields = Fields.ALL, 
               skipFileHeader: Boolean = false, writeFileHeader: Boolean = false) extends FixedPathSource(p)
  with DelimitedScheme {
    override val separator = ","
    override val fields = f
    override val skipHeader = skipFileHeader
    override val writeHeader = writeFileHeader
}
