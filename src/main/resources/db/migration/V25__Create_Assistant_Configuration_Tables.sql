-- Create assistant_configurations table
CREATE TABLE assistant_configurations (
    id SERIAL PRIMARY KEY,
    assistant_id VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    client_id VARCHAR(255) NOT NULL,
    client_email VARCHAR(255) NOT NULL,
    client_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create assistant_documents table
CREATE TABLE assistant_documents (
    id SERIAL PRIMARY KEY,
    assistant_id VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_assistant_configurations_assistant_id ON assistant_configurations(assistant_id);
CREATE INDEX idx_assistant_configurations_client_id ON assistant_configurations(client_id);
CREATE INDEX idx_assistant_configurations_status ON assistant_configurations(status);
CREATE INDEX idx_assistant_documents_assistant_id ON assistant_documents(assistant_id);
CREATE INDEX idx_assistant_documents_uploaded_by ON assistant_documents(uploaded_by);

-- Add comments for documentation
COMMENT ON TABLE assistant_configurations IS 'Stores assistant configuration data including subject, description, and status';
COMMENT ON COLUMN assistant_configurations.assistant_id IS 'ID of the assistant being configured';
COMMENT ON COLUMN assistant_configurations.subject IS 'Subject/title of the assistant configuration';
COMMENT ON COLUMN assistant_configurations.description IS 'Detailed description of the assistant configuration';
COMMENT ON COLUMN assistant_configurations.status IS 'Current status of the configuration (PENDING, OPEN, IN_PROGRESS, RESOLVED, CLOSED)';
COMMENT ON COLUMN assistant_configurations.client_id IS 'ID of the client configuring the assistant';
COMMENT ON COLUMN assistant_configurations.client_email IS 'Email of the client configuring the assistant';
COMMENT ON COLUMN assistant_configurations.client_name IS 'Name of the client configuring the assistant';

COMMENT ON TABLE assistant_documents IS 'Stores uploaded documents for assistant configurations';
COMMENT ON COLUMN assistant_documents.assistant_id IS 'ID of the assistant the document belongs to';
COMMENT ON COLUMN assistant_documents.file_name IS 'Original name of the uploaded file';
COMMENT ON COLUMN assistant_documents.file_path IS 'Path where the file is stored';
COMMENT ON COLUMN assistant_documents.file_type IS 'Type/format of the file (PDF, DOC, DOCX, TXT)';
COMMENT ON COLUMN assistant_documents.file_size IS 'Size of the file in bytes';
COMMENT ON COLUMN assistant_documents.uploaded_by IS 'User ID who uploaded the document'; 