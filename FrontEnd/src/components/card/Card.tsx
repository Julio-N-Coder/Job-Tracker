import { Job } from "../../types";

export default function Card({jobId, jobTitle, company, status, appliedDate, userId}: Job) {
  return (
    <div>
      <h3>Card</h3>
      <h4>{jobId}</h4>
      <h4>{jobTitle}</h4>
      <h4>{company}</h4>
      <h4>{status}</h4>
      <h4>{appliedDate.toISOString()}</h4>
      <h4>{userId}</h4>
    </div>
  );
}
