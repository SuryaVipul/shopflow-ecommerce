CREATE TABLE categories (
                            id         BIGINT       NOT NULL AUTO_INCREMENT,
                            name       VARCHAR(120) NOT NULL,
                            slug       VARCHAR(140) NOT NULL,
                            parent_id  BIGINT       NULL,
                            created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (id),
                            CONSTRAINT uk_categories_slug UNIQUE (slug),
                            CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE SET NULL,
                            INDEX idx_categories_parent_id (parent_id)
);

CREATE TABLE products (
                          id           BIGINT         NOT NULL AUTO_INCREMENT,
                          sku          VARCHAR(64)    NOT NULL,
                          name         VARCHAR(255)   NOT NULL,
                          description  TEXT,
                          price        DECIMAL(12, 2) NOT NULL,
                          category_id  BIGINT         NOT NULL,
                          avg_rating   DECIMAL(3, 2)  NOT NULL DEFAULT 0.00,
                          review_count INT            NOT NULL DEFAULT 0,
                          active       BOOLEAN        NOT NULL DEFAULT TRUE,
                          created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          CONSTRAINT uk_products_sku UNIQUE (sku),
                          CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
                          CONSTRAINT chk_products_price CHECK (price >= 0),
                          INDEX idx_products_name (name),
                          INDEX idx_products_category_id (category_id),
                          INDEX idx_products_active_price (active, price)
);

CREATE TABLE product_images (
                                id         BIGINT       NOT NULL AUTO_INCREMENT,
                                product_id BIGINT       NOT NULL,
                                image_url  VARCHAR(500) NOT NULL,
                                is_primary BOOLEAN      NOT NULL DEFAULT FALSE,
                                PRIMARY KEY (id),
                                CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
                                INDEX idx_product_images_product_id (product_id)
);

CREATE TABLE reviews (
                         id         BIGINT    NOT NULL AUTO_INCREMENT,
                         product_id BIGINT    NOT NULL,
                         user_id    BIGINT    NOT NULL,
                         rating     INT       NOT NULL,
                         comment    TEXT,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
                         CONSTRAINT uk_reviews_product_user UNIQUE (product_id, user_id),
                         CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
                         INDEX idx_reviews_product_id (product_id)
);