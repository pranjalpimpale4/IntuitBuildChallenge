# Producer-Consumer Simulation System

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Monitoring & Logging](#monitoring--logging)
- [Testing](#testing)
- [Dependencies](#dependencies)
- [Key Design Patterns](#key-design-patterns)
- [System Flow](#system-flow)

## Overview

A high-performance, thread-safe producer-consumer simulation system. Features include fair locking, safe shutdown patterns, dependency injection, and comprehensive test coverage.

> **For setup instructions, running the application, and sample output, see [README.md](README.md)**

---

## Architecture

### Core Components

#### 1. **Configuration Class**
Encapsulates all system parameters with immutability and validation.

```java
// Factory methods for different initialization modes
Configuration config = Configuration.fromUserInput();  // Interactive
Configuration config = Configuration.withDefaults();   // Default values
Configuration config = Configuration.custom(4, 20, 2, 10);  // Programmatic
```

**Features:**
- Input validation (throws `IllegalArgumentException` for invalid values)
- Immutable design pattern
- Calculates total expected items
- Clean toString() for debugging

#### 2. **SimulationEngine Class**
Orchestrates the entire simulation lifecycle using modern concurrency patterns.

```java
SimulationEngine engine = new SimulationEngine(config);
engine.start();              // Initialize and start all components
engine.waitForCompletion();  // Wait for all items to be processed
engine.shutdown();           // Safe shutdown sequence
```

**Responsibilities:**
- Creates and wires all dependencies
- Manages lifecycle (start → wait → shutdown)
- Implements safe shutdown pattern
- Encapsulates complexity

#### 3. **AdvancedBlockingQueue<T>**
Thread-safe, fair blocking queue with custom implementation.

**Features:**
- Fair ReentrantLock (FIFO ordering)
- Blocking put/take operations
- Timeout-based offer/poll operations
- Validates capacity > 0 and non-null items

#### 4. **SystemMetrics (implements MetricsCollector)**
Centralized metrics collection using AtomicLong.

**Features:**
- Thread-safe counters for production/consumption
- Breaks circular dependency between Dashboard and ThreadManager
- Shared component for all workers

#### 5. **ThreadManager**
Manages all worker threads using ExecutorService pools.

**Features:**
- CachedThreadPool for producers
- CachedThreadPool for consumers
- ScheduledExecutorService for auto-scaler
- Graceful shutdown with awaitTermination
- Auto-scaling based on queue load

**What is Auto-Scaling?**
Auto-scaling is a dynamic resource management mechanism that automatically adjusts the number of consumer threads based on the current system load. In this system:

- **Load Calculation**: The system continuously monitors the queue load percentage (current queue size / capacity * 100)
- **Threshold**: When queue load exceeds 75%, the system detects high load
- **Scaling Action**: Emergency consumer threads are automatically deployed to process the backlog
- **Purpose**: Prevents queue overflow, reduces processing delays, and ensures system responsiveness under varying workloads
- **Implementation**: The auto-scaler runs as a periodic task (every 1 second) using ScheduledExecutorService, checking queue status and deploying additional consumers as needed
- **Emergency Consumers**: These are temporary consumer threads that help clear the backlog. They are tracked separately and included in the final analysis report

#### 6. **Dashboard (implements MetricsCollector)**
Real-time monitoring using ScheduledExecutorService.

**Features:**
- Updates every 500ms (precise scheduling)
- Displays produced/consumed counts
- Shows queue size and active consumers
- Implements MetricsCollector for workers

#### 7. **ProducerWorker & ConsumerWorker**
Worker threads with dependency injection.

**Features:**
- Accept MetricsCollector via constructor (decoupled)
- Use ThreadLocalRandom (no contention)
- Proper InterruptedException handling
- Type-safe poison pill (QueueCommand.POISON_PILL)

---

## Monitoring & Logging

### Dashboard Output
```
╔═══════════════════════════════════════════╗
║          SYSTEM DASHBOARD                 ║
╠═══════════════════════════════════════════╣
║ Produced:  45 / 80                        ║
║ Consumed:  38 / 80                        ║
║ Queue:     [####......] 7/10              ║
║ Consumers: 3 active                       ║
╚═══════════════════════════════════════════╝
```

### Execution History Log
All events are logged to `execution_history.log`:
```
[2024-11-29 10:23:45] Logger Initialized. Recording events...
[2024-11-29 10:23:45] Producer-1 produced: Package-1
[2024-11-29 10:23:45] Consumer-Worker-1 consumed: Package-1
[2024-11-29 10:23:46] AUTO-SCALE: +1 consumer (load 80%)
[2024-11-29 10:23:50] System Shutdown. Saving final log...
```

---

## Testing

### Running Tests
```bash
cd assignment1
mvn clean test
```

### Test Suite Coverage

| Test File                  | Tests   | Focus                                    |
|---------------------------|---------|------------------------------------------|
| **AdvancedQueueTest**     | 8       | Queue mechanics, blocking, timeouts      |
| **ComponentCoverageTest** | 17      | Worker white-box testing                  |
| **SystemIntegrationTest** | 3       | End-to-end integration                    |
| **SystemMetricsTest**     | 11      | Thread-safe metrics                       |
| **ConfigurationTest**     | 24      | Config validation                         |
| **SimulationEngineTest**  | 39      | Engine lifecycle, analysis results        |
| **DeadlockAndStarvationTest** | 16   | Deadlock, starvation, livelock            |
| **TOTAL**                 | **118** | **Comprehensive coverage**                |

### What's Tested:
- Deadlock prevention (7 tests)
- Starvation resistance (4 tests)
- Livelock detection (1 test)
- Race conditions (3 tests)
- Thread safety (8+ tests)
- Edge cases (30+ tests)
- Input validation (15+ tests)
- Safe shutdown pattern
- ExecutorService lifecycle
- Fair lock behavior


See `TEST_COVERAGE_ANALYSIS.md` for detailed report.

---

## Dependencies

### Required (Maven):
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
```

### JDK Requirements:
- **Minimum**: Java 8 (for ThreadLocalRandom)
- **Recommended**: Java 11+ (for modern API support)

---

## Key Design Patterns

1. **Producer-Consumer Pattern**: Core concurrency pattern
2. **Poison Pill Pattern**: Safe thread termination
3. **Factory Pattern**: Configuration creation
4. **Dependency Injection**: Constructor-based DI
5. **Interface Segregation**: MetricsCollector interface
6. **Immutability**: Configuration class
7. **Thread Pool Pattern**: ExecutorService usage
8. **Safe Shutdown Pattern**: Graceful termination

---

## System Flow

```
┌─────────────────────────────────────────────────────┐
│ 1. Main.java                                        │
│    └─> Parse args → Create Configuration           │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 2. SimulationEngine                                 │
│    └─> Wire dependencies (Queue, Metrics, Manager) │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 3. Start Phase                                      │
│    ├─> Dashboard.start() (ScheduledExecutorService)│
│    ├─> ThreadManager.startProducers() (ExecutorS.) │
│    ├─> ThreadManager.startConsumers() (ExecutorS.) │
│    └─> ThreadManager.startAutoScaler() (Scheduled) │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 4. Execution Phase                                  │
│    ├─> ProducerWorkers add items to queue          │
│    ├─> ConsumerWorkers process items from queue    │
│    ├─> SystemMetrics tracks progress               │
│    ├─> Dashboard displays real-time status         │
│    └─> AutoScaler adjusts consumer count           │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 5. Completion Phase                                 │
│    └─> Wait until all items consumed               │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ 6. Shutdown Phase (SAFE PATTERN)                   │
│    ├─> 1. Stop producers (shutdown + await)        │
│    ├─> 2. Insert poison pill                       │
│    ├─> 3. Wait for consumers to drain              │
│    └─> 4. Stop dashboard                           │
└─────────────────────────────────────────────────────┘
```

- `TEST_COVERAGE_ANALYSIS.md` - Comprehensive test breakdown
- `DEADLOCK_STARVATION_REPORT.md` - Concurrency analysis report
