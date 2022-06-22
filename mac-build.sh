#!/bin/bash
rm -rf ../turbo-build/out-mac

java -jar ../turbo-build/packr-all-4.0.0.jar \
     --platform mac \
     --jdk ../turbo-build/mac.tar.gz \
     --useZgcIfSupportedOs \
     --executable moist-region \
     --classpath ./lwjgl3/build/libs/moist-region-0.0.1.jar \
     --mainclass moist.core.lwjgl3.Lwjgl3Launcher \
     --vmargs Xmx1G XstartOnFirstThread \
     --resources assets/* \
     --output ../turbo-build/out-mac

butler push ../turbo-build/out-mac/ lavaeater/moist-region:mac

