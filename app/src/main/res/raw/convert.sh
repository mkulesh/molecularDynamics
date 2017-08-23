#!/bin/sh

find . -name "*.svg" ! -name "formula*" -exec ./convert_single.sh {} \;

./convert_file.sh ./ic_launcher.svg mdpi 48
./convert_file.sh ./ic_launcher.svg hdpi 72
./convert_file.sh ./ic_launcher.svg xhdpi 96
./convert_file.sh ./ic_launcher.svg xxhdpi 144
./convert_file.sh ./ic_launcher.svg xxxhdpi 192
