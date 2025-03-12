interface Job {
  jobId: string;
  jobTitle: string;
  company: string;
  status: string;
  appliedDate: Date;
  userId: string;
}

interface Token {
  sub: string;
  token_use: "token" | "refresh_token";
  exp: number;
  iat: number;
}

export type { Job, Token };
