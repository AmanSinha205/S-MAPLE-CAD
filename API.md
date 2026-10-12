# S-MAPLE card command specification, v1

Applies to the Java Card applet and to the Android mCard app. **Both MUST answer every command with the same bytes and status words.** Written for Phases 1 and 2 (commands in the clear). Phase 3 wraps the same commands in the secure channel without changing them.

Reference codec and self-tests: `tools/reference_codec.py`. The vectors in section 8 are produced by it. The shared Kotlin codec used by the CAD and mCard app MUST reproduce them exactly.

## 1. Conventions

- All multi-byte numbers are big-endian. CLA is `80` for every command.
- Short APDUs only (no extended length). Commands with data carry at most 180 data bytes. Responses carry at most 200 data bytes.
- Text is printable ASCII, `20` to `7E`. No other characters, anywhere.
- The card stores records as **opaque blobs**. It checks length and one version field, nothing else. Encoding and decoding happen only in the apps.
- The card has no clock. Timestamps come from the CAD phone.

## 2. Commands

| INS | Name | Case | Command data | Response data | Phase |
|---|---|---|---|---|---|
| `00` | GET_INFO | 2 | none | 11 bytes (4.1) | 1 |
| `02` | GET_BASIC_INFO | 2 | none | basic info block, at most 58 bytes (3) | 1 |
| `10` | ADD_RECORD | 4 | record blob, 1 to 180 bytes | 3 bytes (4.2) | 2 |
| `12` | GET_RECORDS | 2 | none, P1 P2 = start index | page, at most 200 bytes (4.3) | 2 |
| `E0` | DEV_WRITE_BASIC_INFO | 3 | basic info block | none | 1, dev only |

INS `E0` to `EF` are dev-only and are removed in Phase 4. Any other INS returns `6D00`; a CLA other than `80` returns `6E00`. After Phase 3, every command except SELECT and the authentication messages returns `6982` until a session exists.

## 3. Basic info block (written once, never changed)

| Offset | Size | Field |
|---|---|---|
| 0 | 1 | Format: `10` (version 1 in the high nibble, low nibble 0) |
| 1 | 1 | bits 7-6 gender, bits 5-2 blood group, bits 1-0 zero |
| 2 | 2 | Date of birth, days since 1900-01-01 (uint16, valid to 2079-06-05) |
| 4 | 1 + n | Patient ID: length (1 to 12) then ASCII |
| 5+n | 1 + m | Name: length (1 to 40) then ASCII |

Total at most 58 bytes. Age is computed by the apps from the date of birth.

Gender: `0` male, `1` female, `2` non-binary, `3` not stated.
Blood group: `0` unknown, `1` A+, `2` A-, `3` B+, `4` B-, `5` AB+, `6` AB-, `7` O+, `8` O-.

The applet validates this block **completely** on write, because it can never be changed: format byte, reserved bits zero, gender and blood codes in range, both lengths in range and consistent with Lc, all text printable ASCII. Any failure returns `6A80` and writes nothing.

## 4. Record blob and commands

### 4.0 Record layout (at most 180 bytes)

| Part | Size | Content |
|---|---|---|
| Header | 1 | bits 7-5 format version (`001`), bits 4-0 presence flags: `10` BP, `08` temperature, `04` heart rate, `02` medications, `01` description |
| Timestamp | 3 | UTC minutes since 2025-01-01 00:00 (uint24, valid to 2056-12) |
| HP ID | 1 + n | length (1 to 8) then ASCII. Always present |
| BP | 2 | systolic minus 40, diastolic minus 20 (ranges 40 to 295 and 20 to 275) |
| Temperature | 1 | tenths of a degree Celsius minus 300 (30.0 to 55.5 C) |
| Heart rate | 1 | beats per minute, 0 to 255 |
| Medications | 1 + n | length (1 to 60) then ASCII; items separated by `;`; no item empty or containing `;` |
| Description | 1 + n | length (1 to 100) then ASCII |

Fields appear in this order. A field is present only if its flag is set; an absent field takes no bytes. At least one flag MUST be set. Temperature is always stored in Celsius; the display unit is a UI setting. Worst case is 179 bytes. The CAD refuses input outside these ranges.

### 4.1 GET_INFO (`80 00 00 00 00`)

Response data, 11 bytes:

| Offset | Size | Field |
|---|---|---|
| 0 | 1 | API version, `01` |
| 1 | 1 | Record format version, `01` |
| 2 | 2 | Record count |
| 4 | 2 | Maximum record count (`000A` on the card; the Android mCard reports its own limit) |
| 6 | 2 | Ring size in bytes (`0800` on the card) |
| 8 | 2 | Free bytes = ring size minus the sum of (1 + length) of stored records |
| 10 | 1 | bit 0: basic info written |

### 4.2 ADD_RECORD (`80 10 00 00 Lc blob 00`)

- `Lc` must be 1 to 180, otherwise `6700`. The blob's first byte must have version bits `001`, otherwise `6A80`. The card checks nothing else.
- If the blob is byte-for-byte identical to the newest stored record, nothing changes and the result is `01` (already stored). Otherwise the record becomes the newest, and while the count exceeds the maximum (or the ring cannot hold it) the oldest records are dropped. The result is `00`.
- The change is atomic: after a power loss the card holds either the old state or the new one.
- Response data, 3 bytes: result (`00` stored, `01` already stored), record count (2).

The CAD checks a save by calling GET_RECORDS with start 0 and comparing the first record to what it sent. If a tap is lost mid-save, the CAD may send the same blob again: an identical newest record answers `01`.

### 4.3 GET_RECORDS (`80 12 P1 P2 00`)

`P1 P2` is the start index as a uint16 (0 is the newest record). The card returns as many whole records as fit in 200 data bytes, newest first. Response data:

| Offset | Size | Field |
|---|---|---|
| 0 | 2 | Total record count |
| 2 | 2 | Records remaining after this page |
| 4 | 1 | Records in this page (n) |
| 5 | | n entries of: length (1 byte) then the record blob |

A page always holds at least one record when the start index is valid (each entry is at most 180 bytes). A start index at or beyond the count returns count, `0000`, `00` and status `9000`. The client loops, setting start to the previous start plus n, until remaining is 0.

### 4.4 DEV_WRITE_BASIC_INFO (`80 E0 00 00 Lc block`)

Writes the basic info block. Allowed once. A second attempt returns `6985` and changes nothing. Validation is in section 3.

## 5. Status words

| SW | Meaning |
|---|---|
| `9000` | Success |
| `6700` | Wrong length |
| `6982` | No secure session (from Phase 3) |
| `6985` | Conditions not satisfied (basic info already written) |
| `6A80` | Incorrect data (bad version, bad basic info) |
| `6A86` | Wrong P1/P2 (non-zero P1 P2 on a command that takes none) |
| `6A88` | Data not found (GET_BASIC_INFO before it is written) |
| `6D00` | INS not supported |
| `6E00` | CLA not supported |

## 6. Storage on the card (applet only)

How the applet lays out memory is its own business. These constants are fixed: maximum count 10, ring 2,048 bytes (10 records of 180 bytes plus one length byte each need 1,810), blob at most 180 bytes. A stored record costs its length plus 1 byte. The Android mCard reports its own maximum count and ring size in GET_INFO and otherwise behaves identically.

## 7. Cloud upload

The CAD decodes blobs and uploads the fields as JSON through the REST API. Draft names, **to be reconciled with the server repo**:

| Field | JSON name (placeholder) |
|---|---|
| timestamp | `recordedAt`, ISO-8601 UTC (for example `2026-10-11T10:20:00Z`) |
| HP ID | `hpId` |
| BP | `bpSystolic`, `bpDiastolic` |
| temperature | `temperatureC` |
| heart rate | `heartRateBpm` |
| medications | `medications`, array of strings |
| description | `description` |

## 8. Test vectors

All hex. Source of truth: `tools/reference_codec.py`.

**V1**, 2026-10-11 10:20 UTC, HP `N101`, BP 120/80, 37.0 C, heart rate 80, description "Fever 2 days" (26 bytes). Timestamp is 933740 minutes (`0E 3F 6C`).
`3D 0E 3F 6C 04 4E 31 30 31 50 3C 46 50 0C 46 65 76 65 72 20 32 20 64 61 79 73`

**V2**, 2026-10-11 09:45 UTC, HP `N101`, heart rate 72 only (10 bytes):
`24 0E 3F 49 04 4E 31 30 31 48`

**V3**, 2026-10-10 16:05 UTC, HP `N102`, BP 128/84, 36.9 C, heart rate 76, medications "Paracetamol 500 mg" and "ORS", description "Cough, advised rest. Review in 3 days" (74 bytes):
`3F 0E 3B 25 04 4E 31 30 32 58 40 45 4C 16 50 61 72 61 63 65 74 61 6D 6F 6C 20 35 30 30 20 6D 67 3B 4F 52 53 25 43 6F 75 67 68 2C 20 61 64 76 69 73 65 64 20 72 65 73 74 2E 20 52 65 76 69 65 77 20 69 6E 20 33 20 64 61 79 73`

**V4 (maximum, 179 bytes)**, 2026-01-01 00:00 UTC, HP `N1010101`, BP 295/275, 55.5 C, heart rate 255, medications 60 times `m`, description 100 times `d`. First 12 bytes: `3F 08 05 20 08 4E 31 30 31 30 31 30`

**Basic info**, Asha Verma, female, B+, born 1972-05-20 (26,437 days), patient ID `AV-2041` (23 bytes):
`10 4C 67 45 07 41 56 2D 32 30 34 31 0A 41 73 68 61 20 56 65 72 6D 61`

### APDUs

- GET_INFO on a fresh card: `80 00 00 00 00` returns `01 01 00 00 00 0A 08 00 08 00 00` then `90 00`.
- DEV_WRITE_BASIC_INFO: `80 E0 00 00 17 10 4C 67 45 07 41 56 2D 32 30 34 31 0A 41 73 68 61 20 56 65 72 6D 61` returns `90 00`. The same command again returns `69 85`.
- GET_BASIC_INFO: `80 02 00 00 00` returns the basic info block above, then `90 00`.
- ADD_RECORD with V1 on an empty card: `80 10 00 00 1A 3D 0E 3F 6C 04 4E 31 30 31 50 3C 46 50 0C 46 65 76 65 72 20 32 20 64 61 79 73 00` returns `00 00 01 90 00`. Sending it again returns `01 00 01 90 00`.
- After adding V3, V2, V1 in that order (V3 oldest), GET_INFO (basic info written) returns `01 01 00 03 00 0A 08 00 07 8F 01` then `90 00`. Free bytes `078F` = 2048 - (75 + 11 + 27).
- GET_RECORDS start 0 (`80 12 00 00 00`) returns, then `90 00`:
  `00 03 00 00 03 1A 3D 0E 3F 6C 04 4E 31 30 31 50 3C 46 50 0C 46 65 76 65 72 20 32 20 64 61 79 73 0A 24 0E 3F 49 04 4E 31 30 31 48 4A 3F 0E 3B 25 04 4E 31 30 32 58 40 45 4C 16 50 61 72 61 63 65 74 61 6D 6F 6C 20 35 30 30 20 6D 67 3B 4F 52 53 25 43 6F 75 67 68 2C 20 61 64 76 69 73 65 64 20 72 65 73 74 2E 20 52 65 76 69 65 77 20 69 6E 20 33 20 64 61 79 73`
- GET_RECORDS start 2 (`80 12 00 02 00`) returns only V3:
  `00 03 00 00 01 4A 3F 0E 3B 25 04 4E 31 30 32 58 40 45 4C 16 50 61 72 61 63 65 74 61 6D 6F 6C 20 35 30 30 20 6D 67 3B 4F 52 53 25 43 6F 75 67 68 2C 20 61 64 76 69 73 65 64 20 72 65 73 74 2E 20 52 65 76 69 65 77 20 69 6E 20 33 20 64 61 79 73`
- GET_RECORDS start 5 returns `00 03 00 00 00`

### Eviction test

Add 12 distinct records R1 to R12 (for example, heart rate 60 to 71 at one-minute steps). Then GET_INFO reports count `000A`, GET_RECORDS start 0 begins with R12 and the last record in the full read is R3, and re-sending R12 returns result `01`.

## 9. Changes since the earlier proposal

- GET_RECORDS no longer takes a maximum count: the card fills the page, and the client uses what it needs.
- Counts are 16 bits so capacity can grow beyond 255 later.
- The earlier sample record used a wrong header byte (`17`). The correct header for that record is `3D`, as in V1.
- GET_INFO gained a basic-info-written flag.

## 10. Open items

1. Clinical sign-off on the encodable ranges: systolic 40 to 295, diastolic 20 to 275, temperature 30.0 to 55.5 C, heart rate 0 to 255.
2. The 180-byte maximum blob and 200-byte page assume secure-messaging overhead (MAC and padding) fits in a 255-byte APDU. Phase 3 confirms or shrinks them.
3. JSON field names must be reconciled with the server's REST API.
4. The Android mCard's maximum count and ring size (it may keep more than 10).
5. Identical consecutive records are treated as duplicates. Two genuinely identical notes in the same minute from the same HP would collapse into one; this is accepted.
6. The CAD phone clock is trusted for timestamps.
