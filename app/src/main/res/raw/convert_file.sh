#!/bin/bash

NAME1=${1%.svg}
TARGET="../drawable-"${2}"/"${NAME1:2}".png"
echo Converting to ${TARGET}
inkscape --export-png ${TARGET} -w ${3} -h ${3} ${1}

