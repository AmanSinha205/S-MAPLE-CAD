# Atkinson Hyperlegible Font Instructions

The S-MAPLE design specification requires **Atkinson Hyperlegible** (SIL Open Font License) for enhanced accessibility and readability in low-vision and clinical environments.

### Font File Placement:
Place the following TrueType font files in this directory (`res/font/`):
1. `atkinson_hyperlegible_regular.ttf` (Weight: 400 Regular)
2. `atkinson_hyperlegible_bold.ttf` (Weight: 700 Bold)
3. `atkinson_hyperlegible_italic.ttf` (Optional: 400 Italic)
4. `atkinson_hyperlegible_bold_italic.ttf` (Optional: 700 Bold Italic)

### Download Source:
Official Braille Institute release:
https://www.brailleinstitute.org/freefont/
or Google Fonts:
https://fonts.google.com/specimen/Atkinson+Hyperlegible

Until TTF binaries are added to `res/font/`, the typography implementation in `org.smaple.core.designsystem.theme.Type.kt` provides graceful fallback to the Android system sans-serif font family with Atkinson Hyperlegible metrics and line-heights preserved.
