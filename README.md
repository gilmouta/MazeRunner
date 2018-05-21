# MazeRunner
To first install the project, please run the install.sh script.
The project currently has 4 module:

## WebServer
The Web Server is responsible for receiving requests and creating threads running the MazeRunner application. It can be run using the executeweb.sh script after the initial instalation.

The server is listening on port 8000 by default and accepts queries structured as follows:

>http://\<address\>:8000/mzrun.html?m=\<maze-filename\>&x0=\<x_start\>&y0=\<y_start\>&x1=\<x_final\>&y1=\<y_final\>&v=\<velocity\>&s=\<strategy\>`

## BIT
The BIT module is responsible for instrumenting the MazeRunner code and generating the metrics, and then send them to the Metric Storage System.

The instrumented classes are the classes responsible for the three MazeRunner Strategies (A*, DFS, BFS) and the planned metrics are Method Call Depth and Iteration Count.

## Metric System

The Metric System is responsible for sending metrics to the DynamoDB, as well as estimating execution costs.

## Load Balancer

The Load Balancer system is responsible for distributing the several requests across the running instances, based on estimations by the Metric System. 
It also contains the AutoScaler, which is responsible for running new instances when needed, and removing them when they aren't. 