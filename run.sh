#!/usr/bin/env bash 
# run.sh - Simple script that runs Scalding on the workshop files.
# Contrast with $SCALDING_HOME/scripts/scald.rb, which is more full featured, 
# but also more restrictive for our purposes.

script=$1
if [ -z "$script" ]
then
	echo "Must specify a Scalding script!"
	exit 1
fi
shift

x=$(basename $script)
classfile=${x%.scala}
now=$(date +'%Y%m%d-%H%S%M')
$NOOP mkdir -p tmp/$now
echo "Compiling script \"$script\""
$NOOP scalac -cp 'lib/*' -d tmp/$now $script
$NOOP java -Xmx3g -cp 'lib/*:'tmp/$now com.twitter.scalding.Tool $classfile --local "$@"

$NOOP rm -rf tmp/$now
