#!/bin/ksh

typeset -i count=0
langpacks=$(ls *.xml)

typeset help verbose
for opt; do
    case "$opt" in 
      (-h[elp]) help=true; continue;;
      (-v[erbose]) verbose=true; contnue;;
      (*) langpacks=$opt.xml; continue;;
    esac
done

if [ a$help = "atrue" ] ; then
    echo Shell scrip to verify entries in langpacks.
    echo This shell script writes the ids which are in eng.xml
    echo but not in the langpack with the given ISO3 to stdout.
    echo usage: $0 [hv] [ISO3]
    exit 0
fi
for lp in $langpacks; do
    echo "Result for langpack $lp:"
    result=
    count=0
    for i in `awk '{print $2}' eng.xml | grep 'id="'`; do
        MATCH=`grep "$i" $lp`;
        if [ "${MATCH}" == "" ]; then
            result[count]=$i
            count=count+1
        fi;
    done;
    if [ count -eq 0 ] ; then
        echo "    All ids present"
    else
        echo "    $count IDs missing!"
        if [ a$verbose = "atrue" ] ; then
            for line in ${result[*]} ; do
                echo "    $line"
            done;
        fi
    fi
    
done;
