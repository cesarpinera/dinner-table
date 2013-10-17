#/bin/sh
echo "Computing hw1-soln1.txt (60 seconds)"
lein run resources/hw1-inst1.txt solution/hw1-soln1.txt 6
echo "Computing hw1-soln2.txt (60 seconds)"
lein run resources/hw1-inst2.txt solution/hw1-soln2.txt 6
echo "Computing hw1-soln3.txt (60 seconds)"
lein run resources/hw1-inst3.txt solution/hw1-soln3.txt 6
