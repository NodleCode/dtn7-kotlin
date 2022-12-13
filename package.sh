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

tar xvf tools/example-dtnchat/build/distributions/example-dtnchat.tar -C pkg/
mv pkg/example-dtnchat/bin/example-dtnchat pkg/bin/dtnchat
mv pkg/example-dtnchat/bin/example-dtnchat.bat pkg/bin/dtnchat.bat
mv pkg/example-dtnchat/lib/* pkg/lib
rm -rf pkg/example-dtnchat/

# tar
tar cvzf ./pkg.tar.gz ./pkg/

