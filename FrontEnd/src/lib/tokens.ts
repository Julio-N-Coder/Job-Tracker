import { Token } from "../types";

export default async function areTokensValid() {
  if (localStorage.token) {
    if (isTokenExpired(localStorage.token)) {
      localStorage.removeItem("token");
    } else {
      return true;
    }
  } else if (!localStorage.refresh_token) {
    return false;
  } else if (isTokenExpired(localStorage.refresh_token)) {
    localStorage.removeItem("refresh_token");
    return false;
  }
  console.log("is running?");

  const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
  const refresh_token: string = localStorage.refresh_token;

  try {
    const response = await fetch(`${BACKEND_URL}/api/token/refresh`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${refresh_token}`,
      },
    });

    if (!response.ok) {
      return false;
    }
    const newToken = await response.text();
    localStorage.token = newToken;

    return true;
  } catch (error: any) {
    console.error("Error: ", error.message);
  }

  return false;
}

function isTokenExpired(token: string) {
  let tokenClaims: Token = JSON.parse(atob(token.split(".")[1]));
  let expiration = tokenClaims.exp * 1000;

  if (expiration < Date.now()) {
    return true;
  }
  return false;
}
