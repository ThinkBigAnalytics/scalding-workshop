#!/usr/bin/env ruby
#-------------------------------------------
# run.rb - Simple driver for the Scalding workshop.
# usage: run.rb scaldingscript.scala [options]
#
# It verifies that you passed the name of a Scalding script (a Scala source file)
# as an argument, compiles it, and invokes Scalding in "local" mode.
# Scalding comes with a more sophisticated driver script called "scald.rb".
# For example, scald.rb handles invoking Scalding scripts as Hadoop jobs.
# This script is simpler and avoids some issues using "scald.rb".

# Increase (or decrease) this heap size value if necessary.
HEAP = "-Xmx1g"
VERSION = "0.3.0"
ASSEMBLY = "target/ScaldingWorkshop-#{VERSION}.jar"
LIBS = "lib/*"

$LOAD_PATH << File.join(File.expand_path(File.dirname(File.symlink?(__FILE__) ? File.readlink(__FILE__) : __FILE__)), 'lib')

require 'fileutils'

def run_command(command)
	puts "Running: #{command}"
	`#{command}`
	unless $?.success?
		puts "Failed!"
		exit(1)
	end
end

# TODO: Use a real option processor.
if ARGV.length == 0
	puts "Must specify a Scalding script!"
	exit(1)
end
script = ARGV[0]
unless script =~ /\//
	script="scripts/#{script}"
end
ARGV.shift
classfile = File.basename(script, ".scala")

now = Time.now.strftime("%Y%m%d-%H%S%M")
tmpnow = "tmp/#{now}"

status = 0
begin
	unless File.exists?(ASSEMBLY)
		puts "You must build the all-inclusive 'assembly' (#{ASSEMBLY}) first."
		puts "See the README for instructions."
		exit(1)
	end

	FileUtils.mkdir_p(tmpnow)
	puts "Compiling script \"#{script}\""
	run_command("scalac -deprecation -cp 'classes:#{ASSEMBLY}:#{LIBS}' -d #{tmpnow} #{script}")
	run_command("java #{HEAP} -cp 'classes:#{ASSEMBLY}:#{LIBS}:#{tmpnow}' com.twitter.scalding.Tool #{classfile} --local #{ARGV.join(" ")}")
rescue Exception => e
	puts "Exception #{e} raised!"
	status = 1
ensure
	FileUtils.remove_dir(tmpnow) if File.exists?(tmpnow)
end
exit(status)
