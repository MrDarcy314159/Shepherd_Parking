# Shepherd Parking Android App
![image (2)](https://github.com/user-attachments/assets/8bb5a834-7ed7-4f28-88ca-065ee172d139)

## Overview
Shepherd Parking is an innovative Android application designed to address the parking challenges at Varsity College Sandton. It provides real-time parking information and various features to help students and staff make informed decisions about parking and commuting to campus. 
### The primary goals of the application are:
1. To provide real-time parking availability information to students and staff.
2. To reduce stress and time wasted in searching for parking spots.
3. To improve overall campus traffic flow and parking efficiency.
4. To enable users to make informed decisions about their commute before leaving home.

By offering features such as real-time vehicle tracking, parking projections, and traffic updates, Shepherd Parking aims to streamline the parking process and enhance the overall campus experience.

### Design Considerations
Several key design considerations were taken into account during the development of Shepherd Parking:

User-Centric Approach:

Intuitive interface for easy navigation and quick access to critical information.
Personalized features like check-in systems and tardiness notifications to cater to individual user needs.


Real-Time Data Processing:

Implementation of efficient data synchronization to provide up-to-date parking information.
Use of Firebase Realtime Database for instant updates across all user devices.


Scalability:

Modular architecture to allow for easy addition of new features or expansion to other campuses.
Use of cloud-based services to handle increasing user loads.


Security and Privacy:

Implementation of secure authentication methods, including Google Sign-In.
Careful handling of user data in compliance with privacy regulations.


Cross-Functional Integration:

Seamless integration with Google Maps for location-based services.
Incorporation of push notification systems for timely updates.


Offline Functionality:

Basic app functionality maintained even in low-connectivity situations.


Accessibility:

Design considerations for users with disabilities, including screen reader compatibility.


Performance Optimization:

Efficient coding practices to ensure smooth operation on a wide range of Android devices.


## Key Features

### 1. Real-Time Vehicle Tracking
- Monitors the number of vehicles entering and leaving the campus in real-time.
- Provides accurate and up-to-date information on parking capacity.

### 2. Push Notifications System
- Sends push notifications to users about parking capacity based on their selected time periods.
- Offers suggestions for sending emails to select lecturers if a user will be late
- Utilizes a custom API to send push notifications based on specific parking metrics.

### 3. User Authentication
- Provides a secure sign-up and login system for users.
- Offers Google Authentication for quick and easy access.

### 4. Parking Projection System
- Allows users to opt-in for parking the day before.
- Calculates and displays projected capacity percentages based on available spots and opt-ins.

### 5. Traffic Feed System
- Enables users to post updates about parking or traffic conditions near campus.
- Displays user-generated updates on the 'Traffic Feedback' page with location and optional messages.

### 6. Automated Tardiness Notification System
- Allows users to report if they will be late for a lecture.
- Users can select the module, reason for lateness, and additional information.
- Automatically compiles and sends an email to the respective lecturer.

### 7. Check-In System
- Allows users to register their parking for the next day.
- Includes a form for user details and time selection.
- Displays a confirmation message with a streak counter for regular usage.

### 8. Analytics Dashboard
- Shows projected parking capacity for the next day (morning and afternoon).
- Displays a weekly parking usage chart.

### 9. Guard House Interface
- Allows administrators or security guards to manually track cars entering and leaving campus.
- Provides real-time updates to the parking availability information.

### 10. Map Updates
- Displays an aerial view of the campus with location markers indicating areas of traffic or parking updates.

### 11. Settings Management
- Allows users to update their student information.
- Manages biometric authentication settings (facial recognition and fingerprint).
- Controls push notifications and location services settings.
- Offers language selection options.

## Technical Stack
- Language: Kotlin
- UI Framework: Jetpack Compose
- Architecture: MVVM (Model-View-ViewModel)
- Backend: Firebase (Auth, Realtime Database, Firestore, Cloud Messaging)
- Maps: Google Maps SDK
- Networking: Retrofit
- Authentication: Firebase Auth, Google Sign-In
- Custom API: For push notifications based on parking metrics

## Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.5.0 or later
- Minimum SDK: 24
- Target SDK: 34

## Setup and Installation
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Configure Firebase:
   - Create a new Firebase project
   - Add your Android app to the Firebase project
   - Download the `google-services.json` file and place it in the `app` directory
   - Enable Google Sign-In in the Firebase Console
5. Configure Google Maps:
   - Obtain a Google Maps API key
   - Add the API key to your `local.properties` file: `MAPS_API_KEY=your_api_key_here`
6. Configure the custom push notification API:
7. Build and run the application

## Project Structure
- `MainActivity`: Entry point of the application, sets up navigation
- `SignUpPage`: Handles user registration
- `LoginPage`: Manages user authentication, including Google Sign-In
- `HomePage`: Main dashboard of the app
- `CheckInPage`: Handles user check-ins for future parking
- `TrafficFeedbackPage`: Allows users to provide and view traffic updates
- `Late_Page`: Manages late notifications to lecturers
- `AnalyticsPage`: Displays parking analytics and projections
- `SettingsPage`: User settings and preferences management
- `MapUpdatesPage`: Provides map-related updates for traffic and parking
- `GuardHousePage`: Interface for guard house operations and manual car counting
- `UserManager`: Manages user-related operations and data

## Key Components
- Navigation: Uses Jetpack Navigation Compose
- UI: Material 3 components with custom theming
- State Management: Utilizes Compose state and ViewModel
- Background Services: Firebase Cloud Messaging for notifications
- Location Services: Google Play services location API
- Data Persistence: Firebase Realtime Database and Firestore
- Authentication: Firebase Authentication with Google Sign-In integration

## Utilization of GitHub and GitHub Actions
GitHub plays a crucial role in the development and maintenance of Shepherd Parking:

#### Version Control:

Utilization of Git for tracking changes in the source code.
Branch management for feature development, bug fixes, and releases.


#### Collaboration:

Use of Pull Requests for code reviews and team collaboration.
Issue tracking for bug reports, feature requests, and project management.


#### Documentation:

Hosting of project documentation, including this README, directly in the repository.
Use of Wiki for more extensive documentation and guides.


#### GitHub Actions:

Automated CI/CD pipeline for building, testing, and deploying the app.
Workflow examples:
a. Automatically building the app on each push to the main branch.
b. Running unit and integration tests before merging pull requests.
c. Generating and uploading APK artifacts for easy testing.
d. Automated deployment to Google Play Store for beta and production releases.

![image (1)](https://github.com/user-attachments/assets/be2f00ee-aa88-4536-85de-31c2a9632582)



#### Security:

Regular automated security scans using GitHub's dependency graph and Dependabot alerts.
Secret management for storing sensitive information like API keys securely.


#### Project Management:

Use of GitHub Projects for Agile development, including sprint planning and tracking.
Milestones for tracking progress towards major releases.



By leveraging these GitHub features and GitHub Actions, we ensure a streamlined development process, maintain high code quality, and facilitate efficient collaboration among team members.
## API Design
The app uses two APIs:

1. Custom API for location and traffic feedback:
   - Handles user message submission
   - Stores and retrieves data in JSON format
   - Integrates with Google Maps for marker placement

   Endpoints:
   - POST: To submit location data and associated messages
   - GET: To retrieve stored location data and messages

2. Custom Push Notification API:
   - Sends notifications based on specific parking metrics
   - Integrates with Firebase Cloud Messaging for delivery

## Authentication Flow
1. Users can sign up using email and password or Google Authentication.
2. For email/password sign-up:
   - Users provide necessary details (name, email, password, etc.)
   - Data is validated and stored securely in Firebase Authentication
3. For Google Sign-In:
   - Users select the Google account they want to use
   - Firebase Authentication handles the OAuth flow
4. After successful authentication, users are directed to the home page

## Data Types
The app manages various data types, including:
1. User Information (UserID, Name, Email, etc.)
2. Parking Information (Status, Projected Capacity)
3. Check-In Data
4. Late Notice Data
5. Traffic Feedback
6. Lecturer Details
7. Analytics Data
8. Guard House Data

## Security and Privacy
- Implements secure authentication methods, including Google Sign-In
- Ensures data privacy compliance
- Uses encryption for sensitive data storage and transmission
- Securely manages API keys and sensitive configuration data

## Future Enhancements
- Integration with external traffic APIs
- Expansion of analytics features
- Enhanced gamification elements for user engagement

## Contributing
1. Fork the repository
2. Create a new branch for your feature
3. Commit your changes
4. Push to your branch
5. Create a new Pull Request

![image](https://github.com/user-attachments/assets/91890523-4ad3-4671-a97e-35265a648a4a)


