#!/usr/bin/env ruby
#-------------------------------------------
# run.rb - Simple driver for the Scalding workshop.
# usage: run.rb scaldingscript.scala [options]
#
# It verifies that you passed a Scalding script (a Scala source file)
# as an argument, compiles it, and invokes Scalding in "local" mode.
# Scalding comes with a more sophisticated driver script called "scald.rb".
# for example, scald.rb handles invoking Scalding scripts 
# This script is simpler and avoids some issues using "scald.rb".

# Increase (or decrease) this heap size value if necessary.
HEAP = "-Xmx1g"
SCALDING_VERSION = "0.7.3"
#LIBS = "lib/scalding-assembly-#{SCALDING_VERSION}.jar"
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
ARGV.shift
classfile = File.basename(script, ".scala")

now = Time.now.strftime("%Y%m%d-%H%S%M")
tmpnow = "tmp/#{now}"

status = 0
begin
	FileUtils.mkdir_p('classes') unless File.exists?('classes')
	unless File.exists?('classes/workshop/Csv.class')
		puts "Compiling Helper \"lib/Csv.scala\""
		run_command("scalac -cp '#{LIBS}' -d classes lib/Csv.scala")
	end

	FileUtils.mkdir_p(tmpnow)
	puts "Compiling script \"#{script}\""
	run_command("scalac -cp 'classes:#{LIBS}' -d #{tmpnow} #{script}")
	run_command("java #{HEAP} -cp 'classes:#{LIBS}:#{tmpnow}' com.twitter.scalding.Tool #{classfile} --local #{ARGV.join(" ")}")
rescue Exception => e
	puts "Exception #{e} raised!"
	status = 1
end
FileUtils.remove_dir(tmpnow)
exit(status)
