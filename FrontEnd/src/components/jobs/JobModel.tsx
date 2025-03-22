import { ChangeEvent, FormEvent, useState } from "react";
import { useNavigate } from "react-router";
import { JobBasicData } from "../../types";
import ErrorPopup from "../ErrorPopup";
import SubmitButton from "../SubmitButton";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export default function JobModel({
  action,
  id,
  jobId,
  prevJobData,
}: {
  action: "Add" | "Update";
  id: string;
  jobId?: string;
  prevJobData?: JobBasicData;
}) {
  let navigate = useNavigate();
  const jobInputId = `${action}-job-title-input`;
  const companyInputId = `${action}-company-input`;
  const statusInputId = "status-input";
  const [jobData, setJobData] = useState({
    [jobInputId]: "",
    [companyInputId]: "",
    [statusInputId]: "",
  });
  let [hidePopUp, setHidePopUp] = useState(true);
  let [popUpMessage, setPopUpMessage] = useState("");
  let [isSubmiting, setIsSubmiting] = useState(false);
  const statusMessages: { [index: number]: string } = {
    400: "Bad Request",
    401: "Unauthorized",
    403: "Forbidden",
    500: "Internal Server Error",
  };

  function showPopUp(message: string) {
    setPopUpMessage(message);
    setHidePopUp(false);

    setTimeout(() => {
      setPopUpMessage("");
      setHidePopUp(true);
    }, 5000);
  }

  function toggleSubmitState() {
    setIsSubmiting((prevState) => !prevState);
  }

  async function handleJobSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    toggleSubmitState();

    if (
      !(
        jobData[jobInputId] ||
        jobData[companyInputId] ||
        jobData[statusInputId]
      )
    ) {
      toggleSubmitState();
      return;
    }

    let url = `${BACKEND_URL}/api/job${jobId ? "/" + jobId : ""}`;
    let method = "POST";
    let body = "";

    if (action === "Update") {
      console.log("prev status", jobData.status);
      method = "PUT";

      // if left empty, set to previous value
      let jobTitle = jobData[jobInputId]
        ? jobData[jobInputId]
        : prevJobData?.jobTitle;
      let company = jobData[companyInputId]
        ? jobData[companyInputId]
        : prevJobData?.company;
      let status = jobData[statusInputId]
        ? jobData[statusInputId]
        : prevJobData?.status;
      console.log(jobTitle);
      console.log(company);
      console.log(status);

      body = JSON.stringify({
        jobTitle,
        company,
        status,
      });
    } else {
      body = JSON.stringify({
        jobTitle: jobData[jobInputId],
        company: jobData[companyInputId],
        status: "Not Responded",
      });
    }

    try {
      const response = await fetch(url, {
        method: method,
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + localStorage.token,
        },
        body: body,
      });

      if (!response.ok) {
        let message = await response.text();
        if (!message || message.length < 1) {
          message = "Failed. Reason: " + statusMessages[response.status];
        }

        showPopUp(message);
        toggleSubmitState();
        console.error(message);
        return;
      }

      toggleSubmitState();
      navigate(0);
    } catch (error: any) {
      showPopUp("Error: " + error.message);
      toggleSubmitState();
      console.error("Error: ", error.message);
    }
  }

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const type = event.currentTarget.id;
    const value = event.currentTarget.value;

    setJobData({
      ...jobData,
      [type]: value,
    });
  }

  return (
    <dialog id={id} className="modal modal-bottom sm:modal-middle">
      <ErrorPopup
        className="absolute top-1/4 sm:top-1/9"
        hidden={hidePopUp}
        message={popUpMessage}
      />
      <div className="modal-box">
        <form method="dialog">
          <button className="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">
            ✕
          </button>
        </form>
        <h3 className="font-bold text-lg">{action} a Job</h3>
        <form
          className="space-y-4 flex flex-col"
          id="add-job-form"
          onSubmit={(event) => {
            handleJobSubmit(event);
          }}
        >
          <div>
            <label htmlFor={jobInputId}>
              <p>Job Title</p>
            </label>
            <input
              id={jobInputId}
              className="input"
              minLength={action === "Add" ? 1 : 0}
              maxLength={255}
              required={action === "Add"}
              onChange={handleChange}
            />
          </div>
          <div>
            <label htmlFor={companyInputId}>
              <p>Company</p>
            </label>
            <input
              id={companyInputId}
              className="input"
              minLength={action === "Add" ? 1 : 0}
              maxLength={255}
              required={action === "Add"}
              onChange={handleChange}
            />
          </div>
          {action === "Update" && (
            <div>
              <label htmlFor={statusInputId}>
                <p>Status</p>
              </label>
              <input
                id={statusInputId}
                className="input"
                maxLength={255}
                onChange={handleChange}
              />
            </div>
          )}
          <SubmitButton
            isSubmiting={isSubmiting}
            id={`${action}-submit-button`}
          />
        </form>
      </div>
    </dialog>
  );
}
