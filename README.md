# ReadEasy

ReadEasy is an offline Android app designed to assist users with visual, cognitive, or reading challenges by making text more accessible. It uses on-device OCR and supports features like summarization, translation, and saving important text for later reading.

## Features

- **Image-to-Text (OCR)**: Extract text from images using Google ML Kit's Text Recognition v2.
- **Offline Summarization**: Summarize large blocks of extracted text without internet access.
- **Language Translation**: Translate the extracted or summarized text into a preferred language.
- **Auto Layout / Large Text Mode**: Display extracted text in a readable, user-friendly format.
- **Reading History / Save for Later**: Save important extracted texts and access them anytime.
- **Manage Saved Texts**: Open or delete previously saved entries.

## Getting Started

### Prerequisites

- Android device running Android 6.0 (API level 23) or higher
- Camera access for capturing images
- Storage permission for saving extracted text

### Installation

1. Download the latest APK from the releases section
2. Enable "Install from Unknown Sources" in your device settings
3. Install the APK file
4. Grant necessary permissions when prompted

## Usage

1. **Capture Text**:
   - Open the app and tap the camera button
   - Point your camera at the text you want to extract
   - Hold steady and capture the image

2. **Process Text**:
   - The app will automatically extract text from the image
   - Use the options menu to:
     - Summarize the text
     - Translate to your preferred language
     - Adjust text size and layout
     - Save for later reading

3. **Manage Saved Texts**:
   - Access your saved texts from the history section
   - Open, share, or delete saved entries as needed

## Technical Details

- Built with Kotlin
- Uses Google ML Kit for OCR
- Implements Room database for local storage
- Supports offline functionality for core features

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- Google ML Kit for OCR capabilities
- Android Jetpack components
- Room database for local storage 
