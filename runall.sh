#!/usr/bin/env bash
# Run all the scripts! Mostly used as a sanity check.

if [ -d output ]
then
  now=$(date +"%Y%m%d-%H%S%M")
  echo "Moving old output directory to output.$now"
  mv output output.$now
fi

echo "SanityCheck0"
./run.rb scripts/SanityCheck0.scala

echo "Project1"
./run.rb scripts/Project1.scala

echo "WordCount2"
./run.rb scripts/WordCount2.scala \
  --input  data/shakespeare/plays.txt \
  --output output/shakespeare-wc.txt

echo "StockAverages3"
./run.rb scripts/StockAverages3.scala \
  --input  data/stocks/AAPL.csv \
  --output output/AAPL-year-avg.txt

echo "StocksDividendsJoin4"
./run.rb scripts/StocksDividendsJoin4.scala \
  --stocks data/stocks/AAPL.csv \
  --dividends data/dividends/AAPL.csv \
  --output output/AAPL-stocks-dividends-join.txt

echo "StockCoGroup5"
run.rb scripts/StockCoGroup5.scala \
  --input  data/stocks \
  --output output/AAPL-INTC-GE-IBM.txt

echo "Twitter6"
run.rb scripts/Twitter6.scala \
  --input  data/twitter/tweets.tsv \
  --uniques output/unique-languages.txt \
  --count_star output/count-star.txt \
  --count_star_limit output/count-star-limit.txt

echo "ContextNGrams7"
run.rb scripts/ContextNGrams7.scala \
  --input  data/shakespeare/plays.txt \
  --output output/context-ngrams.txt \
  --ngram-prefix "I love" \
  --count 10

echo "StocksDividendsRevisited8"
run.rb scripts/StocksDividendsRevisited8.scala \
  --stocks-root-path    data/stocks/ \
  --dividends-root-path data/dividends/ \
  --symbols AAPL,INTC,GE,IBM \
  --output output/stocks-dividends-join.txt

echo "MatrixJaccardSimilarity9"
run.rb scripts/MatrixJaccardSimilarity9.scala \
  --input data/matrix/graph.tsv \
  --output output/jaccardSim.tsv

echo "TfIdf10"
run.rb scripts/TfIdf10.scala \
  --input data/matrix/docBOW.tsv \
  --output output/featSelectedMatrix.tsv \
    --nWords 300

echo "HadoopTwitter11 - Requires Hadoop!"
if [ -n "$HADOOP_HOME" ]  # hack
then
../scalding/scripts/scald.rb --hdfs-local --host localhost \
  scripts/HadoopTwitter11.scala \
  --input  data/twitter/tweets.tsv \
  --uniques output/unique-languages.txt \
  --count_star output/count-star.txt \
  --count_star_limit output/count-star-limit.txt
fi