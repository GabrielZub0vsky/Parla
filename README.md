# Parla

A command-line vocabulary learning tool that helps foreign language learners track, study, and recall new words through intelligent quizzing.

## Overview

Parla is a personal vocabulary management system designed for language learners. It lets you record foreign words with their English translations and test yourself with smart quizzes that focus on the words you struggle with most. The app learns from your lookup patterns to identify which words need more practice.

## Features

- **Add Words**: Record foreign words with their English translations, organized by language
- **Instant Translation**: Look up translations of foreign words on demand
- **Smart Quizzing**: Get quizzed on words based on lookup frequency (harder words get more practice)
- **Rare Word Testing**: Includes ~10% of rarely-looked-up words in quizzes to ensure you truly know them
- **Progress Tracking**: Monitors how often you look up each word and tracks your quiz scores
- **Data Persistence**: All words, lookup counts, and quiz scores are automatically saved to a file

## Installation

### Requirements
- Java 8 or higher
- A terminal/command line interface

### Steps

1. Clone or download the Parla repository
2. Navigate to the project directory
3. Compile the Java files:
```bash
   javac Main.java Words.java
```
4. Run the program:
```bash
   java Main
```

## Usage

Once the program starts, you'll see a prompt (`>`). Enter commands to interact with Parla:

### Commands

#### `add`
Add new words to your vocabulary database. You'll be prompted to enter words in this format:
<language> <foreign_word> <english_translation>

#### `translate <foreign_word>`
Look up the English translation of a foreign word. The app tracks lookups to identify which words you struggle with.

#### `quiz <number_of_questions>`
Take a quiz on your vocabulary. The app selects words intelligently:
- **90%** of questions come from frequently-looked-up words (these are harder for you)
- **10%** of questions come from rarely-looked-up words (to ensure you truly know them)
- If you answer incorrectly, that word is marked as looked-up, so it'll appear more in future quizzes

#### `help`
Display all available commands.

#### `exit`
Save your progress and exit.

### Smart Quiz Selection

Parla uses a threshold-based algorithm to select quiz words:

1. **Calculate a difficulty threshold**: The highest lookup count among all words is divided by 3
2. **Separate frequent from rare words**: Words above the threshold are "frequent" (harder), below are "rare" (easier)
3. **Select proportionally**: 
   - 90% of quiz questions are frequent words (sorted by lookup count, highest first)
   - 10% are rare words (selected randomly)
4. **Learn from mistakes**: If you answer incorrectly, that word's lookup count increases, making it appear more often in future quizzes

## File Structure

parla/
├── Main.java          # Command-line interface
├── Words.java         # Core vocabulary and quiz logic
├── parla_data.txt     # Data storage (created on first exit)
└── README.md          # This file


## Roadmap

As this project is designed for me to learn and gain experience with multiple new technologies, here is my step-by-step build plan:

- **Phase 1**: ✅ Command-line tool with file storage (current)
- **Phase 2**: SQL database backend
- **Phase 3**: Web application (HTML/CSS frontend, servlet backend)
- **Phase 4**: Modern web API (Spring Boot backend, React frontend)
- **Phase 5**: Mobile app (React Native frontend)
- **Phase 6**: Multi-user support with authentication
- **Phase 7**: Python backend rewrite
- **Phase 8**: Node.js backend rewrite

Any and all feedback would be greatly appreciated; please email ghzubovsky@dons.usfca.edu.
