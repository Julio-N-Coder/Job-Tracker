interface JobBasicData {
  jobTitle: string;
  company: string;
  status: string;
}

interface Job extends JobBasicData {
  id: string;
  appliedDate: Date;
  userId: string;
}

interface Token {
  sub: string;
  token_use: "token" | "refresh_token";
  exp: number;
  iat: number;
}

export type { JobBasicData, Job, Token };
