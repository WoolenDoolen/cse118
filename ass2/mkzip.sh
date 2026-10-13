#!/bin/bash
zip=`pwd | awk -F '/' '{print $(NF-1)".zip"}'`
echo $zip
rm ../$zip
./gradlew clean
rm -rf app/build
rm -rf build
zip -x "*.DS_Store" \
  -x "*.gitignore" \
  -x "*.idea" \
  -x "local.properties" \
  -r ../$zip * 
