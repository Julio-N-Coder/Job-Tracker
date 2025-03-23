import { useNavigate } from "react-router";
import { Job } from "../../types";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export default function Card({
  id,
  jobTitle,
  company,
  status,
  appliedDate,
  userId,
  modelId,
}: Job & { modelId: string }) {
  let navigation = useNavigate();
  const statusMessages: { [index: number]: string } = {
    400: "Bad Request",
    401: "Unauthorized",
    403: "Forbidden",
    500: "Internal Server Error",
  };

  async function deleteJob() {
    try {
      const response = await fetch(`${BACKEND_URL}/api/job/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: "Bearer " + localStorage.token,
        },
      });

      if (!response.ok) {
        let message = await response.text();
        if (!message || message.length < 1) {
          message = "Failed. Reason: " + statusMessages[response.status];
        }

        console.error(message);
      }

      navigation(0);
    } catch (error: any) {
      console.error("Error: ", error.message);
    }
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
          onClick={() => {
            const dialog = document.getElementById(
              modelId
            ) as HTMLDialogElement;
            dialog.setAttribute("data-job-id", id);
            dialog.showModal();
          }}
        >
          Update
        </button>
        <button className="btn btn-accent" onClick={deleteJob}>
          Delete
        </button>
      </div>
    </div>
  );
}
