import { ChangeEvent, FormEvent, useState, useRef } from "react";
import { useNavigate } from "react-router";
import SubmitButton from "../SubmitButton";
import FormModel from "../baseModels/FormModel";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export default function JobModel({
  action,
  id,
}: {
  action: "Add" | "Update";
  id: string;
}) {
  let navigate = useNavigate();
  let dialogRef = useRef<HTMLDialogElement>(null);
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
    404: "Not Found",
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

    // job id is added to dialog element when update button is pressed
    // undefined if add job button is pressed
    let jobId = dialogRef.current?.dataset.jobId;

    let url = `${BACKEND_URL}/api/job${jobId ? "/" + jobId : ""}`;
    let method = "POST";
    let body = "";

    if (action === "Update" && jobId != null) {
      method = "PUT";

      // grab job data from it's card
      const jobCard = document.getElementById(jobId) as HTMLDivElement;
      let prevJobTitle = jobCard.childNodes[0].textContent;
      let prevCompany = jobCard.childNodes[1].textContent?.split(": ")[1];
      let prevStatus = jobCard.childNodes[2].textContent?.split(": ")[1];

      // if left empty, set to previous value
      let jobTitle = jobData[jobInputId] ? jobData[jobInputId] : prevJobTitle;
      let company = jobData[companyInputId]
        ? jobData[companyInputId]
        : prevCompany;
      let status = jobData[statusInputId] ? jobData[statusInputId] : prevStatus;

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
    <FormModel
      id={id}
      dialogRef={dialogRef}
      hidePopUp={hidePopUp}
      popUpMessage={popUpMessage}
    >
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
    </FormModel>
  );
}
