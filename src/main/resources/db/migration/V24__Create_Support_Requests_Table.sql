-- Create support_requests table
CREATE TABLE support_requests (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    subject VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_support_requests_user_id ON support_requests(user_id);
CREATE INDEX idx_support_requests_status ON support_requests(status);
CREATE INDEX idx_support_requests_created_at ON support_requests(created_at);

-- Add comments for documentation
COMMENT ON TABLE support_requests IS 'Stores support requests from users';
COMMENT ON COLUMN support_requests.user_id IS 'ID of the user submitting the request';
COMMENT ON COLUMN support_requests.user_email IS 'Email of the user submitting the request';
COMMENT ON COLUMN support_requests.user_name IS 'Name of the user submitting the request';
COMMENT ON COLUMN support_requests.subject IS 'Subject/title of the support request';
COMMENT ON COLUMN support_requests.message IS 'Detailed message/description of the support request';
COMMENT ON COLUMN support_requests.status IS 'Current status of the support request (pending, open, closed, etc.)'; 