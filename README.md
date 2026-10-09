# Evil Grandpa Marketplace for Android

A fictional, offline marketplace parody with a dark red-and-black interface. Listings, chats, reviews, suspicious-listing reports, offers, and receipts are stored locally on the device. The app has no payment integration and makes no network requests.

## Features

- Search fictional listings and filter by category
- Open listing details, send local messages, or post your own listing
- Write and save star reviews
- Make ridiculous offers and flag suspicious listings
- Save fake purchase receipts without charging anything
- Persist app data between launches with Android local storage

## Build the APK

Every push to `main` and every manual workflow run builds a debug APK with GitHub Actions. Download the `Evil-Grandpa-Marketplace-APK` artifact from the completed workflow run; the file inside is named `Evil_Grandpa_Marketplace.apk`.

The APK is debug-signed for sideloading and testing. It is not a Play Store release.
