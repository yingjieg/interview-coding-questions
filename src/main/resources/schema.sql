CREATE TABLE IF NOT EXISTS users
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    email
    VARCHAR
(
    100
) UNIQUE NOT NULL,
    password VARCHAR
(
    255
) NOT NULL,
    first_name VARCHAR
(
    50
),
    last_name VARCHAR
(
    50
),
    enabled BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reset_token VARCHAR
(
    255
),
    reset_token_expiry TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS orders
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    user_id
    BIGINT
    NOT
    NULL
    REFERENCES
    users
(
    id
),
    order_status VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS order_items
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    order_id
    BIGINT
    NOT
    NULL
    REFERENCES
    orders
(
    id
),
    attraction_name VARCHAR
(
    100
) NOT NULL,
    attraction_external_id VARCHAR
(
    50
) NOT NULL,
    unit_price DECIMAL
(
    10,
    2
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS bookings
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    order_id
    BIGINT
    NOT
    NULL
    REFERENCES
    orders
(
    id
),
    user_id BIGINT NOT NULL REFERENCES users
(
    id
),
    visit_date DATE NOT NULL,
    booking_status VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING',
    ticket_submission_status VARCHAR
(
    20
) NOT NULL DEFAULT 'NOT_SUBMITTED',
    ticket_submitted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS idempotency_records
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    idempotency_key
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users
(
    id
),
    request_hash VARCHAR
(
    255
) NOT NULL,
    response_data TEXT NOT NULL,
    status VARCHAR
(
    20
) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_reset_token ON users(reset_token);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_order_id ON bookings(order_id);
CREATE INDEX IF NOT EXISTS idx_bookings_visit_date ON bookings(visit_date);
CREATE INDEX IF NOT EXISTS idx_idempotency_key ON idempotency_records(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_idempotency_expires_at ON idempotency_records(expires_at);