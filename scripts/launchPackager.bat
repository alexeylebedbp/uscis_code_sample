:: Copyright (c) Facebook, Inc. and its affiliates.
::
:: This source code is licensed under the MIT license found in the
:: LICENSE file in the root directory of this source tree.

@echo off
title Metro Bundler
call .packager.bat
node "%~dp0..\cli.js" --reactNativePath ../ --projectRoot ../../../ start
pause
exit
