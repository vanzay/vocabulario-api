--
--

CREATE OR REPLACE PROCEDURE "calculate_group_order"(param_book_id int) AS
$$
UPDATE book_phrase bp
SET group_order = p.group_number
FROM phrase p
WHERE bp.phrase_id = p.phrase_id
  AND bp.book_id = param_book_id;

UPDATE book
SET unique_groups = tmp.unique_groups
FROM (
         SELECT count(DISTINCT (group_order)) AS unique_groups
         FROM book_phrase
         WHERE book_id = param_book_id
     ) AS tmp
WHERE book_id = param_book_id;

UPDATE book_phrase bp
SET group_frequency = tmp.group_frequency
FROM (
         SELECT group_order, sum(frequency) as group_frequency
         FROM book_phrase
         WHERE book_id = param_book_id
         GROUP BY group_order
     ) AS tmp
WHERE bp.group_order = tmp.group_order
  AND bp.book_id = param_book_id;

UPDATE book_phrase bp
SET group_order = tmp.row_num
FROM (
         SELECT book_phrase_id,
                ROW_NUMBER() OVER (ORDER BY group_frequency DESC, group_order, frequency DESC) AS row_num
         FROM book_phrase
         WHERE book_id = param_book_id
     ) AS tmp
WHERE bp.book_phrase_id = tmp.book_phrase_id
  AND bp.book_id = param_book_id;
$$ LANGUAGE SQL;

--
--

CREATE OR REPLACE PROCEDURE "delete_book"(param_book_id int) AS
$$
DELETE
FROM user_book
WHERE book_id = param_book_id;

DELETE
FROM book_phrase
WHERE book_id = param_book_id;

DELETE
FROM unexpected_word
WHERE book_id = param_book_id;

DELETE
FROM book
WHERE book_id = param_book_id;
$$ LANGUAGE SQL;
