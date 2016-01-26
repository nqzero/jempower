#!/bin/bash

function field() {
    grep -ho $1 $ii | sed "s/.*://"
}

function median() {
    declare -i num
    declare -a a
    a=($(field $1 | sort -n))
    num=${#a[*]}
    index=$((num/2))
    echo ${a[$index]}
}


ii=20000.9098

req='^Requests[^[]*'
field "$req" | sort -n
median "$req"

# a="$(grep -h Request $ii | sed 's/.*://')";
# num=$(echo "$a" | wc -l); echo "$a" | tail -n +$num | head -n 1;


for ii in *.*; do
    jj="${ii/*./}.${ii/.*/} ";
    nf=$(field 'Failed.*' | sort -n | tail -n 1)
    nf2=$(median 'Failed.*')
    rate2=$(median "$req")
    rate=$(field "$req")
    printf "%-10s: %8d %8d   %8s -- " $jj $nf $nf2 $rate2;
    # echo $rate | cut -d\  -f -5 | sort -n;
    list=$(echo "$rate" | head -n 5 | sort -n);
    echo $list
done | sort -V


echo --------------------------------------------

for ii in *.*; do
    jj="${ii/*./}.${ii/.*/} ";
    nf=$(median 'Failed.*')
    nf2=$(field 'Failed.*' | sort -n | tail -n 1)
    rate=$(median "$req")
    printf "%-10s: %8d %8d %s" $jj $nf $nf2 $rate;
    echo
done | sort -V


exit

# the loop for running the stress tests
for ii in 9090 9092 9093 9095 9096 9098; do
    for jj in 1000 2000 3000 4000 10000 20000; do 
        echo pass ------------- $ii $jj $(curl -s localhost:$ii/hello);
        (cputime.sh ulim.sh ab -r -k -c $jj -n 1000000 localhost:$ii/hello) >> $jj.$ii;
    done;
    cputime.sh sleep 60 >> ${ii}sleep;
done



for ii in 9096; do for jj in 1000 2000 3000 4000 10000 20000; do echo pass ------------- $ii $jj $(curl -s localhost:$ii/hello); (cputime.sh ulim.sh ab -r -k -c $jj -n 1000000 localhost:$ii/hello) >> $jj.$ii; done; cputime.sh sleep 60 >> ${ii}sleep; done
