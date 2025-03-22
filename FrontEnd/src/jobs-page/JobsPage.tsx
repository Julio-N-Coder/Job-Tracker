import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import Card from "../components/card/Card";
import { useNavigate } from "react-router";
import { Job } from "../types";
import areTokensValid from "../lib/tokens";
import ErrorPopup from "../components/ErrorPopup";
import SubmitButton from "../components/SubmitButton";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export default function JobsPage() {
  let navigate = useNavigate();
  let [jobs, setJobs] = useState<Job[]>([]);

  useEffect(() => {
    async function fetchJobs() {
      try {
        if (!(await areTokensValid())) {
          navigate("/login");
        }

        const response = await fetch(`${BACKEND_URL}/api/jobs`, {
          headers: {
            Authorization: "Bearer " + localStorage.token,
          },
        });

        if (!response.ok) {
          navigate("/login");
        }

        let newJobs = ((await response.json()) as Job[]).map((job) => {
          return {
            ...job,
            appliedDate: new Date(job.appliedDate),
          };
        });

        setJobs(newJobs);
      } catch (error: any) {
        console.error("Error: ", error.message);
      }
    }
    fetchJobs();
  }, []);

  return (
    <div className="container mx-auto bg-base-200 rounded-b-lg">
      <div className="bg-base-300 flex justify-between p-2">
        <h2 className="font-bold text-2xl">Jobs</h2>
        <button
          className="btn btn-accent text-xl"
          onClick={() =>
            (
              document.getElementById("add-job-id") as HTMLDialogElement
            ).showModal()
          }
        >
          Add Job
        </button>
      </div>
      <div className="p-2 flex justify-around flex-wrap gap-2">
        {/* display jobs card data */}
        {jobs.map((job, index) => (
          <Card
            key={job.jobId || index}
            jobId={job.jobId}
            jobTitle={job.jobTitle}
            company={job.company}
            status={job.status}
            appliedDate={job.appliedDate}
            userId={job.userId}
          />
        ))}
      </div>
      <AddJobModel />
    </div>
  );
}

function AddJobModel() {
  let navigate = useNavigate();
  const [jobData, setJobData] = useState({
    "job-title-input": "",
    "company-input": "",
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

    try {
      const response = await fetch(`${BACKEND_URL}/api/job`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + localStorage.token,
        },
        body: JSON.stringify({
          jobTitle: jobData["job-title-input"],
          company: jobData["company-input"],
          status: "Not Responded",
        }),
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
    <dialog id="add-job-id" className="modal modal-bottom sm:modal-middle">
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
        <h3 className="font-bold text-lg">Add a Job</h3>
        <form
          className="space-y-4 flex flex-col"
          id="add-job-form"
          onSubmit={(event) => {
            handleJobSubmit(event);
          }}
        >
          <div>
            <label htmlFor="job-title-input">
              <p>Job Title</p>
            </label>
            <input
              id="job-title-input"
              className="input"
              minLength={1}
              maxLength={255}
              required
              onChange={handleChange}
            />
          </div>
          <div>
            <label htmlFor="company-input">
              <p>Company</p>
            </label>
            <input
              id="company-input"
              className="input"
              minLength={1}
              maxLength={255}
              required
              onChange={handleChange}
            />
          </div>
          <SubmitButton isSubmiting={isSubmiting} />
        </form>
      </div>
    </dialog>
  );
}
