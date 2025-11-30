# Producer-Consumer Simulation System

A high-performance, thread-safe producer-consumer simulation system demonstrating modern Java concurrency best practices.

## Table of Contents

- [Overview](#overview)
- [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [Sample Output](#sample-output)
- [Output Storage](#output-storage)
- [System Behavior](#system-behavior)
- [Troubleshooting](#troubleshooting)
- [Additional Documentation](#additional-documentation)

## Overview

This system demonstrates a high-performance, thread-safe producer-consumer pattern with modern Java concurrency best practices including fair locking, safe shutdown patterns, dependency injection, and comprehensive test coverage.

## Setup Instructions

### Prerequisites
- **JDK**: Java 8 or higher (Java 11+ recommended)
- **Maven**: 3.6+ (for building and running)

### Build the Project
```bash
cd assignment1
mvn clean compile
```

### Run Tests
```bash
mvn clean test
```

## Running the Application

### Option 1: Interactive Mode (Recommended)
Simply run without arguments to enter interactive configuration:

```bash
cd assignment1
mvn compile exec:java -Dexec.mainClass="com.pranjal.assign1.Main"
```

Or with your IDE: Just click "Run"

**You'll be prompted:**
```
=== SIMULATION CONFIGURATION ===

Enter Number of Producers: 4
Enter Items per Producer: 10
Enter Number of Consumers: 2
Enter Queue Capacity: 5

Configuration Loaded.
Starting System...
```

### Option 2: Default Configuration
Use built-in defaults (4 producers, 20 items each, 2 consumers, capacity 10):

```bash
mvn compile exec:java -Dexec.mainClass="com.pranjal.assign1.Main" -Dexec.args="--default"
```

### Option 3: Command-Line Arguments
Specify configuration directly:

```bash
mvn compile exec:java -Dexec.mainClass="com.pranjal.assign1.Main" -Dexec.args="4 20 2 10"
```

Format: `<producers> <itemsPerProducer> <consumers> <capacity>`

## Sample Output

### During Execution
The system displays a real-time dashboard that updates every 500ms:

```
 [SYSTEM STATUS] Queue: [##########----------] 10/10 | Producers: 4 | Consumers: 2 | Total In: 45 | Total Out: 38
```

The dashboard shows:
- **Queue**: Visual progress bar and current size/capacity
- **Producers**: Number of active producer threads
- **Consumers**: Number of active consumer threads (including emergency)
- **Total In**: Cumulative items produced
- **Total Out**: Cumulative items consumed

### Auto-Scaling Messages
When queue load exceeds 75%, emergency consumers are automatically deployed:

```
>>> [AUTO-SCALER] High Load detected (85.0%). Deploying Emergency Consumer 1
```

### Final Analysis Results
After completion, a comprehensive analysis report is printed:

```
======================================================================
                    SIMULATION ANALYSIS RESULTS
======================================================================

[CONFIGURATION]
  Producers Started:        4
  Items per Producer:       20
  Consumers Started:        2
  Emergency Consumers Added: 1
  Queue Capacity:           10
  Total Items Expected:     80

[EXECUTION RESULTS]
  Items Produced:           80
  Items Consumed:           80
  Final Queue Size:         0

[ANALYSIS]
  Production Rate:          100.0%
  Consumption Rate:         100.0%
  Status:                   SUCCESS - All items processed

[SYSTEM SUMMARY]
  Total Consumers Used:     3 (2 initial + 1 emergency)
  Shutdown Status:          Clean (All threads terminated)

======================================================================
Detailed logs available in: assignment1/execution_history.log
======================================================================
```

## Output Storage

### Console Output
- **Real-time Dashboard**: Displayed in console, updates every 500ms
- **Auto-Scaler Messages**: Printed to console when emergency consumers are deployed
- **Final Analysis Report**: Printed to console at the end of simulation

### Log File
All detailed events are logged to a file:

**Location**: `assignment1/execution_history.log`

**Format**: 
```
[HH:mm:ss.SSS] [Component-Name] : Message
```

**Example Log Entries**:
```
[14:30:05.123] [SYSTEM          ] : Logger Initialized. Recording events...
[14:30:05.145] [Producer-1      ] : STARTED.
[14:30:05.156] [Consumer-1       ] : STARTED.
[14:30:05.234] [Producer-1      ] : ADDED Record-1-1 | Queue Size: 1
[14:30:05.267] [Consumer-1       ] : PROCESSED Record-1-1 | Queue Size: 0
[14:30:06.123] [AUTO-SCALER     ] : High Load detected (85.0%). Deploying Emergency Consumer 1
[14:30:10.456] [SYSTEM          ] : All 80 items processed.
[14:30:10.567] [SYSTEM          ] : Shutdown complete - all workers stopped gracefully.
[14:30:10.568] [SYSTEM          ] : System Shutdown. Closing logs.
```

**Log File Details**:
- Created automatically when the application starts
- Overwrites previous log file on each run
- Contains timestamps with millisecond precision
- Thread-safe logging (no interleaved messages)
- Flushed immediately to prevent data loss on crash

### Viewing Logs
To view the log file in real-time during execution:
```bash
# Linux/Mac
tail -f assignment1/execution_history.log

# Windows PowerShell
Get-Content assignment1/execution_history.log -Wait -Tail 20
```

## System Behavior

The system will:
1. Start the Dashboard (updates every 500ms)
2. Launch producers to create items
3. Launch consumers to process items
4. Auto-scale consumers based on queue load (when load > 75%)
5. Perform safe shutdown when complete
6. Print comprehensive analysis results

## Troubleshooting

### Log File Not Found
- Ensure you're running from the correct directory
- Check file permissions in the `assignment1` folder
- The log file is created in `assignment1/execution_history.log` relative to your working directory

### Simulation Hangs
- Check if all items are being produced/consumed
- Verify queue capacity is sufficient
- Review `execution_history.log` for error messages

## Additional Documentation

- `DESIGN.md` - Detailed architecture and design patterns
- `TEST_COVERAGE_ANALYSIS.md` - Comprehensive test coverage report
- `DEADLOCK_STARVATION_REPORT.md` - Concurrency analysis and safety verification

