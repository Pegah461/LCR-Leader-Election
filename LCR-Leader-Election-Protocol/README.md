
---

# RMI-Based Leader Election System

This is a Java program that simulates a leader election process in a distributed system using RMI (Remote Method Invocation). The program is designed to allow nodes to communicate with each other, detect leader failures, and elect a new leader.

## Table of Contents
1. [Requirements](#requirements)
2. [Installation](#installation)
3. [Usage](#usage)
4. [Running the Program](#running-the-program)
5. [How the Election Process Works](#how-the-election-process-works)
6. [Troubleshooting](#troubleshooting)

## Requirements

To run the program, you need the following:

- Java SE 8 or later installed on your machine.
- Basic knowledge of RMI and distributed systems.

## Installation

1. **Clone the repository** or download the source code.
2. Compile the Java files using the following command:

   ```bash
   javac NodeImpl.java Node.java
   ```

   This will compile the Java files and generate the necessary class files.

## Usage

To run the program, you need to start multiple nodes (at least 3) using RMI, and each node needs to connect to the next node in a circular manner. The node ID should be unique, and each node will communicate with its neighbor to conduct the election.

Each node takes four arguments when run:

1. **nodeId**: The ID of the node (a unique integer).
2. **nextNode**: The name of the next node in the ring (e.g., `Node2`).
3. **registryPort**: The port number for the RMI registry of the current node.
4. **nextPort**: The port number of the RMI registry of the next node.

### Example

For a system with three nodes, here’s how to run each node:

1. **Start Node 1:**

   ```bash
   java NodeImpl 1 Node2 1099 1100
   ```

2. **Start Node 2:**

   ```bash
   java NodeImpl 2 Node3 1100 1101
   ```

3. **Start Node 3:**

   ```bash
   java NodeImpl 3 Node1 1101 1099
   ```

In this example, each node is connected to the next node in the circular ring structure.

## Running the Program

1. **Start the nodes**: Ensure all nodes are started as described in the [Usage](#usage) section.
2. **Initiate election**: After starting all nodes, you can initiate an election from any node. Type `start` in the terminal of any node to detect a leader failure and begin the election process.
3. **Exit**: To exit the program, type `exit`.

### Example Command Flow

```bash
Type 'start' to initiate the election, or 'exit' to quit:
start
1: Detected Leader failure. Initiating election . . .
1: Recieving election message. Forwarding message.
1: New leader is 3
1: Election is complete.
Type 'start' to initiate the election, or 'exit' to quit:
```

## How the Election Process Works

1. **Node Failure Detection**: The node detects that the leader is down and initiates an election by forwarding its own ID to the next node.
2. **Election Message Propagation**: Each node forwards the election message to the next node. If a node receives an ID smaller than its own, it forwards its own ID. Otherwise, it forwards the received ID.
3. **Leader Election**: The node with the highest ID is elected as the new leader. The leader is announced by propagating a leader message to all nodes.
4. **Completion**: Once the election is complete, the process returns to the input prompt.

## Troubleshooting

1. **RMI Connection Issues**: If nodes are failing to connect to the next node, ensure that:
   - The port numbers are unique and not being used by any other processes.
   - The RMI registry is accessible on the localhost.
   - You can retry connecting by waiting for a few seconds if network issues persist.
   
2. **Node Failures**: If a node fails during the election process, it will be skipped, and the election will continue with the next available node.

3. **Election Delays**: Simulated network delays of 2 seconds are added to mimic real-world conditions. These delays can be adjusted or removed in the `recieveElection` and `recieveLeader` methods.

---

By following these steps, you can successfully run the RMI-based leader election system and test the election functionality in a distributed network.