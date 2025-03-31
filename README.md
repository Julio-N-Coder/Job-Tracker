# Job Tracker

### Welcome! This Project is a website where you are able to write down and update the status of the jobs you have applied to. This project was made with Spring Boot for the backend and React for the front end.

## Features

- Add new job applications with details like company name, position, and application date.
- Update the status of job applications (e.g., applied, interviewing, offered, rejected).
- Able to make multiple accounts to manage different jobs
- Responsive design for seamless use on desktop and mobile devices.

## Technologies Used

- **Backend**: Spring Boot
- **Frontend**: React
- **Database**: PostgreSQL
- **Styling**: Tailwind/CSS

## Prerequisites

Before setting up the project, ensure you have the following installed on your system:

- **Java 21**: Required to run the Spring Boot backend.
- **Node.js**: Required to run the React frontend.
- **npm**: Comes with Node.js, used for managing frontend dependencies.
- **Docker**: Optional, but required if running the project for development.
- **OpenSSL**: Only Required when generate the key pair for JWT authentication.
- **Maven**: Used to build and run the Spring Boot backend.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/job-tracker.git
   ```
2. Navigate to the project directory:
   ```bash
   cd job-tracker
   ```
3. Set up the backend:

   First off, you need to generate a key pair that the backend needs to be able make Json Web Tokens. In the root of the project, their is a keys directory which is where they keys need to be stored. The keys also need to be named private.pem for the private key and public.pem for the public key. You can run commands below in the root of the project to generate the private and public keys.

   ```bash
   openssl genpkey -algorithm ED25519 -out keys/private.pem
   openssl pkey -in keys/private.pem -pubout -out keys/public.pem
   ```

   To run the backend of the project in development mode, run the command below in the root of the project

   ```bash
   ./mvnw spring-boot:run
   ```

   For developing the backend, I have a nice pgadmin4 container that runs the web version of pgadmin4 on `http://localhost:5050`. This automatically starts up along with a postgres container when running the development version of this project as shown in the **compose.yaml** file

4. Set up the frontend:
   - Navigate to the `FrontEnd` folder.
   - Install dependencies:
     ```bash
     npm install
     ```
   - Start the React development server:
     ```bash
     npm run dev
     ```

## Usage

1. For the development version, open the application in your browser at `http://localhost:3000`.
2. Add, update, and manage your job applications.

## Building and Using

If you would like to build and use the project, follow the steps below and choose options that best suit your use case

### FrontEnd

For the front end, you have a few different to build the front end depending on how the frontend accesses the backend.

- Front end and backend running the same host
  ```bash
  npm run build-local
  ```

To specify a custom backend url for the front end to use, make a **.env.production.local** file in the root of the **FrontEnd** directory. The options are the same as the current **.env** file, for example `VITE_BACKEND_URL=http://localhost:8080`

After building the front end, you can either server the contents of the **dist** folder from a web server or run the following command to serve the contents of that directory. The preview url is `http://localhost:4173`

```bash
npm run preview
```

### Spring Boot Backend

For the backend, I suggest making a **application-prod.properties** file in the **src/main/resources** directory with your data in it and follow the steps below

1. In the **application-prod.properties**, add the following content with your own data for them
   ```
   key.file.path=<path to keys directory relative to jar file>
   spring.datasource.url=jdbc:postgresql://<domain or ip>:<port>/<database name>
   spring.datasource.username=<myuser>
   spring.datasource.password=<password>
   ```
2. Package the spring boot project
   ```bash
   ./mvnw clean package
   ```
3. Run the jar file with the prod profile
   ```bash
   java -jar JobTracker.jar --spring.profiles.active=prod
   ```

## Running Test

1. Running all backend test
   ```bash
   ./mvnw test
   ```
1. Running integration test
   ```bash
   python3 integration/integration-test.py
   ```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.
