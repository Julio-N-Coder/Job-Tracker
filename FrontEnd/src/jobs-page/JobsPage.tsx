import { useEffect, useState } from "react";
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
    <div className="container mx-auto bg-base-200">
      {/* display jobs card data */}
      {jobs.map(() => (
        <Card
          jobId="id"
          jobTitle="jobTitle"
          company="company"
          status="status"
          appliedDate={new Date()}
          userId="userId"
        />
      ))}
    </div>
  );
}
