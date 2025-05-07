# Personal Expense Manager (Android App)

A full-featured Android app for personal and financial management, supporting cloud-based storage and offline access. This app allows users to track their transactions, manage expenses, and generate IR3 tax forms — with dedicated roles for users, accountants, and admins.

## 🚀 Overview

- Users can add, view, and edit expenses and income records.
- Supports offline mode using Room Database and synchronizes with Firebase Firestore when online.
- Accountants can generate IR3 tax forms in PDF format using iText, which are stored in Firebase Storage and available for download by users.
- Role-based access control (User, Accountant, Admin).
- Integrated Google Sign-In for user authentication.

## 🛠 Technologies

- **Android** (Java, Android Studio)
- **Firebase Firestore & Firebase Storage**
- **Room Database**
- **iText PDF generation**
- **Google Authentication**
- **MVVM Architecture (Clean separation of UI + Logic + Data)**

## 📌 Features

- Add, edit, delete transactions
- Offline-first → works without internet, syncs when reconnected
- Accountant-only IR form PDF generation module
- PDF stored in Firebase Storage, URL saved in Firestore
- Download PDF from user dashboard
- User roles and authentication with Google Sign-In
- Custom icons and UI with categorized transactions

## 📷 Screenshots



## 📝 How to run

1. Clone the repository.
2. Open with Android Studio.
3. Set up `google-services.json` (Firebase configuration).
4. Run the app on an Android device or emulator.

## 🎯 Future Improvements

- Add charts and analytics view
- Email notification when IR form is generated
- Further security hardening and offline performance tuning

## Author

**Liman Wu**
