import os
import sys
import subprocess
import venv
import shutil


class VirtualEnvironmentManager:
    def __init__(self, project_name="integration", python_version=None):
        """
        Initialize Virtual Environment Manager

        Args:
            project_name (str): Name of the project
            python_version (str, optional): Specific Python version to use
        """
        # Determine base directories
        self.base_dir = os.path.abspath(os.path.dirname(__file__))
        self.venv_dir = os.path.join(self.base_dir, f".venv_{project_name}")
        self.python_version = python_version

    def create_virtual_environment(self):
        """
        Create a new virtual environment

        Returns:
            bool: True if successful, False otherwise
        """
        try:
            # Check if virtual environment already exists
            if os.path.exists(self.venv_dir):
                print(f"Virtual environment already exists at {self.venv_dir}")
                return True

            # Create virtual environment
            venv.create(self.venv_dir, with_pip=True)
            print(f"Virtual environment created at {self.venv_dir}")
            return True

        except Exception as e:
            print(f"Error creating virtual environment: {e}")
            return False

    def install_requirements(self, requirements_file="requirements.txt"):
        """
        Install dependencies from requirements.txt

        Args:
            requirements_file (str): Path to requirements file

        Returns:
            bool: True if successful, False otherwise
        """
        try:
            # Construct full path to requirements file
            req_path = os.path.join(self.base_dir, requirements_file)

            # Check if requirements file exists
            if not os.path.exists(req_path):
                print(f"Requirements file not found: {req_path}")
                return False

            # Path to pip in the virtual environment
            pip_path = os.path.join(self.venv_dir, "bin", "pip")
            if sys.platform == "win32":
                pip_path = os.path.join(self.venv_dir, "Scripts", "pip")

            # Install requirements
            result = subprocess.run(
                [pip_path, "install", "-r", req_path], capture_output=True, text=True
            )

            # Check installation result
            if result.returncode == 0:
                print("Dependencies installed successfully")
                return True
            else:
                print("Error installing dependencies:")
                print(result.stderr)
                return False

        except Exception as e:
            print(f"Error installing requirements: {e}")
            return False

    def run_in_virtual_environment(
        self, script_path="integration.py", script_args=None
    ):
        """
        Run a Python script within the virtual environment

        Args:
            script_path (str): Path to the script to run
            script_args (list, optional): Arguments to pass to the script

        Returns:
            subprocess.CompletedProcess: Result of script execution
        """
        try:
            # Ensure virtual environment exists
            if not os.path.exists(self.venv_dir):
                print("Virtual environment does not exist. Creating now.")
                self.create_virtual_environment()

            # Determine Python executable path
            python_path = os.path.join(self.venv_dir, "bin", "python")
            if sys.platform == "win32":
                python_path = os.path.join(self.venv_dir, "Scripts", "python")

            # Prepare command
            cmd = [python_path, script_path]
            if script_args:
                cmd.extend(script_args)

            process_response = subprocess.run(cmd, text=True)
            return

        except Exception as e:
            print(f"Error running script in virtual environment: {e}")
            return None

    def cleanup(self):
        """
        Remove the virtual environment
        """
        try:
            if os.path.exists(self.venv_dir):
                shutil.rmtree(self.venv_dir)
                print(f"Removed virtual environment at {self.venv_dir}")
        except Exception as e:
            print(f"Error removing virtual environment: {e}")


def main():
    venv_manager = VirtualEnvironmentManager()

    try:
        venv_manager.create_virtual_environment()
        venv_manager.install_requirements()

        integration_file = os.path.join(venv_manager.base_dir, "integration.py")
        venv_manager.run_in_virtual_environment(integration_file)

    except Exception as e:
        print(f"An error occurred: {e}")

    # finally:
    # venv_manager.cleanup()


if __name__ == "__main__":
    main()
