Name: Pradyumna Kaushik
BNumber: B00594994
Email: pkaushi1@binghamton.edu

Programming Language used
-------------------------
Java

RPC Framework used
------------------
Apache Thrift

Instructions to compile and run the application
-----------------------------------------------

1. Extract pkaushi1-project3.tar.gz.

2. Go to directory pkaushi1-project3/

	You should be able to see the following files and directories
	a. src/ -> contains all the source files.	
	b. bin/ -> contains all the compiled .class files.
	c. Makefile -> make file to compile the src files.
	d. Readme -> contains description about the methods to compile and run the project and implementation details.
	e. branches.txt -> contains the ip addresses and corresponding port numbers on which each instance of the branch would run.
	f. branch.sh -> bash script that runs the BranchServer on a given a name and a port.
	g. controller.sh -> bash script that runs the Controller for a given initial amout and branches.txt.
	h. controller.log -> log file that the controller maintains upon every run of the program.	

3. Compile the project using the following command,
	make

4. Now run the bash script branch.sh on as many ports as required (can be different machines too) using the following command,
	./branch.sh <branch name> <port>

5. Update the branches.txt file with the branch name and port number of all the running instances.

6. Now run the bash script for the Controller using the following command,
	bash controller.sh <amount to initialize> <file containing ips and ports>


Implementation details
----------------------

1. The Controller reads the ip addresses and port numbers from the given file and divides the initial amount equally with all the branches and initializes them.
2. Upon initialization each branch waits until it is completely initialized before starting to transfer money.
3. Random money transfer
	a. Used TimerTask and Timer classes.
	b. Each time a random interval of time and a random branch are determined and then a transfer is scheduled.
4. Updating of the balance is synchronized so that only one thread would update the balance at a given instant of time.
5. When branch receives marker (if first marker) then it, 
	a. temporarily stalls money transfers.
	b. records its local state.
	c. sends markers to all the other branches.
	d. records the time at which the markers were sent.
	e. restarts money transfers. 
6. When branch receives money,
	a. Checks whether money arrived after recording local state and before the receipt of the marker from the corresponding branch.
		If yes then it records the receipt as the incoming channel state.
	b. Update balance.


Sample input/output
-------------------

Contents of branches.txt :-
	
	branch1 128.226.180.162 10001
	branch2 128.226.180.163 10002
	branch3 128.226.180.164 10003
	branch4 128.226.180.165 10004

Running the branches :-
	
	./branch.sh branch1 10001 (Output -> starting branch1 on port : 10001...)
	./branch.sh branch2 10002 (Output -> starting branch2 on port : 10002...)
	./branch.sh branch3 10003 (Output -> starting branch3 on port : 10003...)
	./branch.sh branch4 10004 (Output -> starting branch4 on port : 10004...)

Running the controller with inital amount = 4000 :-
	
	bash controller.sh 4000 branches.txt
	
Console output from Controller :-

	Controller started...
	Initializing Snapshot 0 to branch3...
	Initializing Snapshot 1 to branch1...
	Initializing Snapshot 2 to branch1...
	Wed Nov 18 15:20:48 EST 2015 : Snapshots retrieved.
	------------------SNAPSHOT 0------------------
	LocalSnapshot(snapshot_num:0, balance:166, messages:[])
	LocalSnapshot(snapshot_num:0, balance:1455, messages:[])
	LocalSnapshot(snapshot_num:0, balance:101, messages:[])
	LocalSnapshot(snapshot_num:0, balance:2308, messages:[])


	Wed Nov 18 15:20:59 EST 2015 : Snapshots retrieved.
	------------------SNAPSHOT 1------------------
	LocalSnapshot(snapshot_num:1, balance:1105, messages:[])
	LocalSnapshot(snapshot_num:1, balance:496, messages:[])
	LocalSnapshot(snapshot_num:1, balance:1321, messages:[])
	LocalSnapshot(snapshot_num:1, balance:1078, messages:[])


	Wed Nov 18 15:21:10 EST 2015 : Snapshots retrieved.
	------------------SNAPSHOT 2------------------
	LocalSnapshot(snapshot_num:2, balance:1053, messages:[])
	LocalSnapshot(snapshot_num:2, balance:1, messages:[])
	LocalSnapshot(snapshot_num:2, balance:1843, messages:[])
	LocalSnapshot(snapshot_num:2, balance:1103, messages:[])

Log file controller.log :-

	Wed Nov 18 15:20:28 EST 2015 : Intialized branch1.
	Wed Nov 18 15:20:30 EST 2015 : Intialized branch2.
	Wed Nov 18 15:20:32 EST 2015 : Intialized branch3.
	Wed Nov 18 15:20:34 EST 2015 : Intialized branch4.
	Wed Nov 18 15:20:42 EST 2015 : Initialized snapshot message to branch3.
	Wed Nov 18 15:20:48 EST 2015 : Retrieved snapshot from branch1.
	Wed Nov 18 15:20:48 EST 2015 : Retrieved snapshot from branch2.
	Wed Nov 18 15:20:48 EST 2015 : Retrieved snapshot from branch3.
	Wed Nov 18 15:20:48 EST 2015 : Retrieved snapshot from branch4.
	Wed Nov 18 15:20:53 EST 2015 : Initialized snapshot message to branch1.
	Wed Nov 18 15:20:59 EST 2015 : Retrieved snapshot from branch1.
	Wed Nov 18 15:20:59 EST 2015 : Retrieved snapshot from branch2.
	Wed Nov 18 15:20:59 EST 2015 : Retrieved snapshot from branch3.
	Wed Nov 18 15:20:59 EST 2015 : Retrieved snapshot from branch4.
	Wed Nov 18 15:21:04 EST 2015 : Initialized snapshot message to branch1.
	Wed Nov 18 15:21:10 EST 2015 : Retrieved snapshot from branch1.
	Wed Nov 18 15:21:10 EST 2015 : Retrieved snapshot from branch2.
	Wed Nov 18 15:21:10 EST 2015 : Retrieved snapshot from branch3.
	Wed Nov 18 15:21:10 EST 2015 : Retrieved snapshot from branch4.
	Wed Nov 18 15:20:48 EST 2015 : Snapshots retrieved.
	------------------SNAPSHOT 0------------------
	LocalSnapshot(snapshot_num:0, balance:166, messages:[])
	LocalSnapshot(snapshot_num:0, balance:1455, messages:[])
	LocalSnapshot(snapshot_num:0, balance:101, messages:[])
	LocalSnapshot(snapshot_num:0, balance:2308, messages:[])


	Wed Nov 18 15:20:59 EST 2015 : Snapshots retrieved.
	------------------SNAPSHOT 1------------------
	LocalSnapshot(snapshot_num:1, balance:1105, messages:[])
	LocalSnapshot(snapshot_num:1, balance:496, messages:[])
	LocalSnapshot(snapshot_num:1, balance:1321, messages:[])
	LocalSnapshot(snapshot_num:1, balance:1078, messages:[])


	Wed Nov 18 15:21:10 EST 2015 : Snapshots retrieved.
	------------------SNAPSHOT 2------------------
	LocalSnapshot(snapshot_num:2, balance:1053, messages:[])
	LocalSnapshot(snapshot_num:2, balance:1, messages:[])
	LocalSnapshot(snapshot_num:2, balance:1843, messages:[])
	LocalSnapshot(snapshot_num:2, balance:1103, messages:[])

