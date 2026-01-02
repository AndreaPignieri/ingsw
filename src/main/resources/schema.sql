CREATE TABLE IF NOT EXISTS agency (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(50),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "user" ( 
    id SERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255), -- NULL if login is OAuth only
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    agency_id INT REFERENCES agency(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id INT REFERENCES "user"(id) ON DELETE CASCADE,
    role_id INT REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS agent (
    id INT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    biography TEXT,
    profile_photo VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS property (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200),
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('sale', 'rent')),
    size_sqm INT,
    rooms INT,
    floor INT,
    elevator BOOLEAN,
    energy_class VARCHAR(5),
    address VARCHAR(255),
    city VARCHAR(100),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    agent_id INT NOT NULL REFERENCES agent(id),
    agency_id INT NOT NULL REFERENCES agency(id)
);

CREATE TABLE IF NOT EXISTS property_photo (
    id SERIAL PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    display_order INT DEFAULT 0,
    property_id INT NOT NULL REFERENCES property(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS amenity (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS property_amenity (
    property_id INT REFERENCES property(id) ON DELETE CASCADE,
    amenity_id INT REFERENCES amenity(id) ON DELETE CASCADE,
    PRIMARY KEY (property_id, amenity_id)
);


CREATE TABLE IF NOT EXISTS external_account (
    id SERIAL PRIMARY KEY,
    provider VARCHAR(50) NOT NULL, -- 'google', 'facebook'
    provider_user_id VARCHAR(255) NOT NULL,
    user_id INT REFERENCES "user"(id) ON DELETE CASCADE,
    UNIQUE (provider, provider_user_id)
);


CREATE TABLE IF NOT EXISTS agent_evaluation (
    id SERIAL PRIMARY KEY,
    score INT CHECK (score BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    agent_id INT REFERENCES agent(id) ON DELETE CASCADE,
    user_id INT REFERENCES "user"(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_property_city ON property(city);
CREATE INDEX IF NOT EXISTS idx_property_price ON property(price);

CREATE INDEX IF NOT EXISTS idx_property_lat ON property(latitude);
CREATE INDEX IF NOT EXISTS idx_property_lon ON property(longitude);
