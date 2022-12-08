#!/bin/bash

# collect all jar and binaries to deploy
rm -rf ./pkg
mkdir pkg
mkdir pkg/bin
mkdir pkg/lib

tar xvf tools/bputil/build/distributions/bputil.tar -C pkg/
mv pkg/bputil/bin/* pkg/bin
mv pkg/bputil/lib/* pkg/lib
rm -rf pkg/bputil/

# tar
tar cvzf ./pkg.tar.gz ./pkg/
#rm -rf linux-dtn/

