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
            response = requests.get(f"{url}/actuator/health")
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
    # make selenium test here


def main():
    BACKEND_APP_URL = "http://localhost:8080"
    FILE_DIR = os.path.abspath(os.path.dirname(__file__))
    PROJECT_DIR = os.path.dirname(FILE_DIR)
    FRONT_END_DIR = os.path.join(PROJECT_DIR, "FrontEnd")

    # build and Start front end app
    frontEndBuildResult = subprocess.run(
        ["npm", "--prefix", FRONT_END_DIR, "run", "build-local"], text=True
    )

    if frontEndBuildResult.returncode != 0:
        print("Error: Frontend build failed.", file=sys.stderr)
        sys.exit(1)
        return

    front_end_proc = subprocess.Popen(
        ["npm", "--prefix", FRONT_END_DIR, "run", "preview"],
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )

    # Wait for application to be ready
    if wait_for_spring_boot_app(BACKEND_APP_URL):
        # Run Selenium tests
        run_selenium_tests()
    else:
        print("Could not start Selenium tests - application not ready")

    # Add cleanup function to run here to clean up resources at the end


if __name__ == "__main__":
    main()
