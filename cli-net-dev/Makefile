#!/usr/bin/make -f

BUILD_DIR=subutai-cli-net-dev-debian-build

all: package


clean:
	@rm -rf $(BUILD_DIR)
	@rm -rf debian/subutai-cli-net-dev

package:
	@debian/update-version.sh
	dpkg-buildpackage -us -uc -rfakeroot
	@if [ ! -d ${BUILD_DIR} ]; then mkdir ${BUILD_DIR}; fi
	@mv ../*subutai-cli-net-dev_* ${BUILD_DIR}
	@rm -rf debian/subutai-cli-net-dev

install: 
	@echo Installing subutai-cli-net-dev
	dpkg -i ${BUILD_DIR}/*.deb

