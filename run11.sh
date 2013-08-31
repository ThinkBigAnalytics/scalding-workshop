#!/usr/bin/env bash
# Run just the HadoopTwitter11 script, which requires Hadoop to be installed.
# Note that an alternative is to use Scalding's scald.rb script with just the
# "script" file, e.g.,:
# $SCALDING_HOME/scripts/scald.rb --hdfs-local --host $host \
#    src/main/scala/HadoopTwitter11.scala \
#    --input   data/twitter/tweets.tsv \
#    --uniques output/unique-languages \
#    --count-star output/count-star \
#    --count-star-100 output/count-star-100

usage() {
    cat <<EOF
usage $0 [-h|--help] [--host host] [--hdfs|--local|--print] [--root rootpath]
where:
  --host host       is the JobTracker host (default: localhost)
  --root rootpath   is the parent directory for the input and output (default ".")
  --hdfs            run in a Hadoop cluster. You'll need to copy the input files
                    into HDFS and create the output directory and you might want to 
                    use the --root path argument to specify a "root" (or parent)
                    directory for both. E.g.., use the following commands, where 
                    we'll also assume you'll use "--root /user/me/twitter":
                      hdfs -mkdir /user/me/twitter/data 
                      hdfs -mkdir /user/me/twitter/output 
                      hdfs -put data/twitter /user/me/twitter/data 
  --local           Use Cascading's local mode that bypasses MR altogether.
                    (default)
  --print           Print what you'll do, but don't do anything 

EOF
}

assembly=$(ls target/ScaldingWorkshop*.jar 2> /dev/null)
if [ $? -ne 0 ]
then
    echo "$0: The target/ScaldingWorkshop-X.Y.Z.jar hasn't been built yet."
    echo "  Run 'sbt assembly' then try again."
    exit 1
fi
script=HadoopTwitter11

host=localhost
file_system_arg=--local
root=
args=()
while [ $# -ne 0 ]
do
    case $1 in 
        -h|--help)
            usage
            exit 0
            ;;
        --root)
            shift
            root="$1/"
            ;;
        --host)
            shift
            host=$1
            ;;
        --hdfs|--local|--print)
            file_system_arg=$1
            ;;
        *)
            args[${#args[@]}]="$1"
            ;;
    esac
    shift
done

runit() {
$@ hadoop jar $assembly $script \
    $file_system_arg --host $host "${args[@]}" --scalaversion 2.10 \
    --input ${root}data/twitter/tweets.tsv \
    --uniques ${root}output/unique-languages \
    --count-star ${root}output/count-star \
    --count-star-100 ${root}output/count-star-100
}
runit echo running
test -z "$NOOP" && runit