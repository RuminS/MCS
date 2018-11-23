#!/bin/bash

cd enregistrement_perso
for files in `ls`
do
	sox $files temp.dat && ( grep -v ";" temp.dat | awk '{ print $2 }' > '../audio/'$(basename $files '.wav')'.csv') && rm temp.dat
done
