#!/usr/bin/env ruby
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
	FileUtils.mkdir_p(tmpnow)
	puts "Compiling script \"#{script}\""
	run_command("scalac -cp 'lib/*' -d #{tmpnow} #{script}")
	run_command("java -Xmx3g -cp 'lib/*:'#{tmpnow} com.twitter.scalding.Tool #{classfile} --local #{ARGV}")
rescue Exception => e
	puts "Exception #{e} raised!"
	status = 1
end
FileUtils.remove_dir(tmpnow)
exit(status)
