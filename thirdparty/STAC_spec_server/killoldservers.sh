ps -eaf | grep STAC | grep python | awk '{ print "kill -9 ",$2 }' > ./killact.sh
chmod 777 ./killact.sh
./killact.sh
rm -f ./killact.sh

