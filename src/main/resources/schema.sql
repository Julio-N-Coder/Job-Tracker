CREATE TABLE IF NOT EXISTS jobs ( 
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  job_title VARCHAR(255) NOT NULL,
  company VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  applied_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  username VARCHAR(30) UNIQUE NOT NULL,
  hashed_password VARCHAR(50) NOT NULL
);