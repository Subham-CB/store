-- Products
INSERT INTO product (id, description) VALUES (1, 'Ergonomic Steel Keyboard');
INSERT INTO product (id, description) VALUES (2, 'Rustic Wooden Chair');
INSERT INTO product (id, description) VALUES (3, 'Intelligent Cotton Hat');
INSERT INTO product (id, description) VALUES (4, 'Practical Granite Table');
INSERT INTO product (id, description) VALUES (5, 'Awesome Rubber Shoes');
INSERT INTO product (id, description) VALUES (6, 'Sleek Plastic Bottle');
INSERT INTO product (id, description) VALUES (7, 'Handcrafted Wool Jacket');
INSERT INTO product (id, description) VALUES (8, 'Modern Leather Wallet');
INSERT INTO product (id, description) VALUES (9, 'Durable Bronze Lamp');
INSERT INTO product (id, description) VALUES (10, 'Aerodynamic Linen Shirt');

INSERT INTO product (id, description) VALUES (11, 'Fantastic Copper Clock');
INSERT INTO product (id, description) VALUES (12, 'Small Marble Plate');
INSERT INTO product (id, description) VALUES (13, 'Heavy Duty Iron Bench');
INSERT INTO product (id, description) VALUES (14, 'Portable Wooden Desk');
INSERT INTO product (id, description) VALUES (15, 'Elegant Ceramic Vase');
INSERT INTO product (id, description) VALUES (16, 'Compact Granite Keyboard');
INSERT INTO product (id, description) VALUES (17, 'Smart Aluminum Watch');
INSERT INTO product (id, description) VALUES (18, 'Incredible Silk Gloves');
INSERT INTO product (id, description) VALUES (19, 'Recycled Cotton Backpack');
INSERT INTO product (id, description) VALUES (20, 'Lightweight Steel Bottle');

INSERT INTO product (id, description) VALUES (21, 'Premium Leather Sofa');
INSERT INTO product (id, description) VALUES (22, 'Advanced Plastic Monitor');
INSERT INTO product (id, description) VALUES (23, 'Classic Wooden Shelf');
INSERT INTO product (id, description) VALUES (24, 'Sleek Rubber Mouse');
INSERT INTO product (id, description) VALUES (25, 'Gorgeous Bronze Table');
INSERT INTO product (id, description) VALUES (26, 'Practical Wool Blanket');
INSERT INTO product (id, description) VALUES (27, 'Intelligent Granite Chair');
INSERT INTO product (id, description) VALUES (28, 'Ergonomic Cotton Hoodie');
INSERT INTO product (id, description) VALUES (29, 'Handmade Ceramic Mug');
INSERT INTO product (id, description) VALUES (30, 'Durable Aluminum Laptop Stand');


-- Product ↔ Order mappings

INSERT INTO order_product (order_id, product_id) VALUES (1, 1);
INSERT INTO order_product (order_id, product_id) VALUES (1, 5);

INSERT INTO order_product (order_id, product_id) VALUES (2, 2);
INSERT INTO order_product (order_id, product_id) VALUES (2, 8);

INSERT INTO order_product (order_id, product_id) VALUES (3, 3);
INSERT INTO order_product (order_id, product_id) VALUES (3, 10);

INSERT INTO order_product (order_id, product_id) VALUES (4, 4);
INSERT INTO order_product (order_id, product_id) VALUES (4, 15);

INSERT INTO order_product (order_id, product_id) VALUES (5, 6);
INSERT INTO order_product (order_id, product_id) VALUES (5, 20);

INSERT INTO order_product (order_id, product_id) VALUES (6, 7);
INSERT INTO order_product (order_id, product_id) VALUES (6, 25);

INSERT INTO order_product (order_id, product_id) VALUES (7, 9);
INSERT INTO order_product (order_id, product_id) VALUES (7, 30);

INSERT INTO order_product (order_id, product_id) VALUES (8, 11);
INSERT INTO order_product (order_id, product_id) VALUES (8, 18);

INSERT INTO order_product (order_id, product_id) VALUES (9, 12);
INSERT INTO order_product (order_id, product_id) VALUES (9, 22);

INSERT INTO order_product (order_id, product_id) VALUES (10, 13);
INSERT INTO order_product (order_id, product_id) VALUES (10, 27);

INSERT INTO order_product (order_id, product_id) VALUES (11, 14);
INSERT INTO order_product (order_id, product_id) VALUES (11, 21);

INSERT INTO order_product (order_id, product_id) VALUES (12, 16);
INSERT INTO order_product (order_id, product_id) VALUES (12, 24);

INSERT INTO order_product (order_id, product_id) VALUES (13, 17);
INSERT INTO order_product (order_id, product_id) VALUES (13, 19);

INSERT INTO order_product (order_id, product_id) VALUES (14, 23);
INSERT INTO order_product (order_id, product_id) VALUES (14, 26);

INSERT INTO order_product (order_id, product_id) VALUES (15, 28);
INSERT INTO order_product (order_id, product_id) VALUES (15, 29);