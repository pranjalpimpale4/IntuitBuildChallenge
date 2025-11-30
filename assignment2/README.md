# ProsperityConnect: Intuit Ecosystem Intelligence Platform

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Sample Output](#sample-output)
- [Testing](#testing)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Key Files Explained](#key-files-explained)
- [Troubleshooting](#troubleshooting)
- [Dataset Design and Assumptions](#dataset-design-and-assumptions)
- [Additional Documentation](#additional-documentation)
- [Author](#author)

---

## Overview

**ProsperityConnect** is an analytics platform that provides strategic insights into customer subscription ecosystems. It processes subscription telemetry data to deliver actionable business intelligence through six analytical modules:

1. **Ecosystem Depth Analysis** - Customer stickiness and product adoption
2. **Revenue Matrix** - Product performance by market segment
3. **Golden Bundle Finder** - Top revenue-generating product combinations
4. **Strategic Risk Alerts** - Churn detection for high-value customers
5. **Next Best Action Engine** - AI-driven upsell recommendations
6. **Segment Unit Economics** - Statistical analysis and ARPU metrics

---

## Features

- **Parallel Stream Processing** - Multi-core performance for large datasets
- **Functional Programming** - Immutable data structures and stateless operations
- **Comprehensive Analytics** - Six modules covering key business metrics
- **Color-Coded Dashboard** - Professional ANSI-colored console output
- **Data Quality Validation** - Robust error handling and input validation
- **High Test Coverage** - 95%+ test coverage with comprehensive unit tests

---

## Setup Instructions

### Step 1: Clone or Download the Project

```bash
# If using Git
git clone https://github.com/pranjalpimpale4/IntuitBuildChallenge
cd assignment2

# Or extract the project ZIP file to your desired location
```

### Step 2: Verify Project Structure

Ensure your project has the following structure:

```
assignment2/
├── pom.xml
├── README.md
├── BUSINESS_EXPLANATION.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── pranjal/
    │   │           └── assign2/
    │   │               ├── App.java
    │   │               ├── ConsoleColors.java
    │   │               ├── DataSeeder.java
    │   │               ├── model/
    │   │               │   └── SubscriptionRecord.java
    │   │               └── service/
    │   │                   ├── DataIngestionService.java
    │   │                   └── ProsperityAnalyticsService.java
    │   └── resources/
    │       └── prosperity_ecosystem_data.csv (generated)
    └── test/
        └── java/
            └── com/
                └── pranjal/
                    └── assign2/
                        └── [test files]
```

### Step 3: Generate Sample Data

First, generate the sample dataset:

```bash
# Compile the project
mvn clean compile

# Run DataSeeder to generate the CSV file
mvn exec:java -Dexec.mainClass="com.pranjal.assign2.DataSeeder"
```

**Expected Output:**
```
Generating High-Fidelity Intuit Ecosystem Dataset...
SUCCESS: Generated 773 lines of high-entropy data.
```

This creates `src/main/resources/prosperity_ecosystem_data.csv` with synthetic subscription data.

### Step 4: Verify Data File

Check that the CSV file was created:

```bash
# Windows
dir src\main\resources\prosperity_ecosystem_data.csv

# Linux/Mac
ls -lh src/main/resources/prosperity_ecosystem_data.csv
```

---

## Running the Application

### Option 1: Using Maven

```bash
# Run the main application
mvn exec:java -Dexec.mainClass="com.pranjal.assign2.App"
```

### Option 2: Using Java Directly

```bash
# Compile
mvn clean compile

# Run
java -cp target/classes com.pranjal.assign2.App
```

### Option 3: Using Your IDE

1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Navigate to `src/main/java/com/pranjal/assign2/App.java`
3. Right-click → Run `App.main()`

---

## Project Structure

```
assignment2/
│
├── src/main/java/com/pranjal/assign2/
│   ├── App.java                          # Main application entry point
│   ├── ConsoleColors.java                # ANSI color codes utility
│   ├── DataSeeder.java                  # Synthetic data generator
│   │
│   ├── model/
│   │   └── SubscriptionRecord.java      # Immutable data model
│   │
│   └── service/
│       ├── DataIngestionService.java    # CSV loading and parsing
│       └── ProsperityAnalyticsService.java  # Core analytics engine
│
├── src/main/resources/
│   └── prosperity_ecosystem_data.csv    # Generated dataset
│
└── src/test/java/com/pranjal/assign2/
    ├── AppTest.java                      # Integration tests
    ├── ConsoleColorsTest.java           # Color utility tests
    ├── DataSeederTest.java              # Data generation tests
    ├── model/
    │   └── SubscriptionRecordTest.java  # Model tests
    └── service/
        ├── DataIngestionServiceTest.java
        └── ProsperityAnalyticsServiceTest.java
```

---

## Sample Output

When you run the application, you'll see a color-coded dashboard:

```
================================================================================
   PROSPERITY CONNECT  ::  INTUIT ECOSYSTEM INTELLIGENCE PLATFORM (v1.3)
   Context: Live Data Simulation | Period: Q4 2025
================================================================================

[1] ECOSYSTEM DEPTH & STICKINESS (The "Network Effect")
    1 Product(s) : ████████████████████████████████ (250 Customers)
    2 Product(s) : ████████████████████ (120 Customers)
    3 Product(s) : ██████ (45 Customers)

[2] REVENUE MATRIX (Product x Segment Performance)
    ----------------------------------------------------------------------------
    PRODUCT              | SMB MRR         | CONSUMER MRR    
    ----------------------------------------------------------------------------
    QB_ONLINE            | $ 21250.00      | $ 0.00         
    QB_PAYROLL           | $ 9750.00       | $ 0.00         
    TURBOTAX             | $ 0.00          | $ 0.00         
    MAILCHIMP            | $ 2250.00       | $ 0.00         

[3] GOLDEN BUNDLES (Highest Revenue Combinations)
    QB_ONLINE + QB_PAYROLL           :: $ 15000.00 MRR
    QB_ONLINE + MAILCHIMP            :: $ 8500.00 MRR
    QB_ONLINE + QB_PAYROLL + MAILCHIMP :: $ 195.00 MRR

[4] STRATEGIC HEALTH ALERTS (Churn Risk Detection)
    (!) ALERT: C-RISK-99 [SMB] - Risk: 46 days inactive, MRR: $250.00
    (!) ALERT: C-8821 [SMB] - Risk: 45 days inactive, MRR: $250.00

[5] "NEXT BEST ACTION" ENGINE (AI Recommendations)
    CUSTOMER   | SEGMENT | CURRENT STACK   | RECOMMENDATION      | LOGIC
    ----------------------------------------------------------------------------
    C-NBA-100  | SMB     | QB_ONLINE       | + ADD PAYROLL       | Users > 5
    C-7721     | SMB     | QB_ONLINE       | + ADD PAYROLL       | Users > 5
    C-NBA-200  | CONS    | TURBOTAX        | + CREDIT KARMA      | Loyal User

[6] SEGMENT UNIT ECONOMICS (Statistical Breakdown)
    SEGMENT    |  AVG MRR  |  MAX MRR  |  VOLUME
    ------------------------------------------------
    SMB        | $  85.50  | $ 250.00  |   300
    CONSUMER   | $   5.20  | $ 120.00  |   200
    ACCOUNTANT | $ 250.00  | $ 500.00  |    75

================================================================================
   SYSTEM STATUS: Analysis Complete.
================================================================================
```

**Note:** Colors will appear in terminals that support ANSI escape codes (Windows Terminal, PowerShell, Unix terminals).

---

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
# Test analytics service
mvn test -Dtest=ProsperityAnalyticsServiceTest

# Test data ingestion
mvn test -Dtest=DataIngestionServiceTest

# Test data model
mvn test -Dtest=SubscriptionRecordTest
```

### Test Coverage

The project includes comprehensive unit tests with **95%+ coverage**:

- Functional programming patterns
- Stream operations
- Data aggregation
- Lambda expressions
- Edge cases and error handling
- Integration tests

### View Test Report

```bash
mvn test
# Reports available in: target/surefire-reports/
```

---

## Technology Stack

- **Java 17** - Modern Java features (Records, Streams, Pattern Matching)
- **Maven 3.11+** - Build automation and dependency management
- **JUnit 5.10** - Modern testing framework
- **Java Streams API** - Functional programming and parallel processing
- **BigDecimal** - Financial precision for revenue calculations
- **ANSI Colors** - Terminal color formatting

---

## Architecture

### Design Patterns

- **Layered Architecture**: Separation of concerns (Data → Business → Presentation)
- **Functional Programming**: Immutable data, stateless operations
- **Service Layer Pattern**: Business logic encapsulated in services
- **Record Pattern**: Modern Java data modeling

### Key Principles

1. **Immutability** - All data structures are immutable for thread safety
2. **Stateless Operations** - Analytics methods are pure functions
3. **Parallel Processing** - Leverages multi-core CPUs for performance
4. **Type Safety** - Strong typing prevents runtime errors
5. **Error Handling** - Graceful degradation on failures

### Data Flow

```
CSV File → DataIngestionService → SubscriptionRecord[] 
    → ProsperityAnalyticsService → Analytics Results 
    → App (Dashboard) → Console Output
```

---

## Key Files Explained

### `App.java`
- **Purpose**: Application entry point and dashboard rendering
- **Key Features**: Orchestrates data pipeline and displays analytics

### `DataSeeder.java`
- **Purpose**: Generates synthetic subscription data
- **Key Features**: Realistic data distribution, strategic scenario injection

### `DataIngestionService.java`
- **Purpose**: CSV file loading and parsing
- **Key Features**: Parallel processing, data validation, error handling

### `ProsperityAnalyticsService.java`
- **Purpose**: Core analytics engine with 6 modules
- **Key Features**: Functional programming, complex aggregations, business rules

### `SubscriptionRecord.java`
- **Purpose**: Immutable data model for subscription records
- **Key Features**: Type safety, financial precision, thread safety

### `ConsoleColors.java`
- **Purpose**: ANSI color codes for terminal output
- **Key Features**: Professional dashboard styling

---

## Troubleshooting

### Issue: "File not found" error

**Solution:**
```bash
# Ensure DataSeeder has been run first
mvn exec:java -Dexec.mainClass="com.pranjal.assign2.DataSeeder"
```

### Issue: Colors not showing

**Solution:**
- Use Windows Terminal or PowerShell (not Command Prompt)
- On Linux/Mac, ensure terminal supports ANSI codes
- Colors are optional - functionality works without them

---

## Dataset Design and Assumptions

### Why Subscription/Sales Data?

This application uses subscription telemetry data (similar to sales data) because:
- **Complex Aggregation Scenarios**: Demonstrates revenue aggregation, customer segmentation, and product bundle analysis
- **Multiple Analytical Dimensions**: Enables analysis across customer, product, segment, and behavioral dimensions
- **Real-World Relevance**: Models real SaaS/subscription business scenarios with practical business questions
- **Rich Data Structure**: 11 columns provide sufficient complexity for demonstrating advanced Stream operations

### CSV Schema Design (11 Columns)

The dataset structure was designed to support comprehensive analytical queries:

| Column | Type | Purpose | Example |
|--------|------|---------|---------|
| **customer_id** | String | Unique identifier for customer grouping and aggregation | "C-1001" |
| **segment** | String | Market segmentation for multi-dimensional analysis | "SMB", "CONSUMER", "ACCOUNTANT" |
| **product** | String | Product SKU for product-level aggregations and bundle analysis | "QB_ONLINE", "TURBOTAX" |
| **mrr** | BigDecimal | Monthly Recurring Revenue with financial precision | 85.00 |
| **active_users** | Integer | Usage-based metrics for behavioral analysis | 3, 10, 15 |
| **ai_enabled** | Boolean | Feature adoption flag for cross-sell analysis | true, false |
| **live_expert** | Boolean | Premium feature indicator | true, false |
| **data_connections** | Integer | Integration complexity metric | 0, 5, 25 |
| **is_trial** | Boolean | Trial status for conversion analysis | true, false |
| **churn_signal** | Boolean | Cancellation indicator for risk detection | true, false |
| **days_inactive** | Integer | Engagement metric for churn prediction | 0, 45, 300 |

### Data Distribution Assumptions

The synthetic dataset follows realistic business patterns:

- **45% SMB Segment**: Core revenue drivers with multi-product bundles (QB_ONLINE + QB_PAYROLL + MAILCHIMP)
- **40% Consumer Segment**: High volume, freemium model with upsell opportunities (TURBOTAX → CREDIT_KARMA)
- **15% Accountant Segment**: Premium "whale" customers with high MRR and advanced features

**Rationale**: This distribution enables testing of:
- Segment-based aggregations
- Product bundle identification
- Revenue optimization across different customer types
- Churn risk analysis for high-value segments

### Dataset Generation Strategy

The `DataSeeder` utility generates 500+ customer profiles with:
- **Realistic Variance**: Price fluctuations simulate market dynamics
- **Strategic Scenarios**: Injects "legendary" test cases (golden bundles, churn risks, upsell opportunities)
- **High Entropy**: Shuffled customer IDs prevent pattern detection
- **Multi-Product Customers**: Enables bundle analysis and ecosystem depth calculations

### Key Assumptions

1. **One Row Per Product**: Each CSV row represents one product subscription per customer (customers can have multiple rows)
2. **Financial Precision**: MRR uses BigDecimal to prevent rounding errors in aggregations
3. **Boolean Flags**: Simple true/false values for feature adoption and status indicators
4. **Temporal Metrics**: Days inactive represents engagement over time for churn prediction
5. **Segment Classification**: Three distinct segments with different revenue patterns and behaviors

### Why This Dataset Fits the Problem

This subscription dataset is ideal for demonstrating Stream operations because:
- **Grouping Operations**: Multiple grouping dimensions (customer, product, segment)
- **Aggregation Complexity**: Revenue sums, counts, averages, and statistical summaries
- **Filtering Scenarios**: Risk detection, upsell opportunities, bundle identification
- **Multi-Level Analysis**: Nested maps (Product → Segment → Revenue) demonstrate advanced Stream techniques
- **Real-World Patterns**: Mirrors actual business intelligence use cases

---

## Additional Documentation

- **BUSINESS_EXPLANATION.md** - Detailed business rationale for each component
- **JavaDoc Comments** - Inline documentation in all source files
- **Test Files** - Comprehensive examples of usage

---

## Author

**Pranjal**  
*Version 1.3*

