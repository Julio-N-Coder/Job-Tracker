import requests
import time
import subprocess
import os
import sys

BACKEND_APP_URL = "http://localhost:8080"
FRONT_END_URL_FROM_CONTAINER = "http://host.docker.internal:4173"
FILE_DIR = os.path.abspath(os.path.dirname(__file__))
PROJECT_DIR = os.path.dirname(FILE_DIR)
FRONT_END_DIR = os.path.join(PROJECT_DIR, "FrontEnd")

POSTGRES_DB = "mydatabase"
POSTGRES_PASSWORD = "secret"
POSTGRES_USER = "myuser"

FRONT_END_PID = None
SPRING_BOOT_PID = None

processes = {}


def wait_for_spring_boot_app(url, timeout=60, check_interval=2):
    """
    Wait for the Spring Boot application to be fully ready.

    :param url: Health check endpoint URL
    :param timeout: Maximum time to wait (in seconds)
    :param check_interval: Time between checks (in seconds)
    :return: True if app is ready, False if timeout occurs
    """
    start_time = time.time()

    while time.time() - start_time < timeout:
        try:
            response = requests.get(f"{url}/api/actuator/health")
            if response.status_code == 200:
                health_data = response.json()

                # Check if the application is UP
                if health_data.get("status") == "UP":
                    print("Spring Boot application is ready!")
                    return True

            print("Waiting for application to be ready...")
            time.sleep(check_interval)

        except requests.ConnectionError:
            print("Connection error. Retrying...")
            time.sleep(check_interval)

    print("Timeout: Application did not become ready")
    return False


def run_selenium_tests():
    """
    Function to run Selenium tests after app is ready
    """
    print("make selenium test here")


def run_compose_file(action: str, *extra) -> int:
    cmd = [
        "docker",
        "compose",
        "--file",
        os.path.join(FILE_DIR, "compose.yaml"),
        action,
    ]
    cmd.extend(extra)

    return subprocess.run(cmd).returncode


def cleanup():
    print("Cleaning up resources")
    # Shut down processes
    processes[FRONT_END_PID].terminate()
    processes[SPRING_BOOT_PID].terminate()

    run_compose_file("stop")
    # run_compose_file("down", "--volumes")


def main():
    global FRONT_END_PID
    global SPRING_BOOT_PID

    def exit_with_error(error_message: str):
        print(error_message, file=sys.stderr)
        sys.exit(1)

    # start docker compose file
    composeStatus = run_compose_file("up", "--detach")
    if composeStatus != 0:
        exit_with_error("Failed to start docker compose file")

    # build and Start front end app
    frontEndBuildResult = subprocess.run(
        ["npm", "--prefix", FRONT_END_DIR, "run", "build-local"], text=True
    )

    if frontEndBuildResult.returncode != 0:
        exit_with_error("Error: Frontend build failed.")

    front_end_proc = subprocess.Popen(
        ["npm", "--prefix", FRONT_END_DIR, "run", "preview"],
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    FRONT_END_PID = front_end_proc.pid
    processes[FRONT_END_PID] = front_end_proc

    mvn = os.path.join(PROJECT_DIR, "mvnw")
    # build sprint boot application
    springBuildResult = subprocess.run(
        [mvn, "clean", "package", "-DskipTests"], text=True
    )

    if springBuildResult.returncode != 0:
        exit_with_error("Error: Sprint Boot build failed.")

    # Start Spring Boot application
    JAR_FILE = os.path.join(PROJECT_DIR, "target", "JobTracker.jar")
    DB_URL = f"jdbc:postgresql://localhost:5432/{POSTGRES_DB}"

    spring_proc = subprocess.Popen(
        [
            "java",
            "-jar",
            JAR_FILE,
            "--key.file.path=keys",
            f"--spring.datasource.url={DB_URL}",
            f"--spring.datasource.username={POSTGRES_USER}",
            f"--spring.datasource.password={POSTGRES_PASSWORD}",
        ],
        text=True,
    )
    SPRING_BOOT_PID = spring_proc.pid
    processes[SPRING_BOOT_PID] = spring_proc

    # Wait for application to be ready, then run test
    if wait_for_spring_boot_app(BACKEND_APP_URL):
        run_selenium_tests()
    else:
        print("Could not start Selenium tests - application not ready")

    cleanup()


if __name__ == "__main__":
    main()
