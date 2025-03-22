import { Job, JobBasicData } from "../../types";
import JobModel from "../jobs/JobModel";

export default function Card({
  id,
  jobTitle,
  company,
  status,
  appliedDate,
  userId,
}: Job) {
  const updateJobId = "update-job-id";

  function passData(): JobBasicData {
    return {
      jobTitle: jobTitle,
      company: company,
      status: status,
    };
  }

  return (
    <div className="bg-base-300 p-2 rounded text-lg flex gap-2">
      <div className="space-y-2" id={id} data-userid={userId}>
        <h3 className="text-3xl text-center font-bold">{company}</h3>
        <p>Job Title: {jobTitle}</p>
        <p>Status: {status}</p>
        <div className="flex flex-col items-center">
          <p className="underline">Applied Date</p>
          <p>{appliedDate.toISOString().split("T")[0]}</p>
        </div>
      </div>
      <div className="flex flex-col justify-around">
        <button
          className="btn btn-accent"
          onClick={() =>
            (
              document.getElementById(updateJobId) as HTMLDialogElement
            ).showModal()
          }
        >
          Update
        </button>
        <button className="btn btn-accent">Delete</button>
      </div>
      <JobModel
        action="Update"
        id={updateJobId}
        jobId={id}
        prevJobData={passData()}
      />
    </div>
  );
}
