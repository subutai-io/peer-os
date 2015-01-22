#!/usr/bin/make -f

BUILD_DIR=subutai-cli-debian-build

all: package


clean:
	@rm -rf $(BUILD_DIR)
	@rm -rf debian/subutai-cli

package:
	@debian/update-version.sh
	dpkg-buildpackage -us -uc -rfakeroot
	@if [ ! -d ${BUILD_DIR} ]; then mkdir ${BUILD_DIR}; fi
	@mv ../*subutai-cli_* ${BUILD_DIR}
	@rm -rf debian/subutai-cli

install: 
	@echo Installing subutai-cli
	dpkg -i ${BUILD_DIR}/*.deb

