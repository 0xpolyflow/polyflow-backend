ALTER TABLE polyflow.user
    ADD COLUMN stripe_customer_id VARCHAR UNIQUE,
    ADD COLUMN total_domain_limit INT NOT NULL DEFAULT 0,
    ADD COLUMN total_seat_limit   INT NOT NULL DEFAULT 0;
