//> Hash Tables table-c
#include <stdlib.h>
#include <string.h>

#include "memory.h"
#include "object.h"
#include "table.h"
#include "value.h"

//> max-load
#define TABLE_MAX_LOAD 0.75

//< max-load
void initTable(Table* table) {
  table->count = 0;
  table->capacity = 0;
  table->entries = NULL;
}
//> free-table
void freeTable(Table* table) {
  FREE_ARRAY(Entry, table->entries, table->capacity);
  initTable(table);
}
//< free-table
//> find-value-entry
//> omit
// NOTE: The "Optimization" chapter has a manual copy of this function.
// If you change it here, make sure to update that copy.
//< omit
static Entry* findEntry(
    Entry* entries,
    int capacity,
    Value key) {

  uint32_t index =
      hashValue(key) & (capacity - 1);

  Entry* tombstone = NULL;

  for (;;) {
    Entry* entry = &entries[index];

    if (IS_EMPTY(entry->key)) {
      if (IS_NIL(entry->value)) {
        return tombstone != NULL
            ? tombstone
            : entry;
      } else {
        if (tombstone == NULL) {
          tombstone = entry;
        }
      }
    } else if (valuesEqual(
                   entry->key,
                   key)) {
      return entry;
    }

    index =
        (index + 1) & (capacity - 1);
  }
}
//< find-value-entry
//> table-get-value-key
bool tableGet(
    Table* table,
    Value key,
    Value* value) {

  if (table->count == 0) return false;

  Entry* entry =
      findEntry(
          table->entries,
          table->capacity,
          key);

  if (IS_EMPTY(entry->key)) {
    return false;
  }

  *value = entry->value;
  return true;
}
//< table-get-value-key
//> table-adjust-capacity
static void adjustCapacity(Table* table, int capacity) {
  Entry* entries = ALLOCATE(Entry, capacity);
  for (int i = 0; i < capacity; i++) {
    entries[i].key = EMPTY_VAL;
    entries[i].value = NIL_VAL;
  }
//> re-hash

//> resize-init-count
  table->count = 0;
//< resize-init-count
  for (int i = 0; i < table->capacity; i++) {
    Entry* entry = &table->entries[i];
    if (IS_EMPTY(entry->key)) continue;

    Entry* dest = findEntry(entries, capacity, entry->key);
    dest->key = entry->key;
    dest->value = entry->value;
//> resize-increment-count
    table->count++;
//< resize-increment-count
  }
//< re-hash

//> Hash Tables free-old-array
  FREE_ARRAY(Entry, table->entries, table->capacity);
//< Hash Tables free-old-array
  table->entries = entries;
  table->capacity = capacity;
}
//< table-adjust-capacity
//> table-set-value-key
bool tableSet(
    Table* table,
    Value key,
    Value value) {

  if (table->count + 1 >
      table->capacity * TABLE_MAX_LOAD) {

    int capacity =
        GROW_CAPACITY(table->capacity);

    adjustCapacity(table, capacity);
  }

  Entry* entry =
      findEntry(
          table->entries,
          table->capacity,
          key);

  bool isNewKey =
      IS_EMPTY(entry->key);

  if (isNewKey &&
      IS_NIL(entry->value)) {
    table->count++;
  }

  entry->key = key;
  entry->value = value;

  return isNewKey;
}
//< table-set-value-key
//> table-delete-value-key
bool tableDelete(Table* table, Value key) {
  if (table->count == 0) return false;

  Entry* entry =
      findEntry(
          table->entries,
          table->capacity,
          key);

  if (IS_EMPTY(entry->key)) {
    return false;
  }

  // Tombstone.
  entry->key = EMPTY_VAL;
  entry->value = BOOL_VAL(true);

  return true;
}
//< table-delete-value-key
void tableAddAll(Table* from, Table* to) {
  for (int i = 0;
       i < from->capacity;
       i++) {

    Entry* entry =
        &from->entries[i];

    if (!IS_EMPTY(entry->key)) {
      tableSet(
          to,
          entry->key,
          entry->value);
    }
  }
}
//> table-find-string-value-key
ObjString* tableFindString(
    Table* table,
    const char* chars,
    int length,
    uint32_t hash) {

  if (table->count == 0) {
    return NULL;
  }

  uint32_t index =
      hash & (table->capacity - 1);

  for (;;) {
    Entry* entry =
        &table->entries[index];

    if (IS_EMPTY(entry->key)) {
      if (IS_NIL(entry->value)) {
        return NULL;
      }
    } else if (IS_STRING(entry->key)) {
      ObjString* string =
          AS_STRING(entry->key);

      if (string->length == length &&
          string->hash == hash &&
          memcmp(
              string->chars,
              chars,
              length) == 0) {

        return string;
      }
    }

    index =
        (index + 1) &
        (table->capacity - 1);
  }
}
//< table-find-string-value-key
//> remove-white-value-keys
void tableRemoveWhite(Table* table) {
  for (int i = 0;
       i < table->capacity;
       i++) {

    Entry* entry =
        &table->entries[i];

    if (!IS_EMPTY(entry->key) &&
        IS_STRING(entry->key) &&
        !AS_STRING(entry->key)
             ->obj.isMarked) {

      tableDelete(
          table,
          entry->key);
    }
  }
}
//< remove-white-value-keys
//> mark-value-keys
void markTable(Table* table) {
  for (int i = 0;
       i < table->capacity;
       i++) {

    Entry* entry =
        &table->entries[i];

    markValue(entry->key);
    markValue(entry->value);
  }
}
//< mark-value-keys
