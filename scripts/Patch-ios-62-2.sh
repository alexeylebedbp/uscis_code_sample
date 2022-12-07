#!/bin/bash
#Author: Maxim Vovenko
#Patches ReactNative 62.2 So Release Builds are Successfull
THIS_DIR=$(cd -P "$(dirname "$(readlink "${BASH_SOURCE[0]}" || echo "${BASH_SOURCE[0]}")")" && pwd)
REACT_NATIVE_ROOT="$THIS_DIR/../node_modules/react-native"

if grep -q '"version": "0.62.2",' $REACT_NATIVE_ROOT/package.json
then
rm $REACT_NATIVE_ROOT/Libraries/Image/AssetSourceResolver.js
cp $THIS_DIR/AssetSourceResolver.js $REACT_NATIVE_ROOT/Libraries/Image/AssetSourceResolver.js
fi