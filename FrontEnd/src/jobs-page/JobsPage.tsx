import { FormEvent, useEffect, useState } from "react";
import Card from "../components/card/Card";
import { useNavigate } from "react-router";
import { Job } from "../types";
import areTokensValid from "../lib/tokens";

export default function JobsPage() {
  const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
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

        setJobs(await response.json());
        console.log("jobs: ", jobs);
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
        {jobs.map((job) => (
          <Card
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
  function handleJobSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    console.log("send post request here");
  }

  return (
    <dialog id="add-job-id" className="modal modal-bottom sm:modal-middle">
      <div className="modal-box">
        <form method="dialog">
          <button className="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">
            ✕
          </button>
        </form>
        <h3 className="font-bold text-lg">Hello!</h3>
        <p className="">Press ESC key or click the button below to close</p>
        <p className="">Model not setup. add form to add a job</p>
        <form
          id="add-job-form"
          onSubmit={(event) => {
            handleJobSubmit(event);
          }}
        >
          <button className="btn btn-accent">Submit</button>
        </form>
      </div>
    </dialog>
  );
}
