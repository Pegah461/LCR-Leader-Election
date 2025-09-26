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
└─ README.md
```

### Prerequisites
* Java 17+ (or compatible)

### How It Works


### Setup Instructions
### 1: Compile The Code
1. Open terminal and navigate to the directory that contains the source files.
2. Compile the source files using the 'javac' command:
   '''bash
   javac *.java
   '''

```bash
java PeerRegisterImpl
# or, explicitly:
java PeerRegisterImpl 1099 Node0
```

2. **Start each Node** (in separate terminals). Usage from code:

```text
Usage: java App <nodeId> <startAt(HH:mm:ss)>
```

Example (staggered starts):

```bash
java App 1 12:00:00
java App 6 12:00:02
java App 11 12:00:04
```

The app binds the node as `Node<id>`, looks up the register at `Node0`, registers, and schedules an election start. When the first node reaches its start time it calls `announceElectionStart()` to block further joins and then begins the election.

## Configuration

| Component | Setting      | Default    | Notes                                                                    |
| --------- | ------------ | ---------- | ------------------------------------------------------------------------ |
| Register  | RMI port     | 1099       | `PeerRegisterImpl` main will create the registry if missing              |
| Register  | Binding name | `Node0`    | Nodes look this up in `App`                                              |
| Node      | Start time   | required   | `HH:mm:ss` local time; if time already passed, it schedules for tomorrow |
| Node      | Binding      | `Node<id>` | Ex: `Node6`                                                              |

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
Process[6]: Registered with PeerRegister.
Process[6]: Scheduled to start election at: 12:00:02 ...
```

## Notes & Caveats

* Method names use the spelling **`recieveELECTION` / `recieveLEADER`** to match the source.
* The register depends on a consistent binding pattern (`Node<id>`) to wire the ring.
* On non‑Windows systems, replace the `cls` clear‑screen command with your platform equivalent.

## License

MIT (see LICENSE)
