import { useState } from "react";
import Card from "../components/card/Card";

export default function JobsPage() {
  const [count, setCount] = useState(0);

  return (
    <div className="container mx-auto bg-base-200">
      <button
        className="btn btn-primary"
        onClick={() => setCount((count) => count + 1)}
      >
        count is {count}
      </button>
      {/* fetch jobs and display card data here */}
      <Card />
    </div>
  );
}
