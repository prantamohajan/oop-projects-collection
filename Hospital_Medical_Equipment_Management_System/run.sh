#!/bin/bash
cd "$(dirname "$0")/src" && mkdir -p ../out && javac -encoding UTF-8 -d ../out *.java && java -cp ../out HospitalGUI
