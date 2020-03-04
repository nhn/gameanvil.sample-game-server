ECHO protobuf version controlling.
protoc --version
protoc  ./Authentication.proto --java_out=../java
protoc --descriptor_set_out=Authentication.desc Authentication.proto
protoc  ./Result.proto --java_out=../java
protoc --descriptor_set_out=Result.desc Result.proto
protoc  ./GameMulti.proto --java_out=../java
protoc --descriptor_set_out=GameMulti.desc GameMulti.proto
protoc  ./GameSingle.proto --java_out=../java
protoc --descriptor_set_out=GameSingle.desc GameSingle.proto
protoc  ./User.proto --java_out=../java
protoc --descriptor_set_out=User.desc User.proto

pause
