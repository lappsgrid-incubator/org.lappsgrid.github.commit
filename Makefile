
INSTALL_DIR=$(HOME)/bin
VERSION=$(shell cat VERSION)

help:
	@echo "GOALS"
	@echo 
	@echo "clean   - removes artifacts from previous builds."
	@echo "jar     - builds the jar file."
	@echo "install - installs the script and jar to $(INSTALL_DIR)"
	@echo "release - creates a tarball ready to be uploaded."
	@echo "upload  - uploads the files to the ANC server."
	@echo "commit  - creates a PR in the vocabulary pages repository."
	@echo

clean:
	mvn clean

jar:
	mvn package

install:
	cat ghc | sed 's/__VERSION__/'$(VERSION)'/' > $(INSTALL_DIR)/ghc
	chmod a+x $(INSTALL_DIR)/ghc
	cp target/ghc-$(VERSION).jar $(INSTALL_DIR)

release: 
	if [ -e target/dist ] ; then rm -rf target/dist ; fi
	mkdir target/dist
	cp target/ghc-$(VERSION).jar target/dist
	cat ghc | sed 's/__VERSION__/'$(VERSION)'/' > target/dist/ghc
	cd target/dist && tar czf ghc-$(VERSION).tgz ghc ghc-$(VERSION).jar

upload:
	scp -P 22022 target/dist/ghc-$(VERSION).tgz anc.org:/home/www/anc/downloads
	scp -P 22022 target/dist/ghc-$(VERSION).tgz anc.org:/home/www/anc/downloads/ghc-latest.tgz

ifeq ($(TOKEN),)
commit:
	@echo "Please set TOKEN to your GitHub API token."
else
commit:
	ghc -f vocabulary.commit -t $TOKEN
endif
