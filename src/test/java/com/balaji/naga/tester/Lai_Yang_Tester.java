package com.balaji.naga.tester;

import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm;
import com.balaji.naga.message.LaiYangMessage;
import com.balaji.naga.message.Message;
import com.balaji.naga.resource.LaiYangOrchestration;
import com.balaji.naga.resource.Orchestration.ProcessInitMeta;
import com.balaji.naga.resource.Orchestration.ProcessInitMeta.ProcessInitMetaType;
import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.resource.communication.Channel.ChannelType;
import com.balaji.naga.snapshot.LaiYangSnapshot;
import com.balaji.naga.utils.Messages;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Lai_Yang_Tester {
    private static final Logger LOGGER = Logger.getLogger(Lai_Yang_Tester.class.getName());

    public static void main (String[] arg) {
        test();
    }

    public static void test() {
        System.out.println("Start !");
        LaiYangOrchestration orchestration = null;
        try {
            orchestration = new LaiYangOrchestration();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(Messages.ENTER_NO_OF_PROCESS);
            int noOfProcesses = Integer.parseInt(reader.readLine().trim());

            System.out.println(Messages.ENTER_CHANNEL_TYPE);
            ChannelType channel = ChannelType.getChannelByInt(Integer.parseInt(reader.readLine().trim()));

            System.out.println(Messages.ENTER_FIXED_VARIABLE);
            ProcessInitMetaType processInitMetaType = ProcessInitMetaType.getTypeFromInt( Integer.parseInt(reader.readLine().trim()));

            if (processInitMetaType == ProcessInitMetaType.FIXED) {
                System.out.println(Messages.ENTER_INITIAL_AMOUNT);
                long initialValue = Long.parseLong(reader.readLine().trim());
                orchestration.createProcess(noOfProcesses, initialValue, channel);
            } else {
                int process = 1;
                ProcessInitMeta meta = new ProcessInitMeta(noOfProcesses);
                do {
                    System.out.print(Messages.ENTER_INITIAL_AMOUNT_FOR_PROCESS + process + " : ");
                    meta.addInitialValue(Long.parseLong(reader.readLine().trim()));
                } while (process ++ < noOfProcesses);

                orchestration.createProcess(meta, channel);
            }
            boolean flag = false;

            while (true) {
                try {
                    System.out.println(Messages.LAI_YANG_TESTER_CHOICE);
                    String command = reader.readLine().trim();
                    switch (Integer.parseInt(command)) {
                        case 1 :
                            System.out.print(Messages.ENTER_FROM);
                            long from = Long.parseLong(reader.readLine().trim());

                            System.out.print(Messages.ENTER_TO);
                            long to = Long.parseLong(reader.readLine().trim());

                            System.out.print(Messages.ENTER_AMOUNT);
                            long amount = Long.parseLong(reader.readLine().trim());

                            Message message = new LaiYangMessage(LaiYangSnapshotAlgorithm.MessageColor.WHITE, amount, from, to);
                            orchestration.sendMessage(message);

                            System.out.println(Messages.TRANSACTION_COMPLETE);
                            break;
                        case 2 :
                            System.out.print(Messages.RECORD_SNAPSHOT_AT);
                            long at = Long.parseLong(reader.readLine().trim());

                            orchestration.recordGlobalStateFrom(at);
                            break;
                        case 3 :
                            System.out.print(Messages.ENTER_PROCESS_ID);
                            long processID = Long.parseLong(reader.readLine().trim());
                            LaiYangSnapshot snapshot = (LaiYangSnapshot) orchestration.getLastGlobalSnapshotFromProcess(processID);

                            if (snapshot != null) {
                                System.out.println(snapshot+"");
                            } else {
                                System.out.println(Messages.SNAPSHOT_DOES_NOT_EXIST);
                            }
                            break;
                        case 4 :
                            flag = true;
                            break;
                        default :
                            continue;
                    }
                    System.out.println("");
                    if (flag) {
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    System.out.println(Messages.ENTER_VALID_NUMBER);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
//            TODO: Need a way to shutdown all threads
//            if(orchestration != null) {
//                orchestration.shutdownAllProcess();
//            }
        }
        System.out.println("Ends !!");
    }
}
