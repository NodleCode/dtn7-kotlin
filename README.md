# dtn7-kotlin
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

dtn7-kotlin is a delay-tolerant networking software suite for kotlin, Bundle Protocol Version 7

## bputil Tool


```
Usage: bputil [-chpV] [--crc-16] [--crc-32] [-d=<destination>] [-l=<lifetime>]
              [-r=<report>] [-s=<source>]

bputil is a simple tool to encode and parse bundle

Options:
      --crc-16            use crc-16
      --crc-32            use crc-32
  -c, --check             check that input data is bundle
  -d, --destination=<destination>
                          destination eid
  -h, --help              Show this help message and exit.
  -l, --lifetime=<lifetime>
                          lifetime of the bundle
  -p, --payload-only      only dump payload (check mode only)
  -r, --report=<report>   report-to eid
  -s, --source=<source>   destination eid
  -V, --version           Print version information and exit.

```

Example:

```echo "hello world" | ./bputil -s "dtn://source/" -d "dtn://destination" --crc-32 | ./bputil --show```

## Using the library

1. Add the JitPack repository.
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

2. Add the substrate-client dependency
```
dependencies {
        implementation 'com.github.NodleCode:dtn7-kotlin:1.0'
}
```

## Additional Notes

This is a work in progress and comes with no warranty.
contribution are welcome. If you have any question, ideas or if you found a bug, please open an issue!
