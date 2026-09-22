#!/usr/bin/env python3
"""
Reproduces, against a real SQLCipher build, the exact statement sequence that
DatabaseEncryption.kt uses to convert the legacy plaintext Room database:

    PRAGMA key = "x'<64 hex>'"           -- raw key, no KDF
    DROP TABLE IF EXISTS android_metadata
    ATTACH DATABASE '<plain>' AS plaintext KEY ''
    SELECT sqlcipher_export('main', 'plaintext')
    DETACH DATABASE plaintext
    PRAGMA user_version = <old version>

It builds a Room-like schema (AUTOINCREMENT, FTS4 with content=, Room's 4 content-sync triggers,
room_master_table), exports it, and checks what DatabaseEncryption.verifyEncryptedCopy() checks
(schema objects, row counts, user_version, quick_check) plus FTS search, triggers and key handling.

    pip install sqlcipher3-binary
    python3 tools/verify_sqlcipher_export.py
"""
import os
import secrets
import sqlite3
import sys
import tempfile

import sqlcipher3


def snapshot(query):
    objects, tables = set(), []
    for typ, name in query("SELECT type, name FROM sqlite_master "
                           "WHERE name NOT LIKE 'sqlite_%' AND name != 'android_metadata'"):
        objects.add(f"{typ}:{name}")
        if typ == "table":
            tables.append(name)
    return objects, {t: query(f'SELECT COUNT(*) FROM "{t}"')[0][0] for t in tables}


def main() -> int:
    failures = []

    def check(label, ok):
        print(("PASS  " if ok else "FAIL  ") + label)
        if not ok:
            failures.append(label)

    with tempfile.TemporaryDirectory() as tmpdir:
        plain_path = os.path.join(tmpdir, "db")
        tmp_path = plain_path + ".enc.tmp"
        key = f"x'{secrets.token_hex(32)}'"

        p = sqlite3.connect(plain_path)
        p.execute("PRAGMA journal_mode=WAL")
        p.executescript("""
        CREATE TABLE android_metadata (locale TEXT);
        INSERT INTO android_metadata VALUES ('en_US');
        CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT);
        INSERT INTO room_master_table VALUES (42, 'abcdef123456');
        CREATE TABLE `messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `threadId` INTEGER NOT NULL,
            `address` TEXT NOT NULL, `body` TEXT NOT NULL);
        CREATE INDEX `index_messages_threadId` ON `messages` (`threadId`);
        CREATE VIRTUAL TABLE `messages_fts` USING FTS4(`body` TEXT NOT NULL, `address` TEXT NOT NULL, content=`messages`);
        CREATE TRIGGER room_fts_content_sync_messages_fts_BEFORE_UPDATE BEFORE UPDATE ON `messages` BEGIN DELETE FROM `messages_fts` WHERE `docid`=OLD.`rowid`; END;
        CREATE TRIGGER room_fts_content_sync_messages_fts_BEFORE_DELETE BEFORE DELETE ON `messages` BEGIN DELETE FROM `messages_fts` WHERE `docid`=OLD.`rowid`; END;
        CREATE TRIGGER room_fts_content_sync_messages_fts_AFTER_UPDATE AFTER UPDATE ON `messages` BEGIN INSERT INTO `messages_fts`(`docid`, `body`, `address`) VALUES (NEW.`rowid`, NEW.`body`, NEW.`address`); END;
        CREATE TRIGGER room_fts_content_sync_messages_fts_AFTER_INSERT AFTER INSERT ON `messages` BEGIN INSERT INTO `messages_fts`(`docid`, `body`, `address`) VALUES (NEW.`rowid`, NEW.`body`, NEW.`address`); END;
        """)
        p.executemany("INSERT INTO messages (threadId, address, body) VALUES (?,?,?)",
                      [(i % 7, f"ADDR{i % 13}", f"Your invoice {i} is ready - \u0633\u0644\u0627\u0645") for i in range(1, 5001)])
        p.execute("PRAGMA user_version = 30")
        p.commit()

        p.execute("PRAGMA wal_checkpoint(TRUNCATE)")
        version = p.execute("PRAGMA user_version").fetchone()[0]
        plain_snap = snapshot(lambda q: p.execute(q).fetchall())
        p.close()

        e = sqlcipher3.connect(tmp_path)
        e.execute(f'PRAGMA key = "{key}"')
        e.execute("PRAGMA user_version = 1")  # what SQLiteOpenHelper does for a brand-new file
        e.execute("DROP TABLE IF EXISTS android_metadata")
        e.execute(f"ATTACH DATABASE '{plain_path}' AS plaintext KEY ''")
        e.execute("SELECT sqlcipher_export('main', 'plaintext')").fetchall()
        e.execute("DETACH DATABASE plaintext")
        e.execute(f"PRAGMA user_version = {version}")
        e.commit()
        e.close()

        check("exported file no longer has the plain SQLite header", open(tmp_path, "rb").read(16) != b"SQLite format 3\x00")

        v = sqlcipher3.connect(tmp_path)
        v.execute(f'PRAGMA key = "{key}"')
        check("user_version carried over", v.execute("PRAGMA user_version").fetchone()[0] == version)
        check("quick_check ok", v.execute("PRAGMA quick_check(1)").fetchone()[0] == "ok")
        enc_snap = snapshot(lambda q: v.execute(q).fetchall())
        check("schema objects identical (tables, FTS shadow tables, indexes, triggers)", enc_snap[0] == plain_snap[0])
        check("row counts identical in every table", enc_snap[1] == plain_snap[1])
        check("room_master_table identity hash kept",
              v.execute("SELECT identity_hash FROM room_master_table WHERE id=42").fetchone() == ("abcdef123456",))
        check("FTS search finds all 5000 rows",
              v.execute("SELECT COUNT(*) FROM messages_fts WHERE messages_fts MATCH 'invoice'").fetchone()[0] == 5000)
        check("FTS search works for non-Latin text",
              v.execute("SELECT COUNT(*) FROM messages_fts WHERE messages_fts MATCH ?", ("\u0633\u0644\u0627\u0645",)).fetchone()[0] == 5000)
        check("all 4 Room FTS triggers preserved",
              v.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='trigger'").fetchone()[0] == 4)
        v.execute("INSERT INTO messages (threadId, address, body) VALUES (1,'X','brand new needle')")
        v.execute("UPDATE messages SET body = 'updated haystack' WHERE address='X'")
        check("AUTOINCREMENT continues after the old maximum id",
              v.execute("SELECT max(id) FROM messages").fetchone()[0] == 5001)
        check("triggers keep FTS in sync after the export",
              v.execute("SELECT COUNT(*) FROM messages_fts WHERE messages_fts MATCH 'haystack'").fetchone()[0] == 1
              and v.execute("SELECT COUNT(*) FROM messages_fts WHERE messages_fts MATCH 'needle'").fetchone()[0] == 0)
        v.commit()
        v.close()

        w = sqlcipher3.connect(tmp_path)
        w.execute("PRAGMA key = \"x'" + "00" * 32 + "'\"")
        try:
            w.execute("SELECT count(*) FROM sqlite_master").fetchone()
            check("a wrong key is rejected", False)
        except Exception:
            check("a wrong key is rejected", True)

    print("\nSQLCipher", "OK" if not failures else f"FAILED: {failures}")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
