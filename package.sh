#!/bin/bash

# collect all jar and binaries to deploy
rm -rf ./linux
mkdir linux
mkdir linux/bin
mkdir linux/lib

tar xvf tools/bputil/build/distributions/bputil.tar -C linux/
mv linux/bputil/bin/* linux/bin
mv linux/bputil/lib/* linux/lib
rm -rf linux/bputil/

# tar
tar cvzf ./linux.tar.gz ./linux/
#rm -rf linux-dtn/

