import picture from "./assets/job-search-picture.svg";

export default function Index() {
  return (
    <div className="flex flex-col-reverse items-center lg:flex-row justify-around lg:pr-8">
      <img
        className="w-100 h-100 sm:w-130 sm:h-130 xl:w-170 xl:h-170"
        src={picture}
        alt="Image of person looking for a job"
      />
      <h1 className="text-7xl xsm:text-8xl sm:text-9xl font-bold flex flex-col items-center justify-center">
        <span>Job</span>
        <span>Tracker</span>
      </h1>
    </div>
  );
}
