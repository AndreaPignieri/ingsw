CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE agency (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(50),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users ( 
    id SERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255), -- NULL if login is OAuth only
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    agency_id INT,
    CONSTRAINT fk_user_agency FOREIGN KEY (agency_id) REFERENCES agency(id) ON DELETE SET NULL
);

CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_role (
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

CREATE TABLE agent (
    id INT PRIMARY KEY,
    biography TEXT,
    profile_photo VARCHAR(255),
    CONSTRAINT fk_agent_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE property (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200),
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('sale', 'rent')),
    size_sqm INT,
    rooms INT,
    floor INT,
    energy_class VARCHAR(5),
    energy_class VARCHAR(5),
    address VARCHAR(255),
    city VARCHAR(100),
    
    -- PostGIS Geometry column
    location GEOGRAPHY(Point, 4326),
    
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    agent_id INT NOT NULL,
    agency_id INT NOT NULL,
    CONSTRAINT fk_property_agent FOREIGN KEY (agent_id) REFERENCES agent(id),
    CONSTRAINT fk_property_agency FOREIGN KEY (agency_id) REFERENCES agency(id)
);

CREATE TABLE property_photo (
    id SERIAL PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    display_order INT DEFAULT 0,
    property_id INT NOT NULL,
    CONSTRAINT fk_photo_property FOREIGN KEY (property_id) REFERENCES property(id) ON DELETE CASCADE
);

CREATE TABLE amenity (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE property_amenity (
    property_id INT,
    amenity_id INT,
    PRIMARY KEY (property_id, amenity_id),
    CONSTRAINT fk_prop_amenity_property FOREIGN KEY (property_id) REFERENCES property(id) ON DELETE CASCADE,
    CONSTRAINT fk_prop_amenity_amenity FOREIGN KEY (amenity_id) REFERENCES amenity(id) ON DELETE CASCADE
);


CREATE TABLE external_account (
    id SERIAL PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    user_id INT,
    UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_external_account_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE agent_evaluation (
    id SERIAL PRIMARY KEY,
    score INT CHECK (score BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    agent_id INT,
    user_id INT,
    CONSTRAINT fk_evaluation_agent FOREIGN KEY (agent_id) REFERENCES agent(id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_property_city ON property(city);
CREATE INDEX idx_property_price ON property(price);

CREATE INDEX idx_property_location ON property USING GIST (location);