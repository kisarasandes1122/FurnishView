# FurnishView

![FurnishView Logo](docs/images/logo.png)

> An advanced interior design application for 3D furniture placement and room visualization

## 📋 Overview

FurnishView is a Java desktop application that enables users to design and visualize interior spaces in both 2D and 3D views. With an intuitive interface and robust feature set, the application allows for the creation of various room shapes, furniture placement, and price estimation.

## ✨ Features

- **Multi-shape Room Design**: Create rectangular, circular, L-shaped, or T-shaped rooms with custom dimensions
- **3D Visualization**: View your designs in both 2D top-down and 3D perspective modes
- **Furniture Library**: Choose from a variety of furniture types with customizable dimensions
- **Interactive Furniture Placement**: Drag and drop furniture with mouse or keyboard controls
- **Material Customization**: Set colors and textures for both rooms and furniture
- **Price Estimation**: Get real-time cost breakdowns of your design
- **Project Management**: Save, load, and organize your design projects
- **User Authentication**: Secure login and account management
- **Admin Features**: Manage inventory prices and access all projects

## 🖼️ Screenshots

### Login Screen
![Login](https://github.com/user-attachments/assets/48c4e2be-ca97-4217-92a2-c30f7952e9bc)


### Main Design Interface
![Main Designing Frame](https://github.com/user-attachments/assets/42c6c441-9f5b-4fd5-acd9-f05ead028742)


### Project Dashboard
![Dashboard](https://github.com/user-attachments/assets/1967f577-fca8-4bf7-9826-9f3e6f5ec87f)


## 🛠️ Technology Stack

- **Java**: Core programming language
- **Swing**: GUI components
- **JOGL (Java OpenGL)**: 3D rendering
- **Serialization**: Object persistence
- **Custom Undo/Redo Framework**: Action history management

## 📦 Requirements

- Java Runtime Environment (JRE) 8 or newer
- OpenGL-compatible graphics hardware
- At least 4GB RAM
- 500MB free disk space

## 🚀 Installation

1. Download the latest release from the [Releases](https://github.com/kisarasandes1122/furnishview/releases) page
2. Extract the ZIP file to your preferred location
3. Run the application using:
   ```
   java -jar FurnishView.jar
   ```

## 👨‍💻 Development Setup

To set up the development environment:

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/furnishview.git
   ```

2. Import the project into your IDE (Eclipse, IntelliJ IDEA, etc.)

3. Add required libraries to your classpath:
   - JOGL (Java OpenGL) - version 2.3.2 or newer
   - JUnit (for tests)

4. Build the project according to your IDE's procedures

## 📝 Usage Guide

### Getting Started

1. **Login or Register**: Use the credentials `designer/password` or register a new account
2. **Create a New Project**: From the dashboard, click "Create New Project"
3. **Room Setup**: Choose a room shape and set dimensions
4. **Add Furniture**: Select furniture from the library and add to your design
5. **Manipulate Furniture**: Use mouse or keyboard controls to position items
6. **Change View Mode**: Toggle between 2D and 3D views using the "View Mode" dropdown
7. **Save Your Design**: Use File > Save Design to save your progress

### Controls

- **Left Mouse Button**: Select and drag furniture
- **Right Mouse Button**: Rotate camera (in 3D mode)
- **Mouse Wheel**: Zoom in/out
- **Arrow Keys**: Move selected furniture
- **Q/E Keys**: Rotate selected furniture
- **Delete Key**: Remove selected furniture

## 🔧 Project Structure

```
furnishview/
├── src/                  # Source code
│   ├── ui/               # User interface components
│   ├── model/            # Data models
│   ├── rendering/        # 3D rendering
│   ├── util/             # Utility classes
│   └── managers/         # Business logic managers
├── resources/            # Application resources
│   └── images/           # Icons and images
├── designs/              # Project saves directory
├── lib/                  # Libraries
└── docs/                 # Documentation

