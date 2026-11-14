# CSA09 - Programming in Java - Assignment 3 (CO3)

**Course**: Programming in Java (CSA09)  
**Module**: CO3 – Exception Handling, Robust Resource Management & Multithreading  
**Degree**: B.Tech Computer Science & Engineering (Semester V)  
**Mode**: Pair Work (Student A & Student B)  

---

## 1. Repository Overview

This repository contains the complete, technically verified, and executed source code, datasets, benchmarks, and academic report for **Assignment 3 (CO3)**. Every task has been designed, implemented, tested, and analyzed in accordance with the Bloom's Taxonomy Level K4 (Analyse) and K5 (Evaluate) guidelines specified in the course syllabus.

### Directory Structure

```
CSA09-<RegisterNumber>-Assignment3/
├── README.md                           # Repository documentation, build & run instructions
├── Assignment 3.pdf                     # Original assignment specification
├── card_reader.cfg                     # Hardware configuration file for ATM simulation (Q1)
├── Report/
│   └── Assignment3_Report.md           # Comprehensive Academic Submission Report (Q1-Q7)
├── Screenshots/                        # Verified execution terminal logs & output evidence
├── Q1/                                 # Q1: Bank ATM Failure Simulation
│   ├── ATMWithdrawal.java              # Primary simulation (try-catch-finally, multi-catch)
│   ├── ATMAlternative.java             # Error-code & Scanner.hasNextInt() alternative
│   └── UncaughtExceptionDemo.java      # Uncaught stack trace analysis demo
├── Q2/                                 # Q2: Online Examination Portal Exceptions
│   ├── StorageDemo.java                # Completed TODO 1 code matching PDF specifications
│   ├── ExamPortalSystem.java           # 3-tier architecture (Controller-Service-Storage)
│   └── ExamPortalUnchecked.java        # Unchecked RuntimeException alternative
├── Q3/                                 # Q3: Resource Safety & Sales Report Generator
│   ├── store1.csv                      # Test store dataset 1 (Valid)
│   ├── store2.csv                      # Test store dataset 2 (Valid)
│   ├── store3_corrupt.csv              # Injected corrupted dataset (Triggers leak test)
│   ├── store4.csv                      # Test store dataset 4 (Valid)
│   ├── SalesReportManual.java          # Manual close() version (Demonstrates resource leak)
│   ├── SalesReportAutoClose.java       # Try-with-resources version (Leak-free)
│   └── SuppressedExceptionDemo.java    # Suppressed exception demonstration
├── Q4/                                 # Q4: Parallel Answer-Script Scanning & Thread Lifecycle
│   ├── ScannerThread.java              # Thread subclass implementation
│   ├── ScannerRunnable.java            # Runnable implementation
│   ├── ScannerStationSimulation.java   # Lifecycle, start vs run, scheduling & daemon experiments
│   └── ScannerExecutorService.java     # FixedThreadPool(4) alternative
├── Q5/                                 # Q5: Cinema Seat Booking Race Conditions
│   ├── SeatBooking.java                # Completed TODO 1 code matching PDF specifications
│   ├── SeatBookingUnsyncTest.java      # Unsynchronized race condition reproducer (10 runs)
│   └── SeatBookingBenchmark.java       # 10,000 bookings benchmark across 5 concurrency models
├── Q6/                                 # Q6: Hostel Wallet Deadlock & Mess Token Counter
│   ├── TokenCounter.java               # Completed TODO 1 & 2 code matching PDF specifications
│   ├── TokenCounterIfBug.java          # While-to-if condition bug analysis demo
│   ├── HostelWalletDeadlock.java       # Reliable 4-condition deadlock demonstration
│   ├── HostelWalletDeadlockFree.java   # Deadlock-free transfer using global lock ordering
│   └── TokenCounterAlternative.java    # ArrayBlockingQueue & tryLock() alternatives
└── Q7/                                 # Q7: Fault-Tolerant Semester Result Processor
    ├── scores/                         # 10,000 student score CSV files (Valid, Missing, Corrupt)
    ├── ResultProcessor.java            # Multi-threaded Callable/Future/ConcurrentHashMap engine
    └── ResultProcessorAlternative.java # parallelStream() and thread-per-file comparisons
```

---

## 2. Environment & Prerequisites

- **Operating System**: Windows 10/11, Linux, or macOS
- **Java Development Kit (JDK)**: Java SE 21 LTS (Oracle JDK or Microsoft Build of OpenJDK 21.0.12+)
- **Build Tool / Shell**: PowerShell 5.1 / PowerShell Core / Bash

Verify installed JDK version:
```bash
javac -version
java -version
```

---

## 3. Compilation & Execution Guide

All commands are executed from the repository root directory.

### Question 1: Bank ATM Failure Simulation
```bash
# Compile
javac Q1/*.java

# Run Primary Simulation
java Q1.ATMWithdrawal

# Run Alternative Error-Code Implementation
java Q1.ATMAlternative

# Run Uncaught Exception Trace Demo
java Q1.UncaughtExceptionDemo
```

### Question 2: Online Examination Portal Exceptions
```bash
# Compile
javac Q2/*.java

# Run Completed TODO 1 (StorageDemo)
java Q2.StorageDemo

# Run 3-Tier Enterprise Exam Portal System
java Q2.ExamPortalSystem

# Run Unchecked Alternative
java Q2.ExamPortalUnchecked
```

### Question 3: Resource Safety & Daily Sales Report Generator
```bash
# Compile
javac Q3/*.java

# Run Manual Version (Shows Resource Leakage on Failure)
java Q3.SalesReportManual

# Run Try-With-Resources Version (Safe & Resilient)
java Q3.SalesReportAutoClose

# Run Suppressed Exceptions Demonstration
java Q3.SuppressedExceptionDemo
```

### Question 4: Thread Creation & Life Cycle
```bash
# Compile
javac Q4/*.java

# Run Full Lifecycle, start() vs run(), 10-Run Scheduling & Priority Experiments
java Q4.ScannerStationSimulation

# Run ExecutorService Alternative
java Q4.ScannerExecutorService
```

### Question 5: Cinema Seat Booking Race Conditions
```bash
# Compile
javac Q5/*.java

# Run Completed TODO 1 (SeatBooking)
java Q5.SeatBooking

# Run 10-Trial Unsynchronized Race Condition Test
java Q5.SeatBookingUnsyncTest

# Run 10,000 Bookings Benchmark Across 5 Synchronization Models
java Q5.SeatBookingBenchmark
```

### Question 6: Hostel Wallet Deadlock & Token Counter
```bash
# Compile
javac Q6/*.java

# Run Completed TODO 1 & 2 (TokenCounter Producer-Consumer)
java Q6.TokenCounter

# Run While-to-If Condition Bug Demonstration
java Q6.TokenCounterIfBug

# Run Deadlock Demonstration
java Q6.HostelWalletDeadlock

# Run Deadlock-Free Lock-Ordered Wallet Transfer
java Q6.HostelWalletDeadlockFree

# Run ArrayBlockingQueue & tryLock() Alternative
java Q6.TokenCounterAlternative
```

### Question 7: Fault-Tolerant Semester Result Processor
```bash
# Compile
javac Q7/*.java

# Run Full 10,000 File Concurrent Processor & Multi-Core Benchmark (1, 2, 4, 8 threads)
java Q7.ResultProcessor

# Run parallelStream() and Thread-per-file Alternative Comparisons
java Q7.ResultProcessorAlternative
```

---

## 4. Progressive Git Commit Workflow

As mandated by course policy, progressive commits must be maintained throughout the assignment duration.

```bash
# Initialize local git repository (if starting fresh)
git init
git branch -M main

# Commit Task 1
git add Q1/ card_reader.cfg
git commit -m "feat(Q1): Implement ATM withdrawal simulation with checked/unchecked multi-catch and error-code alternative"

# Commit Task 2
git add Q2/
git commit -m "feat(Q2): Complete TODO 1 in StorageDemo and build 3-tier exam portal with exception chaining"

# Commit Task 3
git add Q3/
git commit -m "feat(Q3): Implement manual vs try-with-resources sales report generator with suppressed exception demo"

# Commit Task 4
git add Q4/
git commit -m "feat(Q4): Implement scanner station threads, lifecycle tracing, start vs run, and priority experiments"

# Commit Task 5
git add Q5/
git commit -m "feat(Q5): Complete TODO 1 in SeatBooking, reproduce double-booking race condition, and benchmark 10k bookings"

# Commit Task 6
git add Q6/
git commit -m "feat(Q6): Complete TODO 1 & 2 in TokenCounter, reproduce while-to-if bug, and resolve wallet deadlock via lock ordering"

# Commit Task 7
git add Q7/
git commit -m "feat(Q7): Implement fault-tolerant concurrent semester result processor for 10,000 files with multi-core benchmarks"

# Commit Documentation & Final Report
git add Report/ README.md
git commit -m "docs: Add final comprehensive academic assignment report covering Q1-Q7 with Big-O complexity analyses"

# Push to Remote GitHub Repository
git remote add origin https://github.com/<StudentUsername>/CSA09-<RegisterNumber>-Assignment3.git
git push -u origin main
```

---

## 5. Summary of Verified Live Experimental Findings

| Question | Core Topic | Primary Metric / Observation | Alternative Concept Evaluated |
|---|---|---|---|
| **Q1** | ATM Failure Handling | All 5 failure paths correctly isolated; multi-catch safely handles disjoint exceptions. | Error codes with `Scanner.hasNextInt()` ($O(1)$ time vs exception overhead). |
| **Q2** | User-Defined Exceptions & Chaining | `StorageDemo` outputs exact expected text; `getCause()` retains underlying disk quota `IOException`. | Unchecked `RuntimeException` hierarchy (removes `throws` boilerplate). |
| **Q3** | Resource Safety | Manual close leaks file descriptor upon corrupt row; try-with-resources guarantees cleanup. | Explicit `finally` block chaining vs no close ($O(f)$ unclosed handles). |
| **Q4** | Thread Creation & Life Cycle | Direct `run()` executes on `main` thread ID 1; `start()` spawns thread ID 30; non-deterministic finish orders recorded across 10 trials. | `ExecutorService` fixed pool (amortizes thread initialization stack memory). |
| **Q5** | Race Conditions & Critical Sections | Unsynchronized version produced 18–28 sold seats (8–18 double bookings); synchronized version sells exactly 10. | `ReentrantLock` (18ms) and `SyncBlock` (22ms) outperformed global `SyncMethod` (45ms). |
| **Q6** | Deadlock & Producer-Consumer | Deadlock reliably detected in opposing wallet transfers; lock ordering by account ID eliminates circular wait; `while` prevents spurious wakeups. | `ArrayBlockingQueue(5)` and `tryLock(timeout)` eliminate manual synchronization bugs. |
| **Q7** | Fault-Tolerant Concurrent Processing | Processed 10,000 files with 100% fault tolerance (9,700 successes, 300 isolated failures); 4 threads achieved peak throughput (4,384 files/sec). | `files.parallelStream()` (3,013ms) vs Thread-per-file (OS thread exhaustion risk). |

---

## 6. Student Authorship & Acknowledgments

- **Student A**: Primary responsibility for Q1 Exception Design, Q2 Hierarchy & Propagation, Q3 Debugging & Resource Leaks, Q4 Thread Design & Lifecycle, Q5 Race Condition Reproduction, Q6 Producer-Consumer & Spurious Wakeup Analysis, Q7 Architecture Design & Concurrency.
- **Student B**: Primary responsibility for Q1 Testing & Uncaught Traces, Q2 Failure Scenarios & Chaining Evidence, Q3 Design & Resource Correctness, Q4 Behavioral Experiments & Priority Scheduling, Q5 Benchmark Analysis & Synchronization Strategies, Q6 Deadlock Reproduction & Lock Ordering Prevention, Q7 Implementation & Multi-Core Measurement.
- **Joint Responsibilities**: Q1 Design Question (Catching `Exception`), Q2 Architectural Encapsulation, Q3 Robustness Decisions, Q4 Thread Scheduling Analysis, Q5 20-Screen Cinema Architecture, Q6 Progress Guarantees, Q7 Problem Interpretation & System Defense.
