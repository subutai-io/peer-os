#!/usr/bin/make -f

PACKAGE_NAME=subutai-cli-management
BUILD_DIR=$(PACKAGE_NAME)-debian-build

all: package


clean:
	@rm -rf $(BUILD_DIR)
	@rm -rf debian/$(PACKAGE_NAME)

package:
	@debian/update-version.sh
	@dpkg-buildpackage -us -uc -rfakeroot  2>/dev/null
	@if [ ! -d ${BUILD_DIR} ]; then mkdir ${BUILD_DIR}; fi
	@mv ../*$(PACKAGE_NAME)_* ${BUILD_DIR}
	@rm -rf debian/$(PACKAGE_NAME)

install: 
	@echo Installing $(PACKAGE_NAME)
	dpkg -i ${BUILD_DIR}/$(PACKAGE_NAME)*.deb

