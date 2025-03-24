import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import areTokensValid from "../../lib/tokens";
import { User } from "../../types";
import FormModel from "../../components/baseModels/FormModel";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export default function ProfilePage() {
  const navigate = useNavigate();
  let [userName, setUserName] = useState("");
  //   let [userId, setUserId] = useState("");
  let [hidePopUp, setHidePopUp] = useState(true);
  let [popUpMessage, setPopUpMessage] = useState("");
  let [isWaitingForAccountDelete, setIsWaitingForAccountDelete] =
    useState(false);
  const modelId = "delete-acccount-model-id";
  const statusMessages: { [index: number]: string } = {
    400: "Bad Request",
    401: "Unauthorized",
    403: "Forbidden",
    404: "Not Found",
    500: "Internal Server Error",
  };

  function signOut() {
    localStorage.removeItem("token");
    localStorage.removeItem("refresh_token");

    navigate("/");
  }

  function showPopUp(message: string) {
    setPopUpMessage(message);
    setHidePopUp(false);

    setTimeout(() => {
      setPopUpMessage("");
      setHidePopUp(true);
    }, 5000);
  }

  function showModelError(message: string) {
    console.error(message);
    showPopUp(message);
    setIsWaitingForAccountDelete((prev) => !prev);
  }

  async function deleteAccount() {
    setIsWaitingForAccountDelete((prev) => !prev);

    try {
      const response = await fetch(`${BACKEND_URL}/api/user`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        let message = await response.text();
        if (!message || message.length < 1) {
          message = "Failed. Reason: " + statusMessages[response.status];
        }

        showModelError(message);
        return;
      }

      signOut();
    } catch (error: any) {
      showModelError("Error: " + error.message);
    }
  }

  // fetch user info and dislay it
  useEffect(() => {
    async function fetchUserInfo() {
      if (!(await areTokensValid())) {
        navigate("/login");
      }

      try {
        const response = await fetch(`${BACKEND_URL}/api/user`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        if (!response.ok) {
          navigate("/login");
        }

        const user: User = await response.json();
        // setUserId(user.id);
        setUserName(user.username);
      } catch (error: any) {
        console.error("Error: ", error.message);
      }
    }
    fetchUserInfo();
  }, []);
  return (
    <div className="container mx-auto bg-base-200 rounded-b-lg p-2 flex flex-col items-center gap-6">
      <h2 className="text-5xl font-bold">Profile Page</h2>
      <h3 className="text-4xl font-bold">Username: {userName}</h3>
      <button
        className="btn btn-error text-2xl"
        onClick={() =>
          (document.getElementById(modelId) as HTMLDialogElement).showModal()
        }
      >
        Delete Account
      </button>
      <FormModel id={modelId} hidePopUp={hidePopUp} popUpMessage={popUpMessage}>
        <div className="flex flex-col py-2 items-center gap-2">
          <h4 className="text-xl font-bold">
            Are you sure you want to delete your account?
          </h4>
          <button
            className="btn btn-error text-lg w-42"
            onClick={deleteAccount}
            disabled={isWaitingForAccountDelete}
          >
            Delete Account
          </button>
        </div>
      </FormModel>
    </div>
  );
}
