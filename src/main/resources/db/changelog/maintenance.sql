-- Reset Customer Sequence
SELECT setval(pg_get_serial_sequence('customer', 'id'), coalesce(max(id), 0) + 1, false) FROM customer;

-- Reset Orders Sequence
SELECT setval(pg_get_serial_sequence('"order"', 'id'), coalesce(max(id), 0) + 1, false) FROM "order";