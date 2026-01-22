# MediCare Pharmacy Customer System

[![Java](https://img.shields.io/badge/Language-Java-orange)](https://www.java.com/) 
[![SQL Server](https://img.shields.io/badge/Database-SQL%20Server-blue)](https://www.microsoft.com/en-us/sql-server)

**Desktop application** for managing pharmacy customer orders with medicine inventory and order summaries.

---

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technical Details](#technical-details)
- [Class Overview](#class-overview)
- [Screenshots](#screenshots)
- [Installation & Run](#installation--run)
- [Future Improvements](#future-improvements)
- [Author](#author)

---

## Overview
**MediCare Pharmacy Customer System** is a Java Swing desktop application that allows pharmacies to:  
- Collect customer information  
- Maintain adult and child medicine inventories  
- Place orders, track stock, and suggest alternatives  
- Generate detailed order summaries in a GUI  

It ensures accurate dosage, stock management, and age-based medicine selection.

---

## Features

### Customer Management
- Collects details: Name, DOB, Gender, Contact, Address, Allergies, Current Medications
- Calculates age and determines if the customer is a child
- Displays customer info in a GUI dialog

### Medicine Inventory
- Separate adult and child medicine inventories
- Details include: Name, Use, Dosage, Price, Stock
- GUI table with sorting for browsing medicines
- Validates stock before adding to order

### Ordering System
- Place new orders for adults or children
- Add multiple medicines with quantity selection
- Suggests alternatives if stock is insufficient
- Generates detailed **Order Summary** with customer info, items, dosage, and total cost

### GUI Interface
- Main menu with **Place Order**, **Browse Medicines**, and **Exit**
- Header with pharmacy name and slogan, footer with contact info
- Buttons styled for clarity and usability
- Pop-up forms for customer info and medicine selection

### Database Integration
- Microsoft SQL Server with **integrated security**
- Tables: `medicines`, `customers`, `orders`, `order_items`
- Supports CRUD operations for orders and customer data

---

## Technical Details
- **Language:** Java  
- **GUI:** Swing  
- **Database:** SQL Server  
- **Concepts:** OOP, Event-driven programming, Data validation, LocalDate/Period usage, Inventory management

---

## Class Overview

| Class | Responsibility |
|-------|----------------|
| `DatabaseManager` | Manages SQL Server connection and DB initialization |
| `Person` | Base class for individuals (customers, staff) |
| `Customer` | Extends `Person`, adds DOB, allergies, medications, GUI info display |
| `Medicine` | Represents a medicine, manages stock and details |
| `Order` | Represents a customer order, generates order ID and summary |
| `OrderItem` | Represents a medicine item in an order |
| `PharmacySystemGUI` | Main GUI system, handles orders and inventory |
| `InventoryManager` | Interface to view and search inventory |

---

## Installation & Run

### Database Setup
1. Install SQL Server and create a database `PharmacyDB`.
2. Ensure `sqljdbc_auth.dll` path matches your system architecture.
3. Use integrated security for the JDBC connection.

### Compile and Run
```bash
javac PharmacyCustomerSystem.java
java PharmacyCustomerSystem
