# LCR Leader Election (Java RMI)

A simple Java implementation of the **Le Lann–Chang–Roberts (LCR) Leader Election algorithm** using **Java RMI**. This project demonstrates distributed systems concepts, where processes (nodes) arranged in a ring topology elect a single leader.

---

## Table of Contents

* [Overview](#overview)
* [Features](#features)
* [Project Structure](#project-structure)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Compile](#compile)
  * [Run](#run)
* [How It Works](#How-It-Works)
* [Core Classes](#core-classes)

---

## Overview

This project implements the **LCR Leader Election Algorithm** in a simulated distributed system using **Java Remote Method Invocation (RMI)**. Each node is represented as a process with a unique ID, connected in a ring topology. Nodes can initiate elections, forward election messages, and eventually learn the identity of the elected leader.

## Features

* ✅ Ring topology
* ✅ Election and leader announcement messages (`ELECTION(uid)` and `LEADER(uid)`)
* ✅ RMI‑based communication between processes
* ✅ Configurable start times for triggering elections

## Project Structure

```
LCR-Leader-Election-Protocol/
├─ Api.java          # RMI interface for election and leader messages
├─ ApiImpl.java      # Implementation of Api; handles election logic
├─ App.java          # Contains main class and the login for node binding
├─ Node.java         # Simple data model for Node with unique ID
```

## Getting Started

### Prerequisites

* Java 17+ (or compatible version)

### Compile
Compile the source files using the following command:
```bash
javac *.java
```
### Run
Each process is launched as a separate JVM instance:
When running the nodes, ensure that the nodes are connected in a circular manner
```bash
java App <process_id> <nextProcess> <registryPort> <nextPort> <startAt(HH:mm:ss)>
```
#### Example:
```bash
java App 5 process11 1099 1100 12:00:00  
java App 11 process2 1100 1101 12:00:00
java App 2 process7 1101 1102 12:00:00
java App 7 process5 1102 1099 12:00:00
```

## How It Works

* Each process starts with a unique ID.
* At the scheduled `startAt` time, the election begins.
* The elected leader is displayed in the console logs.

Sample console output:
```
=============================================
 _   _    ___   ____   _____
| \ | |  /   \ |  _ \ | ____|
|  \| | | | | || | | ||  _|
| |\  | | |_| || |_| || |___
|_| \_|  \___/ |____/ |_____|
      P R O C E S S [2]
---------------------------------------------
PORT : 1100
CONNECTED TO: process5 AT PORT: 1101
=============================================
```

## Core Classes

| Class       | Responsibility                                                                            |
| ----------- | ----------------------------------------------------------------------------------------- |
| **Api**     | RMI interface defining `receiveELECTION(int uid)` and `receiveLEADER(int leaderID)`       |
| **ApiImpl** | Implements election logic: forwards messages, tracks leader, prevents duplicate elections |
| **Node**    | Encapsulates node identity (UID) with getter/setter                                       |
| **App**     | Main runner; starts processes, connects RMI registries, prints console UI                 |
