#!/bin/bash
rm -rf ../turbo-build/out-win

java -jar ../turbo-build/packr-all-4.0.0.jar \
     --platform windows64 \
     --jdk ../turbo-build/windows.zip \
     --useZgcIfSupportedOs \
     --executable moist-region \
     --classpath ./lwjgl3/build/libs/moist-region-0.0.1.jar \
     --mainclass moist.core.lwjgl3.Lwjgl3Launcher \
     --vmargs Xmx1G XstartOnFirstThread \
     --resources assets/* \
     --output ../turbo-build/out-win

butler push ../turbo-build/out-win lavaeater/moist-region:win
