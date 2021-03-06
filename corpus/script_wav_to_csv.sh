#!/bin/bash

for folder in 'dronevolant_nonbruite_enregistrement_perso'
do 
	mkdir $folder'_csv'
	cd $folder
	for file in `ls`
	do
		sox $file temp.dat && ( grep -v ";" temp.dat | awk '{ print $2 }' > '../'$folder'_csv/'$(basename $file .wav)'.csv') && rm temp.dat
	done
	cd ..
done
