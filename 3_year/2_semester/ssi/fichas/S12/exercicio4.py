#!/usr/bin/env python3
"""A deliberately vulnerable note-taking application."""

import os
import sqlite3
import sys

DB_FILE = "notes.db"


def init_db():
    """Create the notes table if it does not exist."""
    conn = sqlite3.connect(DB_FILE)
    conn.execute(
        "CREATE TABLE IF NOT EXISTS notes "
        "(id INTEGER PRIMARY KEY, title TEXT, body TEXT)"
    )
    conn.execute(
        "INSERT OR IGNORE INTO notes (id, title, body) VALUES "
        "(1, 'Welcome', 'This is your first note.'), "
        "(2, 'Reminder', 'Submit the SSI lab report on time.'), "
        "(3, 'Secret', 'The admin password is hunter2.')"
    )
    conn.commit()
    conn.close()


def search_notes(query):
    """Search notes by title: VULNERABLE to SQL injection."""
    conn = sqlite3.connect(DB_FILE)
    # CWE-89: string concatenation in SQL query
    sql = "SELECT id, title, body FROM notes WHERE title LIKE '%" + query + "%'"
    print(f"[DEBUG] Executing SQL: {sql}")
    try:
        cursor = conn.execute(sql)
        results = cursor.fetchall()
        if results:
            for row in results:
                print(f"  [{row[0]}] {row[1]}: {row[2]}")
        else:
            print("  No notes found.")
    except sqlite3.Error as e:
        print(f"  SQL error: {e}")
    conn.close()

def export_note(note_id):
    """Export a note to a file: SECURE implementation."""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.execute(
        "SELECT title, body FROM notes WHERE id=?", (note_id,)
    )
    row = cursor.fetchone()
    conn.close()

    if row is None:
        print(" Note not found.")
        return

    filename = input(" Enter filename to export to: ")
    
    clean_filename = os.path.basename(filename.strip())
    
    if not clean_filename:
        print(" Invalid filename.")
        return

    try:
        with open(clean_filename, "w") as file:
            file.write(f"Title: {row[0]}\nBody: {row[1]}\n")
        print(f" Note exported securely to {clean_filename}")
    except IOError as e:
         print(f" Error writing to file: {e}")

def main():
    init_db()
    while True:
        print("\n=== Note App ===")
        print("1. Search notes")
        print("2. Export note")
        print("3. Quit")
        choice = input("Choice: ").strip()

        if choice == "1":
            query = input("  Search query: ")
            search_notes(query)
        elif choice == "2":
            try:
                note_id = int(input("  Note ID: "))
            except ValueError:
                print("  Invalid ID.")
                continue
            export_note(note_id)
        elif choice == "3":
            break
        else:
            print("  Invalid choice.")


if __name__ == "__main__":
    main()
