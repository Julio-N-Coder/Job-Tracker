import picture from "./assets/job-search-picture.svg";

export default function Index() {
  return (
    <div className="flex justify-around pr-8">
      <img
        className="w-170 h-170"
        src={picture}
        alt="Image of person looking for a job"
      />
      <h1 className="text-9xl font-bold flex flex-col items-center justify-center">
        <span>Job</span>
        <span>Tracker</span>
      </h1>
    </div>
  );
}
