package com.charrey;

import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.Util;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PrunerTest {


    public static void main(String[] args) throws InterruptedException {

        Runnable runnable1;
        Runnable runnable2;
        Runnable runnable3;
        Thread thread1;
        Thread thread2;
        Thread thread3;

        runnable1 = () -> {
            try {
                testSerialZeroDomainLabelDegree("serialzerodomainlabeldegree.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable2 = () -> {
            try {
                testCachedZeroDomainLabelDegree("cachedzerodomainlabeldegree.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable3 = () -> {
            try {
                testParallelZeroDomainLabelDegree("parallelzerodomainlabeldegree.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

        runnable1 = () -> {
            try {
                testSerialAlldiffLabelDegree("serialalldifflabeldegree.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable2 = () -> {
            try {
                testCachedAlldiffLabelDegree("cachedalldifflabeldegree.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable3 = () -> {
            try {
                testParallelAlldiffLabelDegree("parallelalldifflabeldegree.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

        runnable1 = () -> {
            try {
                testSerialZeroDomainUnmatchedDegrees("serialzerodomainUnmatchedDegrees.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable2 = () -> {
            try {
                testCachedZeroDomainUnmatchedDegrees("cachedzerodomainUnmatchedDegrees.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable3 = () -> {
            try {
                testParallelZeroDomainUnmatchedDegrees("parallelzerodomainUnmatchedDegrees.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

        runnable1 = () -> {
            try {
                testSerialAlldiffUnmatched("serialalldiffUnmatchedDegrees.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable2 = () -> {
            try {
                testCachedAlldiffUnmatched("cachedalldiffUnmatchedDegrees.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable3 = () -> {
            try {
                testParallelAlldiffUnmatched("parallelalldiffUnmatchedDegrees.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

        runnable1 = () -> {
            try {
                testSerialZeroDomainMReach("serialzerodomainMReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable2 = () -> {
            try {
                testCachedZeroDomainMReach("cachedzerodomainMReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable3 = () -> {
            try {
                testParallelZeroDomainMReach("parallelzerodomainMReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

        runnable1 = () -> {
            try {
                testSerialAlldiffMReach("serialalldiffMReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable2 = () -> {
            try {
                testCachedAlldiffMReach("cachedalldiffMReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable3 = () -> {
            try {
                testParallelAlldiffMReach("parallelalldiffMReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

        runnable1 = () -> {
            try {
                testSerialZeroDomainNReach("serialzerodomainNReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable2 = () -> {
            try {
                testCachedZeroDomainNReach("cachedzerodomainNReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable3 = () -> {
            try {
                testParallelZeroDomainNReach("parallelzerodomainNReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

        runnable1 = () -> {
            try {
                testSerialAlldiffNReach("serialalldiffNReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable2 = () -> {
            try {
                testCachedAlldiffNReach("cachedalldiffNReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable3 = () -> {
            try {
                testParallelAlldiffNReach("parallelalldiffNReach.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

    }


    public static void testSerialZeroDomainLabelDegree(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void testSerialZeroDomainUnmatchedDegrees(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testSerialZeroDomainMReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testSerialZeroDomainNReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testCachedZeroDomainLabelDegree(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testCachedZeroDomainUnmatchedDegrees(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testCachedZeroDomainMReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testCachedZeroDomainNReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testParallelZeroDomainLabelDegree(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));


            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));

        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testParallelZeroDomainUnmatchedDegrees(String fileName) throws InterruptedException {

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(out, System.out));
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(out, System.out));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testParallelZeroDomainMReach(String fileName) throws InterruptedException { //cr

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(out, System.out));
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(out, System.out));

    } catch (IOException e) {
        e.printStackTrace();
    }
    }


    public static void testParallelZeroDomainNReach(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {


        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(out, System.out));
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(out, System.out));

    } catch (IOException e) {
        e.printStackTrace();
    }
    }



    public static void testSerialAlldiffLabelDegree(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testSerialAlldiffUnmatched(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testSerialAlldiffMReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testSerialAlldiffNReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void testCachedAlldiffLabelDegree(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testCachedAlldiffUnmatched(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testCachedAlldiffMReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testCachedAlldiffNReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , 10*60*1000L, true, false, Util.setOf(out, System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testParallelAlldiffLabelDegree(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(System.out, out));
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(System.out, out));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void testParallelAlldiffUnmatched(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(System.out, out));
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(System.out, out));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void testParallelAlldiffMReach(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(System.out, out));
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(System.out, out));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void testParallelAlldiffNReach(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(System.out, out));
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false, Util.setOf(System.out, out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    static void comparitiveTest(List<Configuration> configurations,
                                double sourceDegree,
                                double sizeFactor,
                                double targetdegree,
                                boolean labels,
                                TestCaseProvider tcp,
                                long timeout,
                                boolean continueOnError,
                                boolean calloutEachResult,
                                Set<Appendable> outputs) throws InterruptedException {
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Object fileLock = new Object();
            Thread theThread = new Thread(() -> {
                Random threadRandom = new Random(512);
                List<Integer> x = new ArrayList<>();
                List<Double> contractionComparedToNot = new ArrayList<>();

                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    Random perXRandom = new Random(threadRandom.nextLong());

                    String toPrint = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + ", cases done = " + lastCasesDone;
                    synchronized (fileLock) {
                        outputs.forEach(y -> {
                            try {
                                y.append(toPrint + "\n");
                                ((Flushable) y).flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    long timeStartForThisX = System.currentTimeMillis();
                    double totalTimeWith = 0d;
                    double totalTimeWithout = 0d;
                    double totalPortfolio = 0d;
                    int cases = 0;
                    while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 1000) {
                        cases++;
                        long testcaseSeed = perXRandom.nextLong();
                        TestCase tc = tcp.get(currentX, (int) Math.round(currentX * sourceDegree), (int)Math.round(currentX * sizeFactor), (int) Math.round(currentX * sizeFactor * targetdegree), testcaseSeed, labels);
                        HomeomorphismResult resultWith;
                        HomeomorphismResult resultWithout = null;
                        double periodWith = -1;
                        double periodWithout = -1;
                        try {
                            long startTime = System.nanoTime();
                            resultWith = testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                            periodWith = System.nanoTime() - startTime;
                            if (configuration.getSettingsWithoutContraction() != null) {
                                startTime = System.nanoTime();
                                resultWithout = testWithoutExpectation(tc, timeout, configuration.getSettingsWithoutContraction());
                                periodWithout = System.nanoTime() - startTime;
                            }
                        } catch (Exception | Error e) {
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            e.printStackTrace();
                            if (continueOnError) {
                                continue;
                            } else {
                                throw e;
                            }
                        }
                        if (resultWithout instanceof FailResult || resultWith instanceof FailResult) {
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            if (!continueOnError) {
                                throw new IllegalStateException("Failed!");
                            }
                        } else if (resultWith instanceof SuccessResult && (configuration.getSettingsWithoutContraction() == null || resultWithout instanceof SuccessResult)) {
                            totalTimeWith += periodWith;
                            totalTimeWithout += periodWithout;
                            if (calloutEachResult) {
                                System.out.println("Success");
                            }
                        }
                    }
                    if (totalTimeWith > 0 && totalTimeWithout > 0) {
                        contractionComparedToNot.add(100* (totalTimeWith / totalTimeWithout) - 100d);
                        x.add(currentX);
                    }
                    lastCasesDone = cases;
                    currentX++;
                }
                synchronized (fileLock) {
                    outputs.forEach(y -> {
                        try {
                            y.append(configuration.getString(x, contractionComparedToNot) + "\n");
                            ((Flushable) y).flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
            threads.put(configuration, theThread);
        }
        threads.values().forEach(Thread::start);
        for (Thread thread : threads.values()) {
            thread.join();
        }
    }

    @NotNull
    public static HomeomorphismResult testWithoutExpectation(@NotNull TestCase testCase, long timeout, @NotNull Settings settings) {
        return new IsoFinder().getHomeomorphism(testCase, settings, timeout, "SYSTEMTEST");
    }

}
