#!/usr/bin/make -f

BUILD_DIR=subutai-cli-dev-debian-build

all: package


clean:
	@rm -rf $(BUILD_DIR)
	@rm -rf debian/subutai-cli-dev

package:
	#@debian/update-version.sh
	dpkg-buildpackage -us -uc -rfakeroot
	@if [ ! -d ${BUILD_DIR} ]; then mkdir ${BUILD_DIR}; fi
	@mv ../*subutai-cli-dev_* ${BUILD_DIR}
	@rm -rf debian/subutai-cli-dev

install: 
	@echo Installing subutai-cli-dev
	dpkg -i ${BUILD_DIR}/*.deb

