# LCR Leader Election Protocol — Peer Register

## Overview
A Java RMI implementation of the **Le Lann–Chang–Roberts (LCR)** leader election algorithm with a **Peer Register Node** that coordinates ring formation and blocks any other Nodes from joining during an election.

## Features
- **Peer Registration**: A Peer Register Node is responsible to register peers and ensuring which peer is connected to which one in a ring topology.
- **Leader Election**: All nodes in the ring are aware of their next node so will either forward or drop an election message based on their UID.
- **Time To Start**: All nodes will have to start the election at the same time to ensure concurrency.
- **Remote Communication: Messages sent between nodes are done remotely using RMI.

## Project Structure
```
.
├─ App.java                 # Node launcher and scheduler
├─ Node.java                # RMI interface for node-to-node RPC
├─ NodeImpl.java            # Node logic (recieveELECTION / recieveLEADER)
├─ PeerRegister.java        # RMI interface for the register
├─ PeerRegisterImpl.java    # Register logic (join order, rewiring, gating)
```

## Prerequisites
* Java 17+ (or compatible)

## How It Works

## Setup Instructions
### 1: Compile The Code
1. Open terminal and navigate to the directory that contains the source files.
2. Compile the source files using the `javac` command:
   ```bash
   javac *.java
   ```
### 2: Start Peer Register
Start the Peer Register Node to manage node registration. Run the following command:

### 3: Run All Nodes
Use the following command to start the Nodes
```bash
java App <UID>
```
```bash
java App 5 12:00:00
java App 6 12:00:00
java App 11 12:00:00
```
Peer Register Node will handle the ring topology.

## Sample Output
```
=============================================
 _   _    ___   ____   _____
| \ | |  /   \ |  _ \ | ____|
|  \| | | | | || | | ||  _|
| |\  | | |_| || |_| || |___
|_| \_|  \___/ |____/ |_____|
     P R O C E S S [6]
---------------------------------------------
PORT : 1099
SCHEDULED START : 2025-09-24T12:00:02
=============================================
```
## Election Trigger
This program assumes that the Nodes detected Leader Failure at the same time so they all initate Leader election at exactly the same time.

## Key Classes & Methods
- **Node & NodeImpl**: Implements election logic: initiate election & message forwarding.
   -receiveELECTION(int uid): Handles election message forwarding.
   -receiveLEADER(int uid): Handles leader announcement
- **PeerRegisterImpl**: Implimentation of PeerRegistration logic.
   -register(int id): Handles Node Registration.
   -getSuccessor(int id): Get ID of next node.
   -isElectionInProgress(): Checks to see if election is in progress. Used to block node registration during an election.
