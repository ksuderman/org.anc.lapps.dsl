#!/bin/bash
set -e
cd target
OLD=`ls lsd*.jar`
NEW=`echo $OLD | sed 's/-jar-with-dependencies//'`
mv $OLD $NEW

