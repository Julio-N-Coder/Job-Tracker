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
  const modelId = "delete-acccount-model-id";

  function deleteAccount() {
    console.log("Delete account here");
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
          >
            Delete Account
          </button>
        </div>
      </FormModel>
    </div>
  );
}
