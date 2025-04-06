# ASHA Sakhi Chat 🤖

## Demo Video

[![ASHA Sakhi Chat Demo](https://img.youtube.com/vi/Lpq2BTU-o5M/0.jpg)](https://youtu.be/Lpq2BTU-o5M)

<video width="560" height="315" controls>
  <source src="https://github.com/littleb01s/asha-sakhi-chat/assets/123456789/Asha_sakhi_demo.mp4" type="video/mp4">
  Your browser does not support the video tag.
</video>

## Overview

ASHA Sakhi Chat is an innovative mobile application designed to empower ASHA (Accredited Social Health Activist) workers in India with AI-powered assistance for maternal healthcare. The app runs entirely on-device, making it perfect for areas with limited internet connectivity.

## 🌟 Key Features

- **Offline-First Architecture**: Works without internet connectivity using on-device LLM
- **Multilingual Support**: Communicates in local languages for better accessibility
- **Quick Risk Analysis**: Rapid assessment of pregnancy-related risks
- **Personalized Profiles**: Track and manage patient data efficiently
- **SMS Integration**: Send reminders and alerts to patients without smartphones
- **Low Resource Requirements**: Optimized for basic Android devices

## 🏗️ Technical Architecture

- **On-Device LLM**: Uses MediaPipe to run Gemma 2B model locally
- **TensorFlow Lite**: Powers efficient model inference on mobile devices
- **Material Design 3**: Modern, accessible UI components
- **Kotlin & Jetpack Compose**: Modern Android development stack
- **MVVM Architecture**: Clean separation of concerns

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Android device with minimum 2GB RAM
- Basic understanding of Android development

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/asha-sakhi-chat.git
   ```

2. Download the Gemma model:
   - Visit [Kaggle](https://www.kaggle.com/models/google/gemma)
   - Sign up and accept the Gemma Terms & Conditions
   - Download the `gemma-2b-it-cpu` version from the TensorFlow Lite tab

3. Import the model:
   - Use Device Explorer to copy the model file to:
   - Path: `/data/local/tmp/llm/gemma-2b-it-cpu-int4.bin`

4. Build and run:
   - Open the project in Android Studio
   - Connect your device
   - Click Run

## 💡 Project Motivation

Our team was inspired by the challenges faced by ASHA workers in rural India. Having experienced the critical nature of timely medical assistance firsthand, we understand the importance of reliable healthcare support. ASHA workers, despite being crucial to community health, often face technical barriers like poor connectivity and limited resources.

This project aims to bridge these gaps by:
- Providing instant AI assistance without internet dependency
- Supporting multiple local languages
- Working on basic Android devices
- Integrating with existing healthcare workflows

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Google MediaPipe team for their excellent on-device ML tools
- The ASHA worker community for their invaluable feedback
- All contributors and supporters of this project

---

<div align="center">
  Made with ❤️ by Team Little B01S
</div>

