ECHO protobuf version controlling.
protoc --version
protoc  ./Authentication.proto --java_out=../java
protoc  ./Result.proto --java_out=../java
protoc  ./GameMulti.proto --java_out=../java
protoc  ./GameSingle.proto --java_out=../java
protoc  ./User.proto --java_out=../java

pause
