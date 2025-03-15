import { Job } from "../../types";

export default function Card({
  jobId,
  jobTitle,
  company,
  status,
  appliedDate,
  userId,
}: Job) {
  return (
    <div
      className="bg-base-300 p-2 rounded text-lg space-y-2"
      id={jobId}
      data-userid={userId}
    >
      <h3 className="text-3xl text-center font-bold">{company}</h3>
      <p>Job Title: {jobTitle}</p>
      <p>Status: {status}</p>
      <div className="flex flex-col items-center">
        <p className="underline">Applied Date</p>
        <p>{appliedDate.toISOString().split("T")[0]}</p>
      </div>
    </div>
  );
}
