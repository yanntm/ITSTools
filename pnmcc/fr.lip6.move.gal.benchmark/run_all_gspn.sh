#! /bin/sh

set -e


./install_gspn.sh
./install_inputs.sh

# echo "##teamcity[testSuiteStarted name='PNMCC perfs']"

for i in oracle/*SS.out ; do
    ./run_test.pl $i -gspn;
done;

for i in oracle/*RFS.out ; do
    ./run_test.pl $i -gspn;
done;

for i in oracle/*RF.out ; do
    ./run_test.pl $i -gspn;
done;

for i in ctl/*.out ; do
    ./run_test.pl $i -gspn;
done;



# echo "##teamcity[testSuiteFinished name='PNMCC perfs']"

