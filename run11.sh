#!/usr/bin/env bash
# Run just the HadoopTwitter11 script, which requires Hadoop.
# Assumes the Scalding distro is in a sister directory to this one!

$NOOP ../scalding/scripts/scald.rb --hdfs-local --host localhost \
    scripts/HadoopTwitter11.scala \
    --input  data/twitter/tweets.tsv \
    --uniques output/unique-languages \
    --count_star output/count-star \
    --count_star_limit output/count-star-limit
 