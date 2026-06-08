<div align="center">

# ⚡ Grid & Gadgets - Machine Learning Pipelines

### *Advanced Predictive Modeling for Energy Grids and Tech Markets*

[![KNIME](https://img.shields.io/badge/KNIME-Analytics_Platform-yellow.svg)](https://www.knime.com/)
[![Machine Learning](https://img.shields.io/badge/AI-Machine_Learning-orange.svg)]()

---

**ADI-2526** is an academic Machine Learning project focused on building robust end-to-end data science pipelines. It tackles two distinct domains: predicting energy production levels using weather sensor data (Classification/Regression) and estimating laptop market prices through complex string parsing and feature engineering (Regression).

[Features](#-key-features) • [Datasets](#-datasets) • [Pipelines](#-knime-pipelines) • [Documentation](#-documentation) • [Authors](#-authors)

---

</div>

## ✨ Key Features

<table>
<tr>
<td width="50%">

### 🧹 Advanced Data Cleaning
- Fine-grained handling of missing values and textual errors
- Domain-specific physics rules (e.g., Solar = 0 at night)
- Smart string extraction for dirty text data

</td>
<td width="50%">

### 🤖 Multi-Task Machine Learning
- **Classification:** Predicting energy production categories
- **Regression:** Estimating total network load and laptop prices
- Algorithms: Gradient Boosted Trees, Random Forest, etc.

</td>
</tr>
<tr>
<td width="50%">

### ⚙️ Hyperparameter Optimization
- Automated tuning using Loop Nodes
- Evaluation of Brute Force vs Random Search
- Cross-Validation for robust statistical validation

</td>
<td width="50%">

### 📊 Exploratory Data Analysis
- Interactive correlation heatmaps
- Non-linear power curve visualizations
- In-depth feature importance and residual analysis

</td>
</tr>
</table>

---

## 📂 Datasets

### 1️⃣ Energy Production Dataset (Attributed to the Group by Teaching Staff)
- **Domain:** Renewable & Traditional Energy Grid
- **Goal:** Predict categorical production levels (`Productions`) and total numerical load (`Distribution Network (kWh)`).
- **Challenges:** Sensor noise, class imbalance (rare classes), missing weather data, physics violations.

### 2️⃣ Laptop Price Dataset (Chosen Dataset)
- **Domain:** E-commerce / Tech Hardware
- **Goal:** Predict the final retail price (`Price_euros`) based on hardware specs.
- **Challenges:** Extremely dirty structural text (e.g., extracting "1000" from "1TB HDD + 256GB SSD"), unit removal, and brand tax analysis.

---

## 🚀 KNIME Pipelines

The project relies on visually programmed workflows using **KNIME Analytics Platform**.

### ⚡ Energy Pipeline

1. **Ingestion:** Casting "nan" texts and rounding decimal noise.
2. **EDA:** Generating Heatmaps, Scatter Plots (Power Curves), and Pie Charts.
3. **Expert Rules:** Applying solar/wind physics constraints via Rule Engines.
4. **The Split:** Branching into robust Classification (Target Fix, Stratified Sampling) and surgical Regression (Interpolation, Z-Score, Outlier Removal).

### 💻 Laptop Pipeline

1. **Structural Cleaning:** Removing "GB" and "kg" units.
2. **Feature Engineering:** Splitting complex CPU and Memory strings using Regex/Cell Splitters.
3. **Modeling:** One-Hot Encoding and Gradient Boosting/Random Forest Regression for price prediction.

---

## 📚 Documentation & Report

The repository culminates in a comprehensive 30-page academic report detailing the entire CRISP-DM lifecycle:

- **Executive Summary & Domain Analysis**
- **Exploratory Data Analysis (Visual Proofs)**
- **Methodology & Data Prep**
- **10 Formal Experiments** (Testing SMOTE, Scalers, Imputations, etc.)
- **Results & Residual Analysis**

---

## 👥 Authors

**Group 6** - *Intelligent Learning and Decision Making (2025/2026)*

<table>
<tr>
<td align="center">
<a href="https://github.com/DelgadoDevT">
<img src="https://github.com/DelgadoDevT.png" width="100px;" alt="DelgadoDevT"/><br />
<sub><b>DelgadoDevT</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/PaoComPlanta">
<img src="https://github.com/PaoComPlanta.png" width="100px;" alt="PaoComPlanta"/><br />
<sub><b>PaoComPlanta</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/M4chad0">
<img src="https://github.com/M4chad0.png" width="100px;" alt="M4chad0"/><br />
<sub><b>M4chad0</b></sub>
</a>
</td>
</tr>
</table>