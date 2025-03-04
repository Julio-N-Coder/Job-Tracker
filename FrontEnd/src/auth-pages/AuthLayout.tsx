import { NavLink } from "react-router";
import { useNavigate } from "react-router";
import { FormEvent, ChangeEvent, useState } from "react";

export default function AuthLayout({ type }: { type: string }) {
  let navigation = useNavigate();
  const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
  let [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  let [isSubmiting, setIsSubmiting] = useState(false);

  function handleChangeEvent(event: ChangeEvent<HTMLInputElement>) {
    const type = event.currentTarget.id;
    const value = event.currentTarget.value;

    setFormData({
      ...formData,
      [type]: value,
    });
  }

  function validateData(): boolean {
    const username = formData.username;
    const password = formData.password;
    if (!username || username.length < 3 || username.length > 30) {
      console.error("Username not Valid");
      toggleSubmitState();
      return false;
    }
    if (!password || password.length < 2 || password.length > 100) {
      console.error("Password not Valid");
      toggleSubmitState();
      return false;
    }

    return true;
  }

  function toggleSubmitState() {
    setIsSubmiting((prevState) => !prevState);
    document.getElementById("authPageLink")?.classList.toggle("btn-disabled");
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    toggleSubmitState();

    if (!validateData()) return;

    let response: Response;
    try {
      response = await fetch(`${BACKEND_URL}/api/auth/${type}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        console.error("Failed. Reason: ", await response.text());
        toggleSubmitState();
        return;
      }

      localStorage.token = await response.text();
      console.log(localStorage.token);

      navigation("/jobs-page");
    } catch (e: any) {
      console.log("Error: ", e.message);
      toggleSubmitState();
    }
  }

  return (
    <div className="flex flex-grow flex-col gap-8 justify-center items-center relative">
      <div className="bg-base-200 p-4 rounded-2xl flex flex-col items-center gap-4">
        <form onSubmit={handleSubmit} className="flex flex-col items-center">
          <fieldset>
            <div>
              <label htmlFor="username">
                <legend className="fieldset-legend text-lg">Username</legend>
              </label>
              <label className="input validator w-96" id="username-label">
                <UserSvg />
                <input
                  type="input"
                  id="username"
                  required
                  minLength={3}
                  maxLength={30}
                  placeholder="Username"
                  onChange={handleChangeEvent}
                />
              </label>
              <div className="validator-hint">Must be 3 to 30 characters</div>
            </div>
            <div>
              <label htmlFor="password">
                <legend className="fieldset-legend text-lg">Password</legend>
              </label>
              {/* password input */}
              <label className="input validator w-96" id="password-label">
                <KeySvg />
                <input
                  type="password"
                  id="password"
                  placeholder="Password"
                  required
                  minLength={2}
                  maxLength={100}
                  onChange={handleChangeEvent}
                />
              </label>
              <div className="validator-hint">Must be 2 to 100 characters</div>
            </div>
          </fieldset>
          <button
            type="submit"
            id="submit-button"
            className="btn btn-accent mt-2 text-lg"
            disabled={isSubmiting}
          >
            {isSubmiting ? (
              <>
                <span className="loading loading-spinner"></span>
                <span>Submitting</span>
              </>
            ) : (
              "Submit"
            )}
          </button>
        </form>
        <div className="flex flex-col">
          {type === "signup" ? (
            <p>Have an Account?</p>
          ) : (
            <p>Don't have an account?</p>
          )}
          <NavLink
            id="authPageLink"
            to={type == "signup" ? "/login" : "/signup"}
            className="btn btn-accent text-xl"
          >
            {type == "signup" ? "Login" : "SignUp"}
          </NavLink>
        </div>
      </div>
    </div>
  );
}

function KeySvg() {
  return (
    <svg
      className="h-[1em] opacity-50"
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
    >
      <g
        strokeLinejoin="round"
        strokeLinecap="round"
        strokeWidth="2.5"
        fill="none"
        stroke="currentColor"
      >
        <path d="M2.586 17.414A2 2 0 0 0 2 18.828V21a1 1 0 0 0 1 1h3a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h1a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h.172a2 2 0 0 0 1.414-.586l.814-.814a6.5 6.5 0 1 0-4-4z"></path>
        <circle cx="16.5" cy="7.5" r=".5" fill="currentColor"></circle>
      </g>
    </svg>
  );
}

function UserSvg() {
  return (
    <svg
      className="h-[1em] opacity-50"
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
    >
      <g
        strokeLinejoin="round"
        strokeLinecap="round"
        strokeWidth="2.5"
        fill="none"
        stroke="currentColor"
      >
        <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </g>
    </svg>
  );
}
