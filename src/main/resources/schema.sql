CREATE TABLE IF NOT EXISTS doc_chunk (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_id VARCHAR(36) NOT NULL,
  file_name VARCHAR(255),
  page_number INT,
  content TEXT NOT NULL,
  embedding LONGBLOB NOT NULL,
  INDEX idx_doc (doc_id)
);