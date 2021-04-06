package com.balaji.naga.utils;

public interface Messages {
    String SPACE = " ";
    String NEWLINE = "\n";

    String ENTER_TO = "Enter to : ";
    String ENTER_FROM = "Enter from : ";
    String ENTER_AMOUNT = "Enter amount : ";
    String ENTER_PROCESS_ID = "Enter process ID : ";
    String RECORD_SNAPSHOT_AT = "Record Snapshot at : ";
    String ENTER_NO_OF_PROCESS = "Enter no of process : ";
    String ENTER_INITIAL_AMOUNT = "Enter no of process : ";
    String SNAPSHOT_DOES_NOT_EXIST = "Snapshot(s) does not exist";
    String LAI_YANG_TESTER_CHOICE = "1.Send White Message\n2.Global Snapshot\n3.Print Snapshot\n4.Exit";

    String INVALID_CHANNEL_TYPE = "Invalid Channel Type";
    String TRANSACTION_COMPLETE = "Transaction complete";
    String SNAPSHOT_IS_IN_PROGRESS = "Snapshot is already in progress";

    String INVALID_PROCESS_ID = "In-valid process id";
    String PROCESSES_ARE_IN_GLOBAL_CONSISTENT_STATE = "Processes are in global consistent";
    String PROCESSES_ARE_NOT_IN_GLOBAL_CONSISTENT_STATE = "Processes are not in global consistent";
    String INVALID_PROCESS_ID_FOR_WRITE_CHANNEL = "In-valid process id for access write channel";
}