#!/usr/bin/env bash
# Run all the scripts. Mostly used as a sanity check.
# Run runall.sh -h for details.

usage() {
  cat <<-EOF
$0: Run all the scripts. Mostly used as a sanity check.
usage:
  $0 [-h | --help] [--hadoop [--local]]
where:
  -h | --help  Show this message
  --hadoop     Run the scripts that use Hadoop.
  --local      If running Hadoop scripts, assume local mode.
               (Only suppresses putting data in HDFS)
Use NOOP=echo $0 ... to echo the commands that would be executed.
EOF
}

let dohadoop=0
let dolocal=0
while [ $# -gt 0 ]
do
  case $1 in
    -h|--he)
      usage
      exit 0
      ;;
    --ha*)
      let dohadoop=1
      ;;
    --l*)
      let dolocal=1
      ;;
    *)
      echo "$0: unrecognized option $1"
      usage
      exit 1
      ;;
  esac
  shift
done

if [ -d output ]
then
  now=$(date +"%Y%m%d-%H%S%M")
  echo "Moving old output directory to output.$now"
  $NOOP mv output output.$now
fi

echo "SanityCheck0"
$NOOP ./run.rb scripts/SanityCheck0.scala

echo "Project1"
$NOOP ./run.rb scripts/Project1.scala

echo "WordCount2"
$NOOP ./run.rb scripts/WordCount2.scala \
  --input  data/shakespeare/plays.txt \
  --output output/shakespeare-wc.txt

echo "StockAverages3"
$NOOP ./run.rb scripts/StockAverages3.scala \
  --input  data/stocks/AAPL.csv \
  --output output/AAPL-year-avg.txt

echo "StocksDividendsJoin4"
$NOOP ./run.rb scripts/StocksDividendsJoin4.scala \
  --stocks data/stocks/AAPL.csv \
  --dividends data/dividends/AAPL.csv \
  --output output/AAPL-stocks-dividends-join.txt

echo "StockCoGroup5"
$NOOP run.rb scripts/StockCoGroup5.scala \
  --input  data/stocks \
  --output output/AAPL-INTC-GE-IBM.txt

echo "Twitter6"
$NOOP run.rb scripts/Twitter6.scala \
  --input  data/twitter/tweets.tsv \
  --uniques output/unique-languages.txt \
  --count_star output/count-star.txt \
  --count_star_limit output/count-star-limit.txt

echo "ContextNGrams7"
$NOOP run.rb scripts/ContextNGrams7.scala \
  --input  data/shakespeare/plays.txt \
  --output output/context-ngrams.txt \
  --ngram-prefix "I love" \
  --count 10

echo "StocksDividendsRevisited8"
$NOOP run.rb scripts/StocksDividendsRevisited8.scala \
  --stocks-root-path    data/stocks/ \
  --dividends-root-path data/dividends/ \
  --symbols AAPL,INTC,GE,IBM \
  --output output/stocks-dividends-join.txt

echo "MatrixJaccardSimilarity9"
$NOOP run.rb scripts/MatrixJaccardSimilarity9.scala \
  --input data/matrix/graph.tsv \
  --output output/jaccardSim.tsv

echo "TfIdf10"
$NOOP run.rb scripts/TfIdf10.scala \
  --input data/matrix/docBOW.tsv \
  --output output/featSelectedMatrix.tsv \
    --nWords 300

# Assumes the Scalding distro is in a sister directory to this one!
if [ $dohadoop = 1 ]
then
  echo "HadoopTwitter11 - Use Hadoop!"
  test $dolocal = 1 && $NOOP hadoop fs -put data data
  $NOOP ../scalding/scripts/scald.rb --hdfs-local --host localhost \
    scripts/HadoopTwitter11.scala \
    --input  data/twitter/tweets.tsv \
    --uniques output/unique-languages \
    --count_star output/count-star \
    --count_star_limit output/count-star-limit
fi