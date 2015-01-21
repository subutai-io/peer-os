#!/usr/bin/make -f

BUILD_DIR=subutai-cli-unprivileged-debian-build

all: package


clean:
	@rm -rf $(BUILD_DIR)
	@rm -rf debian/subutai-cli-unprivileged

package:
	#@debian/update-version.sh
	dpkg-buildpackage -us -uc -rfakeroot
	@if [ ! -d ${BUILD_DIR} ]; then mkdir ${BUILD_DIR}; fi
	@mv ../*subutai-cli-unprivileged_* ${BUILD_DIR}
	@rm -rf debian/subutai-cli-unprivileged

install: 
	@echo Installing subutai-cli-unprivileged
	dpkg -i ${BUILD_DIR}/*.deb

