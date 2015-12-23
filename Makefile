LIB_PATH=lib/commons-io-2.4.jar:lib/libthrift-0.9.2.jar:lib/slf4j-simple-1.7.12.jar:lib/slf4j-api-1.7.12.jar
all: clean
	mkdir bin
	javac -classpath $(LIB_PATH) -d bin src/Branch.java src/BranchClientImpl.java src/BranchID.java src/BranchServer.java src/BranchServerImpl.java src/ClientFetcher.java src/Controller.java src/LocalSnapshot.java src/NullInstanceException.java src/SystemException.java src/TransferMessage.java

clean:
	rm -rf bin *~
