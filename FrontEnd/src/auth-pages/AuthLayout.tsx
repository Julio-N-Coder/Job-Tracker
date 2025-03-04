import { NavLink } from "react-router";

export default function AuthLayout({ type }: { type: string }) {
  return (
    <div className="flex flex-grow flex-col gap-8 justify-center items-center">
      <div className="bg-base-200 p-4 rounded-2xl flex flex-col items-center gap-4">
        <form className="flex flex-col items-center">
          <fieldset className="fieldset">
            <legend className="fieldset-legend text-lg">Username</legend>
            <input type="input" className="input w-96" placeholder="Username" />
            <legend className="fieldset-legend text-lg">Password</legend>
            <input type="text" className="input w-96" placeholder="Password" />
          </fieldset>
          <button className="btn btn-accent mt-2 text-lg">Submit</button>
        </form>
        <div className="flex flex-col">
          {type === "signup" ? (
            <p>Have an Account?</p>
          ) : (
            <p>Don't have an account?</p>
          )}
          <NavLink
            to={type == "signup" ? "/login" : "/signup"}
            className="btn btn-accent"
          >
            {type == "signup" ? "Login" : "SignUp"}
          </NavLink>
        </div>
      </div>
    </div>
  );
}
