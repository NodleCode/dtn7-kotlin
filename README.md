# dtn7-kotlin
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

dtn7-kotlin is a delay-tolerant networking software suite for kotlin, Bundle Protocol Version 7.
It is well unit-tested and compatible with the [dtn7-go](https://github.com/dtn7/dtn7-go/) bpv7 suite.
It currently supports the following protocols:

* [bpv7](https://datatracker.ietf.org/doc/html/rfc9171) rfc9171
* [bpsec](https://datatracker.ietf.org/doc/html/rfc9172) rfc9172


## bputil Tool

```
$ bputil --help
Usage: bputil [-hV] [COMMAND]

bputil is a simple tool to encode and parse bundle

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  status
  create
  show
  key
```

Example:

```
$ echo "hello world" | ./bputil create -s "dtn://source/" -d "dtn://destination/" --crc-32 | ./bputil show

Bundle(primaryBlock=PrimaryBlock(version=7, procV7Flags=0, crcType=CRC32, destination=dtn://destination/, source=dtn://source/, reportTo=dtn://report/, creationTimestamp=1613738857441, sequenceNumber=0, lifetime=0, fragmentOffset=0, appDataLength=0), canonicalBlocks=[CanonicalBlock(blockType=1, blockNumber=1, procV7flags=0, crcType=CRC32, data=BlobBlockData(buffer=0x68656c6c6f20776f726c640a))])
```

### creating bundle key

This implementation support the [bpsec](https://tools.ietf.org/html/draft-ietf-dtn-bpsec-26) (current version 26). Since there are no specific standard defined yet for actual security context, we currently support only a simple signature block based on Ed25519. bputil allows one to create signing key like this:

```
$ bputil key create
ed25519 priv=0xc9f07a22dc1f565eb0f1e984e95d773cbf1b46ee8177ba1e51c025918d2225a7  pub=0xd9ae678a046a2fd20f9bd29344e603be4119775264f5d4f82c617e6b76c00dd0
```

the private key can be used to sign a bundle with bputil like so:

```
echo "hello world" | bputil create --sign 0 --key 0xdb87c297ea39ee8e32f00cf75eb49ad5d959916bb806ac96d86352cfe42ddd5e  | bputil show

Bundle(primaryBlock=PrimaryBlock(version=7, procV7Flags=0, crcType=CRC32, destination=dtn://destination/, source=dtn://source/, reportTo=dtn://report/, creationTimestamp=1613739504192, sequenceNumber=0, lifetime=0, fragmentOffset=0, appDataLength=0), canonicalBlocks=[CanonicalBlock(blockType=40, blockNumber=2, procV7flags=0, crcType=NoCRC, data=AbstractSecurityBlockData(securityTargets=[0], securityContext=1, securityBlockV7Flags=1, securitySource=dtn:none, securityContextParameters=[SecurityContextParameter(id=0, result=0x8662ffc4bda05bcf5537ea0db1d6898d1765b64eb5a9f2935b3073018d94dbeb)], securityResults=[[SecurityResult(id=0, result=0x5d5241073f6435d5acaac49d71998e4864862244c994418a118e2a4ac5314c929a92a5d58ec598ec20f4d897b4ee2b1bfc8e4a0e7739caf4a5551ddc360b5b0a)]])), CanonicalBlock(blockType=1, blockNumber=1, procV7flags=0, crcType=CRC32, data=BlobBlockData(buffer=0x68656c6c6f20776f726c640a))])
```

### creating status report

bputil can also be used to create administration record, only the generation of status report is currently supported. A full bundle can then be created by simply chaining commands.
For instance, the following command generate a status report, wrap it in a signed bundle and print it in stdout:

```
$ bputil status -d 1613607271 -f 1613607275 -s "dtn://source/" | bputil create -f 2 --sign 0 --key 0xdb87c297ea39ee8e32f00cf75eb49ad5d959916bb806ac96d86352cfe42ddd5e --age 150 -l 200 | bputil show

Bundle(primaryBlock=PrimaryBlock(version=7, procV7Flags=2, crcType=CRC32, destination=dtn://destination/, source=dtn://source/, reportTo=dtn://report/, creationTimestamp=0, sequenceNumber=0, lifetime=200, fragmentOffset=0, appDataLength=0), canonicalBlocks=[CanonicalBlock(blockType=40, blockNumber=3, procV7flags=0, crcType=NoCRC, data=AbstractSecurityBlockData(securityTargets=[0], securityContext=1, securityBlockV7Flags=1, securitySource=dtn:none, securityContextParameters=[SecurityContextParameter(id=0, result=0x8662ffc4bda05bcf5537ea0db1d6898d1765b64eb5a9f2935b3073018d94dbeb)], securityResults=[[SecurityResult(id=0, result=0xc0b88afa05cbac8da18a01d64781b13b46f965120aecfe6c09ff27dea9f1f9b287de4de780c30e19a5a1700afafcdafe2583113b5c1908c1f0465ce1c8080708)]])), CanonicalBlock(blockType=7, blockNumber=2, procV7flags=0, crcType=NoCRC, data=BundleAgeBlockData(age=150)), CanonicalBlock(blockType=1, blockNumber=1, procV7flags=0, crcType=CRC32, data=BlobBlockData(buffer=0x8201868481f482f51a602db16b82f51a602db16781f4008201702f2f7374617475732d736f757263652f000000))])
```

### compatibility with dtn7-go

We can combine bputil from this project and dtn-tool from dtn7-go together. For instance we use bputil to produce a signed bundle (with bpsec) and submit it to dtn7-go.

```
$ echo "hello world" | ./bputil create --sign 0 --key 0xdb87c297ea39ee8e32f00cf75eb49ad5d959916bb806ac96d86352cfe42ddd5e --age 150 -l 200 | dtn-tool show -

{"primaryBlock":{"bundleControlFlags":null,"destination":"dtn://destination/","source":"dtn://source/","reportTo":"dtn://report/","creationTimestamp":{"date":"2000-01-01 00:00:00.000","sequenceNo":0},"lifetime":200},"canonicalBlocks":[{"blockNumber":3,"blockTypeCode":40,"blockType":"N/A","blockControlFlags":null,"data":"WHOGgQABAYIBAIGCAFgghmL/xL2gW89VN+oNsdaJjRdltk61qfKTWzBzAY2U2+uBgYIAWEBl7PG9eWtcuV8reSLUJmzoiJm2udFsH7qBPnSWChyxVlb4SgfMS3QrGrQNCTK4r1UUxvhfnfD34L7gR1UD/I4O"},{"blockNumber":2,"blockTypeCode":7,"blockType":"Bundle Age Block","blockControlFlags":null,"data":"150 ms"},{"blockNumber":1,"blockTypeCode":1,"blockType":"Payload Block","blockControlFlags":null,"data":"aGVsbG8gd29ybGQK"}]}
```

## Using the library

### 1. Add the JitPack repository.
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

### 2. Add the dtn7-kotlin

the bpv7 library include the data, parsers and serializers. agent contains a simple yet easily extensible implementation of the bundle protocol.
cla is an empty shell at the moment but aa contain a simple HTTP convergence layer to upload bundles using the request body and receive bundles from the response body.

```
dependencies {
    implementation "com.github.NodleCode.dtn7-kotlin:bpv7:master"
    implementation "com.github.NodleCode.dtn7-kotlin:agent:master"
    implementation "com.github.NodleCode.dtn7-kotlin:aa:master"
    implementation "com.github.NodleCode.dtn7-kotlin:cla:master"
}

if you only need the parser, just use the bpv7:master.
```

## Additional Notes

This is a work in progress and comes with no warranty.
contribution are welcome. If you have any question, ideas or if you found a bug, please open an issue!
