#!/bin/sh

find . -name "*.svg" ! -name "formula*" -exec ./convert_file.sh {} mdpi 32 \;
find . -name "*.svg" ! -name "formula*" -exec ./convert_file.sh {} hdpi 48 \;
find . -name "*.svg" ! -name "formula*" -exec ./convert_file.sh {} xhdpi 64 \;
find . -name "*.svg" ! -name "formula*" -exec ./convert_file.sh {} xxhdpi 96 \;

./convert_file.sh ./ic_launcher.svg mdpi 48
./convert_file.sh ./ic_launcher.svg hdpi 72
./convert_file.sh ./ic_launcher.svg xhdpi 96
./convert_file.sh ./ic_launcher.svg xxhdpi 144
./convert_file.sh ./ic_launcher.svg xxxhdpi 192
