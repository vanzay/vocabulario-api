SET lc_messages TO 'en_US.UTF-8';

--
--

CREATE OR REPLACE FUNCTION make_tsvector(title TEXT, author TEXT) RETURNS tsvector AS
$$
SELECT (setweight(to_tsvector('simple', title), 'A')
    || setweight(to_tsvector('simple', author), 'B'));
$$ LANGUAGE sql IMMUTABLE;

--
--

CREATE TABLE "language"
(
    "language_id"    int PRIMARY KEY,
    "native_name"    varchar(32) NOT NULL,
    "iso2"           varchar(2)  NOT NULL,
    "iso3"           varchar(3)  NOT NULL,
    "core_supported" boolean     NOT NULL,
    "ui_supported"   boolean     NOT NULL,
    CONSTRAINT "native_name_unique" UNIQUE ("native_name"),
    CONSTRAINT "iso3_unique" UNIQUE ("iso3"),
    CONSTRAINT "iso2_unique" UNIQUE ("iso2")
);

--
--

CREATE SEQUENCE "phrase_id_seq" START 10700000;

CREATE TABLE "phrase"
(
    "phrase_id"      int PRIMARY KEY      DEFAULT nextval('phrase_id_seq'),
    "term"           varchar(64) NOT NULL,
    "transcription"  varchar(96)          DEFAULT NULL,
    "visible"        boolean     NOT NULL DEFAULT true,
    "group_number"   int         NOT NULL DEFAULT 0,
    "base_phrase_id" int                  DEFAULT NULL,
    "language_id"    int         NOT NULL,
    CONSTRAINT "fk_phrase_base_phrase" FOREIGN KEY ("base_phrase_id") REFERENCES "phrase" ("phrase_id"),
    CONSTRAINT "fk_phrase_language" FOREIGN KEY ("language_id") REFERENCES "language" ("language_id")
);
CREATE INDEX "fk_phrase_base_phrase_idx" ON "phrase" ("base_phrase_id");
CREATE INDEX "fk_phrase_language_idx" ON "phrase" ("language_id");
CREATE INDEX "phrase_term_idx" ON "phrase" ("term");

ALTER SEQUENCE "phrase_id_seq" OWNED BY "phrase"."phrase_id";

--
--

CREATE SEQUENCE "translation_id_seq" START 100000;

CREATE TABLE "translation"
(
    "translation_id" int PRIMARY KEY DEFAULT nextval('translation_id_seq'),
    "term"           varchar(128) NOT NULL,
    "phrase_id"      int          NOT NULL,
    "language_id"    int          NOT NULL,
    CONSTRAINT "fk_translation_phrase" FOREIGN KEY ("phrase_id") REFERENCES "phrase" ("phrase_id"),
    CONSTRAINT "fk_translation_language" FOREIGN KEY ("language_id") REFERENCES "language" ("language_id")
);
CREATE INDEX "fk_translation_phrase_idx" ON "translation" ("phrase_id");
CREATE INDEX "fk_translation_language_idx" ON "translation" ("language_id");
CREATE INDEX "translation_term_idx" ON "translation" ("term");

ALTER SEQUENCE "translation_id_seq" OWNED BY "translation"."translation_id";

--
--

CREATE SEQUENCE "pronunciation_id_seq" START 500000;

CREATE TABLE "pronunciation"
(
    "pronunciation_id" int PRIMARY KEY       DEFAULT nextval('pronunciation_id_seq'),
    "url"              varchar(256) NOT NULL,
    "sort_order"       int          NOT NULL DEFAULT 0,
    "phrase_id"        int          NOT NULL,
    CONSTRAINT "fk_pronunciation_phrase" FOREIGN KEY ("phrase_id") REFERENCES "phrase" ("phrase_id")
);
CREATE INDEX "fk_pronunciation_phrase_idx" ON "pronunciation" ("phrase_id");

ALTER SEQUENCE "pronunciation_id_seq" OWNED BY "pronunciation"."pronunciation_id";

--
--

CREATE SEQUENCE "book_id_seq" START 1030600;

CREATE TABLE "book"
(
    "book_id"           int PRIMARY KEY       DEFAULT nextval('book_id_seq'),
    "author"            varchar(64)           DEFAULT NULL,
    "title"             varchar(128) NOT NULL,
    "added_date"        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "content_hash"      varchar(64)  NOT NULL,
    "total_words"       int          NOT NULL,
    "unique_words"      int          NOT NULL,
    "unique_groups"     int          NOT NULL,
    "common"            boolean      NOT NULL DEFAULT false,
    "translated"        boolean      NOT NULL DEFAULT false,
    "cover_url"         varchar(256)          DEFAULT NULL,
    "content_url"       varchar(256)          DEFAULT NULL,
    "audio_content_url" varchar(256)          DEFAULT NULL,
    "language_id"       int          NOT NULL,
    CONSTRAINT "fk_book_language1" FOREIGN KEY ("language_id") REFERENCES "language" ("language_id")
);
CREATE INDEX "fk_book_language_idx" ON "book" ("language_id");
CREATE INDEX "book_content_hash_idx" ON "book" ("content_hash");
CREATE INDEX "book_title_author_ftext_idx" ON "book" USING GIN (make_tsvector("title", "author"));

ALTER SEQUENCE "book_id_seq" OWNED BY "book"."book_id";

--
--

CREATE SEQUENCE "book_phrase_id_seq" START 900000;

CREATE TABLE "book_phrase"
(
    "book_phrase_id"  int PRIMARY KEY DEFAULT nextval('book_phrase_id_seq'),
    "part_key"        int NOT NULL,
    "frequency"       int NOT NULL,
    "group_frequency" int NOT NULL    DEFAULT 0,
    "group_order"     int NOT NULL    DEFAULT 0,
    "book_id"         int NOT NULL,
    "phrase_id"       int NOT NULL,
    CONSTRAINT "fk_book_phrase_book" FOREIGN KEY ("book_id") REFERENCES "book" ("book_id"),
    CONSTRAINT "fk_book_phrase_phrase" FOREIGN KEY ("phrase_id") REFERENCES "phrase" ("phrase_id")
);
CREATE INDEX "fk_book_phrase_book_idx" ON "book_phrase" ("book_id");
CREATE INDEX "fk_book_phrase_phrase_idx" ON "book_phrase" ("phrase_id");

ALTER SEQUENCE "book_phrase_id_seq" OWNED BY "book_phrase"."book_phrase_id";

--
--

CREATE SEQUENCE "unexpected_word_id_seq" START 50000;

CREATE TABLE "unexpected_word"
(
    "unexpected_word_id" int PRIMARY KEY DEFAULT nextval('unexpected_word_id_seq'),
    "term"               varchar(64) NOT NULL,
    "frequency"          int         NOT NULL,
    "book_id"            int         NOT NULL,
    CONSTRAINT "fk_unexpected_word_book" FOREIGN KEY ("book_id") REFERENCES "book" ("book_id")
);
CREATE INDEX "fk_unexpected_word_book_idx" ON "unexpected_word" ("book_id");

ALTER SEQUENCE "unexpected_word_id_seq" OWNED BY "unexpected_word"."unexpected_word_id";

--
--

CREATE SEQUENCE "user_id_seq" START 30000050;

CREATE TABLE "user"
(
    "user_id"           int PRIMARY KEY      DEFAULT nextval('user_id_seq'),
    "email"             varchar(64) NOT NULL,
    "password"          varchar(60) NOT NULL,
    "confirmed"         boolean     NOT NULL DEFAULT false,
    "registration_date" timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "language_id"       int         NOT NULL,
    CONSTRAINT "email_unique" UNIQUE ("email"),
    CONSTRAINT "fk_user_language" FOREIGN KEY ("language_id") REFERENCES "language" ("language_id")
);
CREATE INDEX "fk_user_language_idx" ON "user" ("language_id");

ALTER SEQUENCE "user_id_seq" OWNED BY "user"."user_id";

--
--

CREATE SEQUENCE "user_book_id_seq" START 200;

CREATE TABLE "user_book"
(
    "user_book_id"  int PRIMARY KEY    DEFAULT nextval('user_book_id_seq'),
    "added_date"    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "total_phrases" int       NOT NULL DEFAULT 0,
    "known_phrases" int       NOT NULL DEFAULT 0,
    "comfort"       smallint  NOT NULL DEFAULT 0,
    "book_id"       int       NOT NULL,
    "user_id"       int       NOT NULL,
    CONSTRAINT "fk_user_book_user" FOREIGN KEY ("user_id") REFERENCES "user" ("user_id"),
    CONSTRAINT "fk_user_book_book" FOREIGN KEY ("book_id") REFERENCES "book" ("book_id")
);
CREATE INDEX "fk_user_book_user_idx" ON "user_book" ("user_id");
CREATE INDEX "fk_user_book_book_idx" ON "user_book" ("book_id");

ALTER SEQUENCE "user_book_id_seq" OWNED BY "user_book"."user_book_id";

--
--

CREATE SEQUENCE "user_phrase_id_seq" START 1050000;

CREATE TABLE "user_phrase"
(
    "user_phrase_id"  int PRIMARY KEY    DEFAULT nextval('user_phrase_id_seq'),
    "part_key"        int       NOT NULL,
    "added_date"      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "on_studying"     boolean   NOT NULL,
    "last_activity"   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "memory_points"   int       NOT NULL DEFAULT 0,
    "audition_points" int       NOT NULL DEFAULT 0,
    "translation"     varchar(128)       DEFAULT NULL,
    "association"     varchar(64)        DEFAULT NULL,
    "user_id"         int       NOT NULL,
    "phrase_id"       int       NOT NULL,
    CONSTRAINT "fk_user_phrase_user" FOREIGN KEY ("user_id") REFERENCES "user" ("user_id"),
    CONSTRAINT "fk_user_phrase_phrase" FOREIGN KEY ("phrase_id") REFERENCES "phrase" ("phrase_id")
);
CREATE INDEX "fk_user_phrase_user_idx" ON "user_phrase" ("user_id");
CREATE INDEX "fk_user_phrase_phrase_idx" ON "user_phrase" ("phrase_id");

ALTER SEQUENCE "user_phrase_id_seq" OWNED BY "user_phrase"."user_phrase_id";

--
--

CREATE SEQUENCE "search_attempt_id_seq" START 200;

CREATE TABLE "search_attempt"
(
    "search_attempt_id" int PRIMARY KEY       DEFAULT nextval('search_attempt_id_seq'),
    "query_text"        varchar(128) NOT NULL,
    "added_date"        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER SEQUENCE "search_attempt_id_seq" OWNED BY "search_attempt"."search_attempt_id";
