import requests
import time
import subprocess
import os
import sys
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import psycopg2

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


def connect_to_database():
    """
    Connect to the database and return the connection object.
    """
    return psycopg2.connect(
        host="localhost",
        database=POSTGRES_DB,
        user=POSTGRES_USER,
        password=POSTGRES_PASSWORD,
        port="5432",
    )


def run_selenium_tests():
    """
    Function to run Selenium tests after app is ready
    """
    try:
        connection = connect_to_database()
        cursor = connection.cursor()

        # Create screenshots directory if it doesn't exist
        screenshots_dir = os.path.join(FILE_DIR, "screenshots")
        os.makedirs(screenshots_dir, exist_ok=True)

        driver = webdriver.Remote(
            command_executor="http://localhost:4444/wd/hub",
            options=Options(),
        )

        # Go to signup page
        driver.get(f"{FRONT_END_URL_FROM_CONTAINER}/signup")
        wait = WebDriverWait(driver, 3)

        # Wait for login input to load and signin
        userInput = wait.until(EC.element_to_be_clickable((By.ID, "username")))
        userInput.send_keys("TestUser")

        password_input = driver.find_element(By.ID, "password")
        password_input.send_keys("Password")

        submitButton = driver.find_element(By.ID, "submit-button")
        submitButton.click()

        # loop through to add multiple jobs
        jobs_ammount = 4
        print(jobs_ammount, "Total Jobs")
        for job_number in range(jobs_ammount):
            wait.until(EC.element_to_be_clickable((By.ID, "add-job-button"))).click()
            companyInput = wait.until(
                EC.element_to_be_clickable((By.ID, "Add-company-input"))
            )
            companyInput.send_keys(f"CompanyName-{job_number}")

            jobTitleInput = driver.find_element(By.ID, "Add-job-title-input")
            jobTitleInput.send_keys(f"JobTitle-{job_number}")

            addSubmitButton = driver.find_element(By.ID, "Add-submit-button")
            addSubmitButton.click()

            # Wait for page reload
            time.sleep(0.25)

        # Wait for the element to be in the database
        max_retries = 5
        retry_delay = 1
        rows = None

        for attempt in range(max_retries):
            cursor.execute("SELECT * FROM jobs")
            rows = cursor.fetchall()
            if rows:
                break
            print(f"Retrying database fetch... Attempt {attempt + 1}/{max_retries}")
            time.sleep(retry_delay)

        if not rows:
            raise Exception("Failed to fetch job data from the database after retries")

        for test_number, jobData in enumerate(rows):
            test_number += 1
            jobId = jobData[0]

            jobCard = wait.until(EC.element_to_be_clickable((By.ID, jobId)))
            # Validate the job data from the database against the UI elements
            job_card_children = jobCard.find_elements(By.XPATH, "./*")
            company = job_card_children[0].text
            jobTitle = job_card_children[1].text.split(": ")[1]
            jobStatus = job_card_children[2].text.split(": ")[1]

            db_job_title = jobData[1]
            db_company = jobData[2]
            db_job_status = jobData[3]

            if company != db_company:
                raise Exception(f"Company mismatch: {company} != {db_company}")
            if jobTitle != db_job_title:
                raise Exception(f"Job title mismatch: {jobTitle} != {db_job_title}")
            if jobStatus != db_job_status:
                raise Exception(f"Job status mismatch: {jobStatus} != {db_job_status}")
            print("Job", test_number, "Passed!")

        print("Test passed Succesfully!")

        # Save a screenshot of the jobs page
        driver.save_screenshot(os.path.join(FILE_DIR, "screenshots", "jobs.png"))

    except psycopg2.DatabaseError as db_error:
        print(f"Database error occurred: {db_error}", file=sys.stderr)
    except Exception as e:
        print(f"An error occurred during Selenium tests: {e}", file=sys.stderr)
        driver.save_screenshot(os.path.join(FILE_DIR, "screenshots", "error.png"))
    finally:
        driver.quit()
        cursor.close()
        connection.close()
        print("Finished Running Test")


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
    run_compose_file("down", "--volumes")


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
        ["npm", "--prefix", FRONT_END_DIR, "run", "build-for-container"],
        text=True,
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
