# 🌾 Farmer2Market - Android Frontend App

## 📌 Overview
Farmer2Market is an Android mobile application that connects farmers directly with buyers. Farmers can upload products, and buyers can browse and communicate directly with them, reducing middlemen and improving market efficiency.

This frontend is built using **Kotlin** and **XML layouts** in Android Studio.

---

## 📱 Package Name
```
com.bignerdrange.farmer2market
```

---

## ⚙️ Tech Stack
- Language: Kotlin
- UI Design: XML (Android Layouts)
- Real-time Communication: WebSockets
- IDE: Android Studio
- Architecture: Activity-based structure

---

## 🚀 Features

### 🔐 Authentication
- User Login
- User Registration
- Role selection:
  - 🌾 Farmer
  - 🛒 Buyer

---

### 🏪 Marketplace
- Displays all products uploaded by farmers
- Anyone logged in can view products
- Product details include:
  - Name
  - Description
  - Price
  - Location
  - Product image

---

### 📍 Location-Based Filtering
- Buyers can filter products based on location
- Helps connect nearby farmers and buyers

---

### 🖼️ Product Management (Farmers)
- Upload products with images
- Add price, description, and location
- Manage listed products

---

### 💬 Chat System (Real-Time)
- Built using **WebSockets**
- Buyers can chat directly with farmers
- Real-time messaging without refreshing
- Supports continuous conversation flow
- - Push notifications for chat

---

## 🧭 App Screens

- Login Screen
- Register Screen (Farmer / Buyer selection)
- Marketplace Screen
- Product Upload Screen
- Chat Screen

---

## 🔄 App Flow

1. User opens app
2. Login or Register
3. Select role (Farmer or Buyer)
4. Redirected to Marketplace
5. View products with filters
6. Chat with farmers in real time (WebSockets)

---

## 📂 Project Structure (Simplified)

```
app/
│
├── java/com/bignerdrange/farmer2market/
│   ├── activities/
│   │   ├── LoginActivity.kt
│   │   ├── RegisterActivity.kt
│   │   ├── MarketplaceActivity.kt
│   │   ├── ChatActivity.kt
│   │   └── UploadProductActivity.kt
│   │
│   ├── adapters/
│   ├── models/
│   ├── network/
│   │   ├── WebSocketManager.kt
│   │   └── ApiClient.kt
│
├── res/
│   ├── layout/
│   ├── drawable/
│   └── values/
│
└── AndroidManifest.xml
```

---

## 🎯 Purpose of Project
This project was built to gain hands-on experience in:
- Android development using Kotlin
- XML UI design
- Real-time communication using WebSockets
- Image upload handling
- Location-based filtering systems
- Building a real-world marketplace application

---

## 👨‍💻 Developer
**Eliot Chitowamombe**

---

## 📌 Future Improvements
- Payment integration
- Rating system for farmers and buyers
- AI-based product recommendations
