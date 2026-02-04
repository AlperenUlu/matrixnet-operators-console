# 🕸️ MatrixNet: Network Analysis Console

MatrixNet is a Java-based network simulation and analysis engine designed around
**efficient graph algorithms**, **custom data structures**, and **predictable performance**
under dynamic constraints ⚙️.

The system models a network of hosts connected via bidirectional backdoors and provides
tools for optimal routing, connectivity analysis, and infrastructure vulnerability detection 🔍.

---

## ✨ Features

- Dynamic host and connection management
- Constraint-aware shortest path routing
- Bandwidth, firewall, and congestion modeling
- Connectivity and component analysis
- Articulation point and bridge detection
- Comprehensive topology reporting

---

## 🏗️ Architecture Overview

MatrixNet represents the network as a graph using adjacency lists, enabling scalable
operations on large and sparse topologies 📈.  
The design emphasizes **low overhead**, **deterministic behavior**, and **asymptotically
optimal algorithms**.

---

## 🧩 Data Structures

- **Custom Hash Table**
  - Used for fast host lookup by identifier
  - Average-case access time: **O(1)**
- **Adjacency Lists**
  - Memory-efficient graph representation
  - Space complexity: **O(V + E)**
- **Custom Minimum Heap**
  - Used in routing for priority-based exploration
  - Insert / extract-min: **O(log V)**

Built-in priority queues and maps are intentionally avoided to retain full control
over performance characteristics and tie-breaking behavior.

---

## 🧠 Algorithm Design Decisions

- Adjacency lists are chosen over adjacency matrices to avoid **O(V²)** memory usage.
- Routing uses a Dijkstra-style approach with dynamic edge weights to preserve
  optimal substructure.
- Constraints such as bandwidth, firewall level, and sealed connections are enforced
  **during traversal**, not as post-processing steps.
- Tie-breaking is handled deterministically to guarantee reproducible results.

---

## ⏱️ Time Complexity Analysis

### Host Management
| Operation | Complexity |
|---------|------------|
| Create host | **O(1)** average |
| Lookup host | **O(1)** average |

---

### Backdoor Management
| Operation | Complexity |
|----------|------------|
| Create connection | **O(1)** average |
| Seal / unseal connection | **O(1)** |

---

### Routing (Shortest Path)

Routing is performed using a priority-based traversal with dynamic latency calculation 🚦.

| Operation | Complexity |
|----------|------------|
| Route computation | **O((V + E) log V)** |
| Path reconstruction | **O(V)** |

- Supports bandwidth thresholds and per-hop firewall constraints
- Dynamic latency incorporates congestion factor (λ)
- Routes are compared by total latency, hop count, and lexicographic order

---

### 🔗 Connectivity Analysis

| Analysis | Complexity |
|---------|------------|
| Connectivity scan | **O(V + E)** |
| Cycle detection | **O(V + E)** |

---

### 🛡️ Vulnerability Detection

| Analysis | Complexity |
|---------|------------|
| Articulation points | **O(V + E)** |
| Bridges | **O(V + E)** |

Depth-first search–based algorithms with low-link value propagation are used to ensure
linear-time analysis.

---

## ⚠️ Edge Case Handling

- Self-routes are resolved without graph traversal.
- Multiple optimal paths are resolved deterministically.
- Invalid operations fail fast without partial state changes.
- Temporarily removed nodes or edges do not affect persistent state.

---
## 📁 Repository Structure

```bash
.
├── Main.java              # Program entry point
├── Host.java              # Host representation
├── Backdoor.java          # Bidirectional connection model
├── Path.java              # Route representation
├── MatrixManager.java     # Core orchestration logic
├── HashTable.java         # Custom hash table implementation
├── MinimumHeap.java       # Custom min-heap for routing
├── Entry.java             # Auxiliary data holder
├── test_runner.py         # Automated test runner
├── testcases/             # Input and expected output files
│   ├── input/
│   └── output/
├── README.md
└── .gitignore
```

---

## ▶️ Usage

### Compilation

```bash
javac *.java
```
### Execution

```bash
java Main <input_file> <output_file>
```
### Automated Testing

Basic automated testing is supported via a lightweight test runner.

```bash
python3 test_runner.py
```
The runner executes predefined input files and compares the produced
outputs against expected results to ensure correctness and format compliance.

## 🚀 Performance Considerations

- Core operations are linear or near-linear in graph size
- Constraint checks are integrated directly into traversal logic
- Custom data structures reduce abstraction overhead
- Designed to handle large-scale inputs efficiently

---

## 📌 Originality & Academic Integrity Notice

This project is an **original implementation** developed from scratch.
No external codebases, solution templates, or third-party algorithm
implementations were copied or reused.

The repository is provided **for educational and demonstrational purposes only**.
Any resemblance to other implementations is coincidental and limited to
standard algorithmic principles commonly found in academic literature.

---

## 🧾 Final Remarks

MatrixNet prioritizes **algorithmic efficiency**, **clarity of design**, and
**deterministic execution**.  
The project demonstrates how careful data structure selection and asymptotically
optimal algorithms enable scalable and reliable network analysis under complex
operational constraints.

