# Pile ou Face

Application Android minimale : un écran, une pièce. On touche l'écran, la pièce
est lancée (animation de rotation, ombre, rebonds) et retombe **toujours sur PILE**.

## Obtenir l'APK

### Option 1 : Android Studio
1. Ouvrir ce dossier dans Android Studio (« Open »).
2. Laisser Gradle se synchroniser.
3. Menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. L'APK est dans `app/build/outputs/apk/debug/app-debug.apk`.

### Option 2 : ligne de commande
Prérequis : JDK 17 et Android SDK (`ANDROID_HOME` défini, plateforme 35 installée).

    ./gradlew assembleDebug

### Option 3 : GitHub Actions (rien à installer)
1. Créer un dépôt GitHub et y pousser ce dossier.
2. Onglet **Actions** > workflow « Build APK » > une fois terminé, télécharger
   l'artefact `pile-ou-face-apk`.

## Installer sur le téléphone
Copier `app-debug.apk` sur le téléphone, l'ouvrir, et autoriser l'installation
depuis cette source si Android le demande. L'APK « debug » est signé
automatiquement avec une clé de débogage, ce qui suffit pour une installation
personnelle.

## Où modifier quoi
- `CoinView.java` : dessin de la pièce et animation (durée, nombre de tours).
- `MainActivity.java` : écran, textes, réaction au toucher.
- minSdk 26 (Android 8.0), targetSdk 35, aucune dépendance externe.
