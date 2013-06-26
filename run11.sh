#!/usr/bin/env bash
# Run just the HadoopTwitter11 script, which requires Hadoop.
# Assumes the Scalding distro is in a sister directory to this one!

usage() {
    cat <<EOF
usage $0 [-h|--help] [--host host] [--hdfs|--hdfs-local|--local|--print]
where:
  --host host is the JobTracker host (default: localhost)
  --hdfs|--hdfs-local|--local|--print options are passed to scald.rb
    (default --hdfs-local)
EOF
}

host=localhost
file_system_arg=--hdfs-local
args=()
while [ $# -ne 0 ]
do
    case $1 in 
        -h|--help)
            usage
            exit 0
            ;;
        --host)
            shift
            host=$1
            ;;
        --hdfs|--hdfs-local|--local|--print)
            file_system_arg=$1
            ;;
        *)
            args[${#args[@]}]="$1"
            ;;
    esac
    shift
done

runit() {
$@ ../scalding/scripts/scald.rb $file_system_arg --host $host "${args[@]}" \
    scripts/HadoopTwitter11.scala \
    --input  data/twitter/tweets.tsv \
    --uniques output/unique-languages \
    --count_star output/count-star \
    --count_star_limit output/count-star-limit
}
runit echo running
test -z "$NOOP" && runit