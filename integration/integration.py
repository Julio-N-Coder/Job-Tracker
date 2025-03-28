import requests
import time
import subprocess
import os
import sys


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


def cleanup():
    print("cleanup not set up")


BACKEND_APP_URL = "http://localhost:8080"
FILE_DIR = os.path.abspath(os.path.dirname(__file__))
PROJECT_DIR = os.path.dirname(FILE_DIR)
FRONT_END_DIR = os.path.join(PROJECT_DIR, "FrontEnd")

CONTAINER_DB_NAME = "postgres-db-test-90134"
DOCKER_NETWORK_NAME = "postgres-db-test-network-q41348"

POSTGRES_DB = "mydatabase"
POSTGRES_PASSWORD = "secret"
POSTGRES_USER = "myuser"

FRONT_END_PID = None
SPRING_BOOT_PID = None

processes = {}


def main():
    global FRONT_END_PID
    global SPRING_BOOT_PID

    def exit_with_error(error_message: str):
        print(error_message, file=sys.stderr)
        sys.exit(1)

    # create docker network
    network_res = subprocess.run(
        ["docker", "network", "create", "--driver", "bridge", DOCKER_NETWORK_NAME],
        text=True,
    )

    if network_res.returncode != 0:
        exit_with_error("Failed to create docker network")

    # Start postgresql container
    db_res = subprocess.run(
        [
            "docker",
            "run",
            "--name",
            CONTAINER_DB_NAME,
            "--publish",
            "5432:5432",
            "--network",
            DOCKER_NETWORK_NAME,
            "-e",
            f"POSTGRES_DB={POSTGRES_DB}",
            "-e",
            f"POSTGRES_PASSWORD={POSTGRES_PASSWORD}",
            "-e",
            f"POSTGRES_USER={POSTGRES_USER}",
            "-d",
            "postgres:latest",
        ],
        text=True,
    )

    if db_res.returncode != 0:
        exit_with_error("Failed to create postgres container")

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

    # Wait for application to be ready
    if wait_for_spring_boot_app(BACKEND_APP_URL):
        run_selenium_tests()
    else:
        print("Could not start Selenium tests - application not ready")

    # save background task to hashmap and use that to stop resources
    cleanup()


if __name__ == "__main__":
    main()
