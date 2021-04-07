### About System
    P2P message passing system
    Each Process will have two channels between each proceses (Write and Read Channel) 

### Algorithms
1. Lai-Yang Snapshot
   #### Implementation :
   Each process is a background worker, read data like event-loop.
   #### Steps :
    1. Enter no of process & initial amount.
    2. Enter Number
        1. To send White-Message
            1. Enter Source process ID
            2. Enter Destination process ID
            3. Enter Amount to send
        2. To send Red-Message/Take global snapshot
            * Enter process ID to record global snapshot
        3. To print last recorded snapshot to console
            * Enter process ID to get last snapshot
        4. Exit
### Architecture
#### Class
    Process : Holds source & destination and read & write channels.
    LaiYangProcess : Sub-class of Prcess, holds process color, all snapshots.
    
    Message -> Interface for Message
    DataMessage -> Generic Message
    LaiYangMessage -> Algorithm specific implementation where each messages are colored (RED/While)

    Channel -> Interface
    AbstractChannel -> Holds common resource requiments for FIF0 and non-FIFO channels
    FifoChannel -> Ordered message passing implementation (Guarantee order)
    NonFifoChannel -> Un-ordered message passing implementation
    
    Orchestration -> creates process and setup channels between them
    LaiYangOrchestration -> Allows to send colored message, prints global state snapshots from a process

### CHECK : Lai_Yang_Tester.java to test

#### Requirements :
1. Any Operating System
2. JDK-7 or above
