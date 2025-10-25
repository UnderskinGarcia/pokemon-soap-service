-- Create SonarQube database
CREATE DATABASE sonar;

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON DATABASE sonar TO "pokemon";

-- Ensure the user has permissions on the sonar database
\c sonar
GRANT USAGE ON SCHEMA public TO "pokemon";
GRANT CREATE ON SCHEMA public TO "pokemon";
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "pokemon";
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "pokemon";