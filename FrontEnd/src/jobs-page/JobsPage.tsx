import { useEffect, useState } from "react";
import Card from "../components/card/Card";
import { useNavigate } from "react-router";
import { Job } from "../types";
import areTokensValid from "../lib/tokens";
import JobModel from "../components/jobs/JobModel";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export default function JobsPage() {
  let navigate = useNavigate();
  let [jobs, setJobs] = useState<Job[]>([]);
  const addJobId = "add-job-id";

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
            (document.getElementById(addJobId) as HTMLDialogElement).showModal()
          }
        >
          Add Job
        </button>
      </div>
      <div className="p-2 flex justify-around flex-wrap gap-2">
        {/* display jobs card data */}
        {jobs.map((job, index) => (
          <Card
            key={job.id || index}
            id={job.id}
            jobTitle={job.jobTitle}
            company={job.company}
            status={job.status}
            appliedDate={job.appliedDate}
            userId={job.userId}
          />
        ))}
      </div>
      <JobModel action="Add" id={addJobId} />
    </div>
  );
}
