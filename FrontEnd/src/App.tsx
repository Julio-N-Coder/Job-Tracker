import { useState } from "react";
import Navbar from "./navbar/Navbar";
import Card from "./card/Card";

function App() {
  const [count, setCount] = useState(0);

  return (
    <div className="antialiased">
      <Navbar />
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
    </div>
  );
}

export default App;
