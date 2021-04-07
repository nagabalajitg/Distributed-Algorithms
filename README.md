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
    
    Message

###CHECK : Lai_Yang_Tester.java to test