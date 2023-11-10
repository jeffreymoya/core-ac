#!/bin/sh

# build full
rm -rf ./builds/full
mkdir ./builds/full
copier copy . ./builds/full --trust --force --answers-file ../.copier-answers-full.yml
cp ./builds/full/.copier-answers-full.yml ./builds/.copier-answers-full.yml

# build min
rm -rf ./builds/min
mkdir ./builds/min
copier copy . ./builds/min --trust --force --answers-file ../.copier-answers-min.yml
cp ./builds/min/.copier-answers-min.yml ./builds/.copier-answers-min.yml

# build min-db
rm -rf ./builds/min-db
mkdir ./builds/min-db
copier copy . ./builds/min-db --trust --force --answers-file ../.copier-answers-min-db.yml
cp ./builds/min-db/.copier-answers-min-db.yml ./builds/.copier-answers-min-db.yml
