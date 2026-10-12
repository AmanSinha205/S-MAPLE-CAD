# S-MAPLE UI/UX guidelines

Applies to both Android apps: **mCard** (the patient's app) and **CAD** (the healthcare professional's app, "HP").
Built with Kotlin and Jetpack Compose.

- **Version:** 1.0, 2026-10-11
- **Visual reference:** the mockup canvas, https://claude.ai/artifact/WPepP8nw48uCPTLvwExL5u (nine boards: three mCard screens, five CAD screens, one shared-design board)
- **Status of every rule:** `proposed` until the project owner confirms it. Rules marked **[TBD]** need an answer first (see section 13).
- **Wording:** MUST means the app is wrong without it. SHOULD means follow it unless there is a recorded reason.

Where this file and the protocol or command specification disagree about card behaviour, the command specification wins. This file owns what people see, read and touch.

## Contents

1. Principles
2. Users and conditions
3. Design language
4. The token
5. Components
6. Copy and language
7. Protocol events in plain language
8. mCard app
9. CAD app
10. Accessibility checklist
11. Jetpack Compose implementation
12. Decisions log, and what this replaces
13. Open items

---

## 1. Principles

1. **One thing per screen.** Each screen has one job and at most one primary button. If a screen needs two equal choices, one of them is a quiet secondary button.
2. **The phone tells you what is happening.** Every tap, wait, success and failure is shown by the same shape (the token, section 4), a short headline and one next step.
3. **Plain words, never protocol words.** No "authenticating", "certificate", "step 2", error codes or exception text on screen. Section 7 gives the replacements.
4. **Big and calm.** Large type, large buttons, flat surfaces, a small colour set. Nothing blinks or moves unless it carries meaning.
5. **Same product, two densities.** Both apps share one theme, one token, one set of components. mCard is the larger, simpler version (patients). CAD is denser only where an HP needs it (the vitals form).
6. **Nothing is lost silently.** A note is "saved" only after it is verified on the card. An upload is "done" only after the server confirms. Failures say what is safe.
7. **Works with one hand and gloves.** Primary actions sit in the lower half. No gestures are required. No target is under 48 dp.

## 2. Users and conditions

| | mCard user (patient) | CAD user (HP) |
|---|---|---|
| Who | Anyone, including people with little schooling, low vision, or little experience with apps. Often older. May read Hindi better than English. | Nurses and doctors. Comfortable with phones. Hurried, often one-handed, sometimes gloved. |
| Goal | See my latest notes. Know my card is ready. Share my notes if I choose. | Scan a patient, record vitals, save to their card, upload later. |
| Tolerance for choices | Almost none. | Low. Fewer than three taps to start the main task. |
| Typical mistakes | Tapping the wrong thing, giving up after an error. | Tapping before the card is read, losing a half-typed form. |

Design for the mCard user first. If a pattern works for them, it works for the HP.

## 3. Design language

### 3.1 Colour

Light theme only in this phase. Do not ship a dark theme until it is designed.

| Role | Name | Hex | Use |
|---|---|---|---|
| Primary | Pine | `#14503F` | Primary buttons, token (ready, done), headings accents, active nav |
| Primary pressed | Pine deep | `#0E3A2D` | Pressed state, text on Mist chips |
| Ground | Sage | `#F2F6F1` | Screen background |
| Surface | White | `#FFFFFF` | Cards, bottom bar, input fields |
| Quiet fill | Mist | `#DCE9DE` | Chips, icon buttons, quiet highlights |
| Selected / inactive progress | Mist 2 | `#C7DCCB` | Selected nav pill, inactive step bar, token rings |
| Border | Line | `#D3E0D5` | 1 dp card and divider lines |
| Attention | Maple amber | `#F2A33A` | Something is working or waiting (token while working, badges, the "Upload" call-out) |
| Attention tint | Amber tint | `#FBE3BC` | "Waiting" chips |
| Problem | Maple red | `#B3321F` | Problems only (token, error icon) |
| Problem tint | Red tint | `#F6D9D3` | Error banners |
| Text | Ink | `#14231C` | Primary text |
| Secondary text | Moss | `#4B5E55` | Captions, helper text |

Contrast (computed from the hex values above; re-check if a value changes):

| Pair | Ratio |
|---|---|
| Ink on Sage | about 15:1 |
| White on Pine | about 9:1 |
| Moss on Sage / White | about 6.4:1 / 6.9:1 |
| Ink on Amber | about 7.7:1 |
| White on Maple red | about 6.2:1 |

Rules:
- MUST NOT convey state by colour alone. Every state also has a different icon and a different word.
- Red is rare. Never use red for decoration or for "delete".
- Amber means "something is waiting or in progress". Do not use it as a general accent.
- No gradients, no shadows. Separate surfaces with fill contrast and 1 dp borders.

### 3.2 Typography

- **Family:** Atkinson Hyperlegible (regular 400 and bold 700), designed for low vision. **Bundle the TTFs in the app** (`res/font/`, SIL Open Font License) so text never shifts or fails offline.
- **Hindi:** use the system Noto Sans Devanagari through normal font fallback. Test on a low-end device. Give Devanagari at least 1.5× line height.
- **Units:** all text in `sp`, so the system font size setting works.
- **Minimum size:** 16 sp. Nothing smaller, including captions and badges.
- **No all-caps labels.** Sentence case everywhere.

| Style | Size / line | Weight | Use |
|---|---|---|---|
| Display | 40 / 46 | Bold | The token's state word ("Ready") |
| Headline 1 | 34 / 40 | Bold | Screen titles, welcome headline |
| Headline 2 | 28 / 34 | Bold | Section headings, big counts ("3 notes waiting") |
| Title | 22 / 28 | Bold | App bar titles in flows, card titles |
| Body large | 22 / 32 | Regular | mCard hero instruction text |
| Body | 20 / 30 | Regular | Default body |
| Label | 18 to 21 / 24 to 26 | Bold | Buttons (primary 22 to 24), field labels, list titles |
| Caption | 17 to 18 / 24 | Regular | Helper text, timestamps |

Numbers that matter (vitals) use Label or Headline weight, not Caption.

### 3.3 Shape and space

- **Base unit:** 4 dp. **Screen padding:** 24 dp. **Space between groups:** 16 to 20 dp. **Between touch targets:** at least 12 dp.
- **Radii:** buttons and chips are fully round (pill). Cards 24 dp (list cards 20 dp). Input fields 16 dp. The token is the maple shape (section 4).
- **Touch targets:** minimum 48 dp. Primary buttons 64 to 72 dp tall. Secondary 56 to 64. Round icon buttons 52 to 56.
- **Bottom bar:** 96 dp tall, white, 1 dp top border.
- **Width on tablets:** content is centred, maximum width 600 dp. No two-pane layouts in this phase.
- **Orientation:** portrait only on phones.

### 3.4 Icons

- 24 dp, 2 dp stroke, round caps and joins, outlined style. Use inline vector drawables, or Material Symbols (Outlined, rounded) with the same weight.
- Icons used: home, notes (document), person, upload, check, alert (exclamation), mic, speaker, language (globe), chevron, share, back, scan, cloud, add, close, and the contactless waves.
- No emoji, ever (the old apps used them as ad hoc icons).
- Icon-only buttons MUST have a `contentDescription`. Icons next to a text label are decorative (`contentDescription = null`).
- Navigation items always show an icon **and** a text label.

### 3.5 Motion and feedback

- The token's rings pulse outward every 1.6 s while **waiting for a tap** or **working**. A state change crossfades the token's fill and symbol over 300 ms.
- MUST respect reduced motion: when the system animation scale is 0, draw the rings static. State must never depend on animation.
- No screen transitions longer than 300 ms. No bouncing, shaking or auto-advancing.
- **Haptics:** a short tick when a tap connects, a firmer "success" on done, a double pulse on a problem (use `HapticFeedbackConstants.CONFIRM` and `REJECT` on API 30 and above, with a short vibration fallback). Never the only signal.
- Messages never auto-dismiss. Problems stay until the user acts or leaves.

## 4. The token

The token is the single status indicator in both apps and the shared visual signature. It is a soft five-lobed maple shape.

### 4.1 States

| State | Fill | Symbol | Meaning | Rings |
|---|---|---|---|---|
| Ready | Pine | contactless waves, white | Waiting for a phone or card | Pulsing, Mist 2 |
| Working | Maple amber | contactless waves, Ink | Phones are talking, keep them together | Pulsing, amber |
| Done | Pine | check, white | Saved, read or checked | None |
| Problem | Maple red | exclamation, white | Something failed, try again | None |

### 4.2 Geometry

- The outline is the polar curve `r(θ) = R · (1 + 0.085 · cos(5 · (θ + 90°)))`, so one lobe points straight up. It is drawn as a closed path of about 170 points.
- In the mockup the token's component size is the full canvas including rings. The lobe's own diameter is about 61% of it. Rings are the same curve at 1.2× and 1.4× the lobe radius, drawn at 100% and 50% opacity, 3 dp stroke.
- Sizes: hero 236 dp (mCard ready screen), 188 dp (CAD home), 176 dp (CAD save), 148 dp (welcome), 92 dp (inline "card checked").

### 4.3 Behaviour

- One token per screen at most as the hero. The inline small token (CAD patient screen) is a result marker, not a status.
- The word next to the token always matches the state: "Ready", "Working", "Done", "Problem" are headline words on mCard; CAD uses the longer messages in section 7.
- The token's accessibility text and a polite live region announce each state change to TalkBack (section 10).

## 5. Components

Every component below exists once in the shared design module (section 11) and is used by both apps.

**Buttons** (all fully round, text bold)

| Kind | Look | Use |
|---|---|---|
| Primary | Pine fill, white text, 72 dp (flow actions) or 64 dp | The one main action on a screen |
| Secondary | 2 dp Pine outline, Pine text, 56 to 64 dp | Alternative action |
| Amber | Amber fill, Ink text, 52 to 56 dp | A waiting item the user can act on now ("Upload") |
| Text | No fill, Pine text, 52 dp | Tertiary ("Scan a different patient", "Listen to this page") |

A disabled primary button is Mist 2 fill with Moss text, and it MUST say why in a line of text nearby (a disabled button with no reason is not allowed).

**Chips.** Choice chips are 56 dp pills. Selected: Pine fill, white text, check icon. Unselected: white fill, 2 dp Line border. Data chips (vitals on a note) are 34 dp, Mist fill, Pine deep text, 18 sp bold. Status chips ("Waiting") are 34 dp, Amber tint fill, Ink text.

**Cards.** White, 1 dp Line border, radius 24 (20 for list cards), padding 16 to 20 dp. Never nest a card inside a card.

**Inputs.** White field, 2 dp Mist 2 border, 16 dp radius, 56 dp tall (84 dp for the notes box). Always a visible label above the field, never placeholder-only. Value text is 24 sp bold. A field's microphone button is a 44 dp Mist circle inside the field's right edge (48 dp touch area).

**Bottom bar.** Three items maximum. The active item has a Mist 2 pill behind its icon and a bold Pine label. A count badge is a small amber circle with an Ink number.

**Step bar** (CAD flow only). Three segments (Scan, Vitals, Save), 8 dp tall. Done and current segments are Pine, the rest Mist 2. The current label is bold. It appears only inside the scan-and-record flow.

**Banners.** Red tint fill, 18 dp radius, alert icon, one bold line saying what happened and one line saying what is safe or what to do. Never a toast for something the user must read.

**Lists.** Rows have a bold 21 sp title, a 18 sp Moss subtitle, a status chip at the end, 1 dp Line dividers, at least 64 dp tall.

**Loading, empty, error.** Every screen that loads data defines all three:
- *Loading:* a progress indicator and a sentence ("Getting your notes"). Never a blank screen.
- *Empty:* a sentence saying why and what happens next ("No notes yet. Notes your nurse adds will show here.").
- *Error:* a banner (above), plus a "Try again" button where retry is safe.

**No modal confirmations** for ordinary actions. The physical tap that saves a note is the confirmation. The only confirmations are for data loss: signing out with notes not yet uploaded, and leaving the vitals form after typing.

## 6. Copy and language

### 6.1 Voice

- Plain, warm, short. Aim for a school-grade-5 reading level. Sentences of 12 words or fewer, one instruction each.
- Speak to the person: "Hold your phone near your nurse's phone", not "NFC session initiated".
- Say what happened, then what to do, then what is safe. Never blame the user.
- Sentence case. No exclamation marks. No jargon, no abbreviations in mCard.
- Numbers with units written the way the person says them ("98.4°F", "72 per minute"). CAD may use the abbreviations BP, HR, RR in dense lists. **mCard MUST spell them out** ("Blood pressure", "Pulse").

### 6.2 Languages

- English and Hindi from the start. Other languages **[TBD]**.
- All text in `strings.xml` with `values-hi/`. No hard-coded strings. Use plurals for counts ("1 note", "3 notes").
- Language switch on the mCard welcome screen (top right pill showing the *other* language's name in its own script) and in Me. CAD has it in Me.
- Layouts MUST tolerate text 30% longer than English and Devanagari's taller lines.
- Dates: "Today, 10:20", "Yesterday, 4:05 pm", then "12 Sep, 9:40 am". Respect 12 or 24 hour system setting.

### 6.3 Listen button (mCard)

Every mCard screen has a speaker button at top right. It reads the screen's visible text aloud in order, using the system text-to-speech in the chosen language. Hindi voice availability varies by phone: **verify on target devices**, and if no Hindi voice is installed, say so once in plain words and offer English.

## 7. Protocol events in plain language

The apps must translate internal events. Raw protocol names, step numbers and exceptions go to logs and a debug-only screen, never to the UI.

| Internal event | Token | mCard says | CAD says | Next step |
|---|---|---|---|---|
| Waiting for a tap | Ready | "Ready. Hold the back of your phone against your nurse's phone." | "Hold your phone against the patient's phone or card." | none |
| Connected, any exchange running | Working | "Keep your phones together." | "Hold phones together. This takes a few seconds." | none |
| Mutual authentication succeeded (CAD) | Done (small) | n/a | "Card found and checked" | Add vitals |
| Note written and verified by reading it back | Done | "Your nurse added a new note." (then shows it) | "Saved to Asha's card" | Scan next patient |
| Phones moved apart | Problem | "Your phones moved apart. Hold them together again." | "Phones moved apart. Hold them together and try again." | Try again |
| Took too long | Problem | "That took too long. Hold the phones still and try again." | same | Try again |
| Authentication refused (bad or unknown credentials) | Problem | "We could not check your card. Ask your clinic for help." | "This card could not be checked. Try once more. If it keeps happening, the card may need setting up again." | Try again |
| Card refuses data because no session | Problem | n/a | "The card is locked. Scan it again." | Scan again |
| Write succeeded but read-back differs | Problem | n/a | "The note was not saved. Hold the phones together and press Save again." | Save again |
| Server unreachable | banner | "We could not reach the hospital. Your notes are safe on this phone." | "Could not reach the server. Check the internet connection, then try again. Your notes are safe on this phone." | Try again |
| Session expired on upload | banner | n/a | "Please sign in again to upload. Your notes are safe on this phone." | Sign in |
| Some notes uploaded, some not | banner | n/a | "2 of 3 notes uploaded. 1 is still waiting." | Upload again |

**Rule for retries:** whether a repeated save can create a duplicate record depends on the command specification. Until it says retry is safe, "Save again" MUST first check the card's history for the note. **[TBD, depends on spec]**

## 8. mCard app

mCard is the patient's phone acting as a card (HCE). It is always the passive party: it never reads other phones.

### 8.1 Screens

| Screen | Purpose | Mockup board |
|---|---|---|
| Welcome | Language switch, "Set up my card", "I already have a card", Listen | mCard: welcome |
| Set up (register) | One question per screen with a progress line ("Question 2 of 4"), big inputs, a Back button. Fields come from the registration flow **[TBD]** | not drawn |
| Sign in | ID, then a 6-digit PIN pad (72 dp keys, dots shown as you type, no show/hide). PIN reset **[TBD]** | not drawn |
| Home | Greeting, hero token, one instruction, the latest note card with "See all my notes" | mCard: ready to tap |
| My notes | Choose how many (5, 10, 20), "Get latest notes", the list, "Share my notes" | mCard: my notes |
| Note | One full note in large type (name of the person, date, each value on its own line) | not drawn |
| Me | Language, text help, sign out, help contact **[TBD]** | not drawn |

Bottom bar: **Home, My notes, Me.**

### 8.2 Rules

- **Home never has a control panel.** The token, one sentence, the latest note. No sync button, no settings, no transport choice.
- **During a tap** the token changes state and shows the section 7 text. When it finishes, "Your nurse added a new note." appears with the new note below. If the app is closed, the tap still works and a notification says "Your card was used". Opening it shows the latest note. **[verify HCE background behaviour on target phones]**
- **Fetching notes:** the person picks 5, 10 or 20 (default 10) and taps "Get latest notes". While loading, show a progress sentence. The list is newest first.
- **Notes list rows:** date, who wrote it, vitals as chips written out ("Blood pressure 120/80", "Pulse 72"). Tap opens the note.
- **Share my notes:** exports only the notes currently fetched, through the system share sheet. Format **[TBD]**. Directly under the button, one line: "Anyone you share with can read these notes." No modal.
- **Sign out** asks nothing unless the phone holds notes not yet synced, in which case it says so.
- **Never show** protocol words, the card's technical state, or a raw error.
- **Whole-screen rules:** at most one primary button, no gestures-only actions, no icon without a label except Listen and close/back (which have accessible names), no time-limited messages.

### 8.3 Setup and sign-in

- One question per screen. The keyboard type matches the question (numeric for age and PIN).
- The PIN pad is a custom 3×4 grid of 72 dp round keys with a backspace key. Entering six digits signs in; a wrong PIN shakes nothing and says: "That PIN is not right. Try again."
- Local data is encrypted with a key tied to the ID and PIN (decision in the architecture log, 2026-09-08). A forgotten PIN has no recovery on the phone by design: the screen says so plainly and points to the clinic **[TBD]**.

## 9. CAD app

CAD is the HP's phone acting as an NFC reader. It starts every exchange.

### 9.1 Screens

| Screen | Purpose | Mockup board |
|---|---|---|
| Sign in / register | ID and 6-digit PIN, "New here? Register" toggles the extra fields | not drawn |
| Home | "Scan patient" (hero token plus the primary button), "N notes waiting" with "Upload", "Seen today" list | CAD: home |
| Patient found | Name, age, gender, blood group, patient ID; "See past notes on this card", "Add vitals", "Scan a different patient" | CAD: patient found |
| Add vitals | The form (9.3), "Review note" | CAD: add vitals |
| Save | Summary of the note with "Edit", the amber token, "Hold phones together", "Cancel" | CAD: save to patient's phone |
| Saved | Done token, "Saved to Asha's card", "N notes waiting to upload", "Scan next patient" | not drawn |
| Past notes | The card's note history, newest first, read-only | not drawn |
| Upload | The queue, per-note status, "Upload N notes" | CAD: upload |
| Me | Name, clinic, language, sign out | not drawn |

Bottom bar: **Scan, Upload (with count badge), Me.** The bar is hidden inside the scan-and-record flow, where the step bar replaces it.

### 9.2 The flow

`Home → Scan (token waiting) → Patient found → Add vitals → Save (token waiting, second tap) → Saved → Scan next or Home`

- **There is one way to read a card:** Scan patient. Reading the history is a link on the patient screen, not another flow.
- **The step bar** (Scan, Vitals, Save) shows where the HP is. Back goes one step. Leaving the flow with typed vitals asks once: "Leave without saving this note?" with "Leave" and "Stay".
- **The second tap is the confirmation.** There is no "Are you sure?" after the summary. After the tap the app reads the note back and shows Done only if it matches (section 7).
- **The scan result is held** in memory only while the flow is open. If the app is killed, the next launch starts at Home with no half-finished state.

### 9.3 Vitals form

| Field | Input | Unit | Note |
|---|---|---|---|
| Blood pressure | Two numbers: top / bottom | mmHg | One mic covers both |
| Heart rate | Number | per minute | |
| Breathing | Number | per minute | |
| Temperature | Number | °F **[TBD: °C setting]** | |
| Medicine given | Text | | |
| Notes | Text, 2+ lines | | |

- **Every field is optional**, and the helper line says so: "Fill what you measured. Empty fields are left out." "Review note" is enabled when at least one field is filled, and when it is not, the line says "Fill at least one field."
- **Per-field voice input.** Each field has its own mic. Each mic listens only for its field. There is no master "dictate everything" sweep and no forced order.
- **Voice errors** behave identically on every field: "Didn't catch that. Try again." with the mic ready, never an automatic restart on only one field.
- **Validation** rejects only impossible input (letters in a number, negatives). Out-of-range warnings need clinical thresholds. **[TBD, do not invent thresholds.]**
- The patient's name, age and blood group stay visible at the top as a chip, read-only.
- Keep the focused field visible above the keyboard. The primary button stays reachable at 200% font size (the form scrolls).

### 9.4 Review and save

- The review is a **human-readable summary** (patient name, each filled value with its unit, medicine, notes), never the stored format.
- "Edit" returns to the form with everything intact.
- The save screen's token starts Working-amber with rings, because the app is waiting for the tap. It becomes Working while data is exchanged and Done after the verified read-back.
- On a problem the token turns red, the section 7 message appears and "Cancel" becomes "Try again" beside a secondary "Cancel".

### 9.5 Upload

- Upload is **manual**. The HP presses it when they choose, from Home or the Upload tab. Nothing uploads in the background.
- Every record written to a card is queued on the phone with status **Waiting**. A note stays on the phone until the server confirms it, then moves to "uploaded today" and is cleared later per retention policy **[TBD]**.
- States per note: Waiting, Uploading, Uploaded, Could not upload. A partial result is shown as counts ("2 of 3 uploaded") with the failed note still Waiting.
- The primary button reads "Upload N notes" and is disabled at zero, with the line "Nothing waiting."
- Failures use the red banner from section 7. They never delete anything.
- **Sign out with waiting notes** warns: "3 notes are not uploaded. Sign out anyway?" with "Stay" first and "Sign out" second. On a shared phone the signed-out HP's queue stays encrypted until they sign in again.

### 9.6 One-handed and gloved use

- Primary actions live in the lower half, full width.
- No swipe, drag or long-press anywhere. No target under 48 dp. 12 dp minimum between targets.
- Hold phones together: the instruction states which side ("the back of your phone").

## 10. Accessibility checklist

Every screen MUST pass before it ships:

- [ ] All text is in `sp`, and the screen works at 200% system font size (content scrolls, fixed bars stay reachable, buttons wrap to two lines instead of clipping).
- [ ] All colour pairs meet 4.5:1 for text under 24 sp and 3:1 for large text and icons.
- [ ] No state is colour-only.
- [ ] Every interactive element has a role and a name; icon-only buttons have `contentDescription`.
- [ ] Targets are 48 dp or larger with 12 dp between them.
- [ ] TalkBack reads the screen in a sensible order (title, token, instruction, action). The token's state changes are announced through a polite live region with its `stateDescription`.
- [ ] Focus order follows the visual order. No focus traps.
- [ ] Nothing depends on a gesture, a timer, or animation.
- [ ] Reduced motion makes rings static.
- [ ] Form fields have visible labels, correct keyboard types and error text next to the field.
- [ ] Text in Hindi has been checked for clipping.
- [ ] Switch Access and keyboard navigation reach every control.

## 11. Jetpack Compose implementation

### 11.1 Structure

- One shared Gradle library module, `:design-system`, used by both apps. It holds the theme, colour, type, shape, the token, and every component in section 5, with `@Preview`s at 100%, 130% and 200% font scale.
- Apps contain screens, view models and navigation only. Never copy a component into an app module.
- Use stable Material 3 components styled by the theme. Do not depend on alpha Material 3 Expressive APIs. The look comes from this file's tokens, not from a library default.
- Screens are single-activity with Navigation Compose. State is hoisted to view models and exposed as `StateFlow`; screens take state and lambdas.

### 11.2 Colour and type (sketch)

```kotlin
object MapleColors {
    val Pine = Color(0xFF14503F);      val PineDeep = Color(0xFF0E3A2D)
    val Sage = Color(0xFFF2F6F1);      val White = Color(0xFFFFFFFF)
    val Mist = Color(0xFFDCE9DE);      val Mist2 = Color(0xFFC7DCCB)
    val Line = Color(0xFFD3E0D5);      val Amber = Color(0xFFF2A33A)
    val AmberTint = Color(0xFFFBE3BC); val Red = Color(0xFFB3321F)
    val RedTint = Color(0xFFF6D9D3);   val Ink = Color(0xFF14231C)
    val Moss = Color(0xFF4B5E55)
}

private val MapleScheme = lightColorScheme(
    primary = MapleColors.Pine,            onPrimary = MapleColors.White,
    primaryContainer = MapleColors.Mist2,  onPrimaryContainer = MapleColors.PineDeep,
    surfaceVariant = MapleColors.Mist,     onSurfaceVariant = MapleColors.Moss,
    tertiary = MapleColors.Amber,          onTertiary = MapleColors.Ink,
    tertiaryContainer = MapleColors.AmberTint, onTertiaryContainer = MapleColors.Ink,
    error = MapleColors.Red,               onError = MapleColors.White,
    errorContainer = MapleColors.RedTint,  onErrorContainer = MapleColors.Ink,
    background = MapleColors.Sage,         onBackground = MapleColors.Ink,
    surface = MapleColors.White,           onSurface = MapleColors.Ink,
    outline = MapleColors.Line,
)

val Atkinson = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
)

// Map the section 3.2 table onto Typography, for example:
// displayMedium  = 40.sp / 46.sp Bold      headlineLarge = 34.sp / 40.sp Bold
// headlineMedium = 28.sp / 34.sp Bold      titleLarge    = 22.sp / 28.sp Bold
// bodyLarge      = 20.sp / 30.sp Regular   bodyMedium    = 18.sp / 26.sp Regular
```

Wrap both apps in a `MapleTheme { }` that sets the colour scheme, typography and `Shapes` (medium = 20 dp, large = 24 dp, extraLarge = 28 dp).

### 11.3 The token (sketch, compile and adjust)

```kotlin
enum class TokenState { Ready, Working, Done, Problem }

class MapleShape(private val lobes: Int = 5, private val amp: Float = 0.085f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val c = Offset(size.width / 2f, size.height / 2f)
        val base = size.minDimension / 2f / (1f + amp)
        val path = Path()
        val steps = 170
        for (i in 0..steps) {
            val t = 2.0 * Math.PI * i / steps
            val r = base * (1f + amp * cos(lobes * (t + Math.PI / 2)).toFloat())
            val x = c.x + r * cos(t).toFloat()
            val y = c.y + r * sin(t).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}
```

`MapleToken(state, size, description)` draws:
1. Two ring outlines at 1.2× and 1.4× the lobe radius when `state` is Ready or Working, animated with `rememberInfiniteTransition` over 1.6 s and frozen when animations are off (`Settings.Global.ANIMATOR_DURATION_SCALE == 0f`).
2. The lobe (`Modifier.clip(MapleShape())` or drawn with the same path) filled by state, crossfaded with `animateColorAsState` (300 ms).
3. The state symbol centred (`Icon`, white, or Ink on amber).
4. `Modifier.semantics { contentDescription = description; liveRegion = LiveRegionMode.Polite }`.

### 11.4 Tap state

Expose one UI state per exchange and map it to the token and text (section 7):

```kotlin
sealed interface TapUiState {
    data object Waiting : TapUiState
    data object Working : TapUiState
    data class Done(val message: UiText) : TapUiState
    data class Problem(val reason: TapProblem) : TapUiState   // no stack traces or protocol names
}
```

The NFC layer emits protocol events. A mapper in the app module turns them into `TapUiState` and a string resource id. The UI never sees protocol text. Raw events go to Logcat and the debug screen only (debug builds).

### 11.5 Practicalities

- Edge-to-edge with `WindowInsets` handled on every screen. Bottom bars and buttons respect the navigation bar inset.
- Set `Modifier.semantics` and test tags on every control for UI tests.
- Add screenshot tests for each screen in English and Hindi at 100% and 200% font scale.
- Min SDK: choose to suit HCE and Keystore key import, and record it in `docs/design-decisions.md` **[TBD]**.
- Use synthetic data in previews and tests. Never log patient data or keys in release builds.

## 12. Decisions log, and what this replaces

Format as in `ux-decisions.md`: **Decision** / **Reason** / **Status**.

**Decision:** The token is the single status indicator in both apps.
**Reason:** One consistent, glanceable signal beats text-only status; it also gives the two apps a shared identity.
**Status:** proposed.

**Decision:** Light theme only, Atkinson Hyperlegible bundled, minimum text 16 sp.
**Reason:** Legibility for low-vision and older users; no font download dependency; dark theme not yet designed.
**Status:** proposed.

**Decision:** mCard spells out medical terms; CAD may abbreviate.
**Reason:** Patients should not need to decode "BP" or "HR".
**Status:** proposed. The mockup's mCard note chips still say "BP" and "Temp" and will be corrected.

**Decision:** mCard has a language switch and a Listen button on every screen.
**Reason:** Low literacy and Hindi readers are the hardest case for mCard.
**Status:** proposed.

**Decision:** A note is shown as saved only after it is read back from the card and verified.
**Reason:** Prevents a silent failed write; consistent with "nothing is lost silently".
**Status:** proposed.

**Decision:** Upload is manual from CAD, with per-note status. Sign-out warns if notes are waiting.
**Reason:** The current flow has the HP upload when they choose; a shared device can leave a queue behind.
**Status:** proposed.

### Replaces or changes in `ux-decisions.md`

| Earlier decision | Now |
|---|---|
| mCard is a genuinely passive screen with no manual controls | **Changed.** mCard keeps a passive HCE role and a status-first home, but now also has sign-in, "Get latest notes", and "Share my notes". |
| CAD sync is fully automatic, no manual button | **Superseded.** Upload is manual and separate from writing a note. |
| Transport invisible, NFC only, automatic Bluetooth handoff | **Kept in spirit.** NFC is the only transport in the current scope, with no Bluetooth or Wi-Fi Direct in the UI. If another transport is ever added it stays invisible. |
| mCard's NFC role is fixed (always the card) | **Kept.** |
| CAD has one Scan Patient flow | **Kept.** |
| Human-readable preview before sending | **Kept** (the Review summary). |
| Voice dictation per field, no master sweep | **Kept.** |
| Second tap is the confirmation, no extra dialog | **Kept.** |
| Explicit login/logout with ID plus 6-digit PIN (replaced the stay-logged-in model) | **Kept.** |

## 13. Open items

1. Registration fields and the registration flow (what mCard asks, what the server returns).
2. PIN reset or recovery policy.
3. Temperature unit (°F, °C, or a setting).
4. Clinical thresholds for out-of-range warnings, if any.
5. Export format for "Share my notes".
6. Whether a repeated save can create a duplicate record (command specification).
7. HCE behaviour when mCard's app is closed or the phone is locked, on target phones.
8. Hindi text-to-speech availability on target phones; other languages beyond Hindi.
9. Minimum Android version.
10. Retention: how long uploaded notes stay on the CAD phone.
11. Help contact wording for mCard (who does the patient call?).
12. App icons. Proposed, not drawn: the token's lobe shape on a Pine background for mCard, and on an Ink background with amber waves for CAD.
13. Screens not yet drawn: set up, sign in, note detail, saved, past notes, Me (both apps), and the CAD sign-in.
