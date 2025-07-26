# ASHA Sakhi - Edge AI Copilot for Safer Motherhood! 💡

## Demo Video

[ASHA Sakhi Demo Video](https://youtu.be/rSmAmOrN0aw)

## 📱 Download APK

[Download Latest](https://drive.google.com/drive/folders/1_PtBhGqIeZM2L8LLyijDBUZj155woqno?usp=sharing)

## Overview

ASHA Sakhi Chat is an innovative mobile application designed to empower ASHA (Accredited Social Health Activist) workers in India with AI-powered assistance for maternal healthcare. The app runs entirely on-device, making it perfect for areas with limited internet connectivity.

## 🏆 Awards & Recognition

**1st Runner-Up at Pragati AI for Impact Hackathon**  
Team Little B01S secured the 1st Runner-Up position at the Pragati AI for Impact Hackathon hosted by Meta India in Gurugram, demonstrating technical excellence and social impact in maternal healthcare innovation.  
- Instagram Announcement: https://www.instagram.com/p/DMXTGPIgNnB/  
- Facebook Post: https://www.facebook.com/hack2skill/posts/introducing-team-little-b01s-the-1st-runner-up-at-the-pragati-ai-for-impact-hack/773185811877594/  
- LinkedIn Update: https://www.linkedin.com/posts/hack2skill_aiforgood-metaindia-thenudgeinstitute-activity-7352985728261263362-mbqi

## 💡 AI & Technical Features

### On-Device AI Capabilities
- **Local LLM Integration**:  
  - Gemma 3B model running locally via MediaPipe  
  - Gecko model for efficient text processing  
  - Vector database integration for semantic search

### Healthcare Intelligence
- **Risk Analysis Engine**:  
  - XGBoost-based risk assessment system  
  - Real-time pregnancy risk evaluation  
  - Personalized health recommendations  
- **Cloud-Based Diet Analysis**:  
  - Llama and LangChain-powered nutrition recommendations  
  - Personalized diet plans based on health conditions  
  - Integration with local food availability

### Language & Communication
- **Multilingual Support**:  
  - VoskAPI for speech-to-text conversion  
  - Local translation API for language conversion  
  - Support for multiple Indian languages

### Location & Mapping
- **Bhuvan Maps Integration**:  
  - Open-source mapping solution  
  - Location-based healthcare facility search  
  - Route optimization for ASHA workers

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

## 📚 Related Repositories

- [ASHA Sakhi Admin](https://github.com/ii-pewpewpew-ii/asha-sakhi-admin) – Admin dashboard for managing ASHA workers and content  
- [ASHA Sakhi Nutrition & Healthcare Scheme Recommendation](https://github.com/jayenthsk/Asha_Sakhi_Nutrition_Healthcare_Scheme_Recommendation_System) – RAG-based system for nutrition plans and healthcare scheme recommendations

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer  
- Android device with minimum 4GB RAM  
- Basic understanding of Android development

### Installation

1. Clone the repository:  
   ```bash
   git clone https://github.com/balajianbalagan/asha-sakhi-chat.git
   ```

2. Set up the required model files:

   All files should be placed in the following directory on your Android device:  
   ```
   /storage/emulated/0/Android/data/com.littleb01s.ashasakhichat/files/llm/
   ```
   Create the `llm` folder manually if it does not exist.

   Download and rename the following files:

   | File Name                   | Download URL                                                                                                  | Rename As                     |
   |-----------------------------|---------------------------------------------------------------------------------------------------------------|-------------------------------|
   | `Gecko_1024_quant.tflite`   | [Download](https://asha-sakhi-cdn.b-cdn.net/Gecko_1024_quant.tflite)                                          | `Gecko_1024_quant.tflite`     |
   | `sentencepiece.model`       | [Download](https://asha-sakhi-cdn.b-cdn.net/sentencepiece.model)                                              | `sentencepiece.model`         |
   | `asha-kb.pdf`               | [Download](https://asha-sakhi-cdn.b-cdn.net/asha-kb.pdf)                                                      | `asha-kb.pdf`                 |
   | `gemma3-1b-it-int4.task`    | [Download](https://huggingface.co/google/gemma-1.1-2b-it-tflite/blob/main/gemma-1.1-2b-it-cpu-int4.bin?raw=true) | `gemma3-1b-it-int4.task`      |
   | *Optional* Gecko Model      | [Gecko 110M on Hugging Face](https://huggingface.co/litert-community/Gecko-110m-en/tree/main)                 | As required (refer to usage)  |

   Ensure all required files are present:
   ```
   ├── Gecko_1024_quant.tflite
   ├── sentencepiece.model
   ├── asha-kb.pdf
   ├── gemma3-1b-it-int4.task
   ```

3. Build and run:
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

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Google MediaPipe team for their excellent on-device ML tools  
- The ASHA worker community for their invaluable feedback  
- All contributors and supporters of this project

  
  Made with ❤️ by Team Little B01S  


[1] https://www.youtube.com/watch?v=rSmAmOrN0aw
[2] https://drive.google.com/drive/folders/1_PtBhGqIeZM2L8LLyijDBUZj155woqno?usp=sharing
