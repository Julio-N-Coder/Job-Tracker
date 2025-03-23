import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router";
import "./index.css";
import Index from "./index.tsx";
import Container from "./container.tsx";
import JobsPage from "./pages/jobs-page/JobsPage.tsx";
import About from "./about/about.tsx";
import LoginPage from "./pages/auth-pages/LoginPage.tsx";
import SignUpPage from "./pages/auth-pages/SignUpPage.tsx";
import ProfilePage from "./pages/profilePage/ProfilePage.tsx";

const root = document.getElementById("root") as ReactDOM.Container;

ReactDOM.createRoot(root).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<Container />}>
        <Route index element={<Index />} />
        <Route path="jobs-page" element={<JobsPage />} />
        <Route path="about" element={<About />} />
        <Route path="profile-page" element={<ProfilePage />} />
      </Route>
      {/* I made a seperate root route below so I can center input divs with a navbar */}
      <Route
        path="/"
        element={<Container className="min-h-screen flex flex-col" />}
      >
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
      </Route>
    </Routes>
  </BrowserRouter>
);
