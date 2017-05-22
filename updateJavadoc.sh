#!/usr/bin/env bash

# -I means ignore lines matching a regular expression
# -q means "quiet" - only tell whether files differ or not
# -r means "recursive" - explore subdirectories
# -N means "treat absent files as empty" which makes absent files show up in Quiet mode.
diff -N -I '<!-- Generated by javadoc ' -qr docs/apidocs/ target/apidocs/ > target/javadocPatch.txt

while read  ignore1 oldfile ignore2 newfile ignore3
do
  if [ ! -f "$oldfile" ]
  then
    echo "Added $oldfile"
    echo -n >$oldfile
    cp -fu $newfile $oldfile
  elif [ ! -f "$newfile" ]
  then
    echo "Deleted $newfile"
    rm $newfile
  else
    echo "cp -fu $oldfile $newfile"
    cp -fu $newfile $oldfile
  fi
done < "target/javadocPatch.txt"