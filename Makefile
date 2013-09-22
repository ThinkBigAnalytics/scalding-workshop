# Makefile to build releases, etc.

VERSION = 0.2.1
RELEASE = scalding-workshop-${VERSION}
DISTRO_ZIP = ${RELEASE}.zip
DISTRO_TGZ = ${RELEASE}.tgz

DISTRO_DIRS  = data images lib scripts src
DISTRO_FILES = LICENSE.txt Makefile api.zip \
  run run.rb runall.sh run11.sh \
  README.md README.html Workshop.md Workshop.html

all: clean stage build-release

clean:
	rm -rf classes ${RELEASE} ${DISTRO_ZIP} ${DISTRO_TGZ}

stage: ${RELEASE}
	@for d in ${DISTRO_DIRS}; \
	  do echo "Staging directory $$d"; \
	  cp -rf $$d ${RELEASE}/$$d; \
	done
	@for f in ${DISTRO_FILES}; \
	  do echo "Staging file $$f"; \
	  cp $$f ${RELEASE}; \
	done
	@mkdir -p ${RELEASE}/project
	@cp project/{Build.scala,build.properties,plugins.sbt} ${RELEASE}/project

${RELEASE}:
	mkdir -p $@

build-release:
	tar czf ${DISTRO_TGZ} ${RELEASE}
	zip -r ${DISTRO_ZIP} ${RELEASE}
