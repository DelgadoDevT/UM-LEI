# Ground Control — Static Assets 📦

Central repository for all client-side assets (CSS, fonts, icons) used by the Ground Control web UI.

> Key principle: **Offline‑first** — no external CDNs. All assets are served locally by the Go HTTP server. ⚠️

---

## Table of contents
- Purpose 🎯
- Directory structure 📁
- Design & typography 🎨
- Integration examples 🛠️
- Maintenance & deployment 🚀
- Quick tests ✅

---

## Purpose 🎯

This folder holds everything the frontend needs to work without internet access:
- Icon fonts (FontAwesome)
- Local fonts (Inter, JetBrains Mono)
- Any other static assets referenced by templates

---

## Directory structure 📁

Keep this structure exactly so the Go file server resolves paths correctly:

```text
static/
├── fontawesome/           # Local FontAwesome 6 Free library
│   ├── css/               # Minified CSS (all.min.css)
│   └── webfonts/          # Icon font binaries (.woff2, .ttf)
└── fonts/                 # Custom typography files
    ├── Inter-Regular.ttf
    ├── Inter-Bold.ttf
    └── JetBrainsMono-Regular.ttf
```

---

## Design & typography 🎨

Fonts used and purpose:

- **Inter (Sans‑Serif)** — UI elements, headings, labels  
  Files: `Inter-Regular.ttf`, `Inter-Bold.ttf`  
  Reason: Legibility at small sizes.

- **JetBrains Mono (Monospace)** — telemetry, logs, numeric tables  
  File: `JetBrainsMono-Regular.ttf`  
  Reason: Fixed-width makes numbers and logs easier to read.

---

## Integration examples 🛠️

All assets are exposed via the Go HTTP file server and referenced directly in templates.

Go file server (example):
```go
// maps /static/* -> ./static/*
fs := http.FileServer(http.Dir("./static"))
http.Handle("/static/", http.StripPrefix("/static/", fs))
```

Load FontAwesome in HTML:
```html
<link rel="stylesheet" href="/static/fontawesome/css/all.min.css">
```

Local @font-face CSS example:
```css
@font-face {
  font-family: 'Inter';
  src: url('/static/fonts/Inter-Regular.ttf') format('truetype');
  font-weight: 400;
  font-style: normal;
  font-display: swap;
}
```

---

## Maintenance & deployment 🔧

- Do not rename files referenced by templates (e.g., `/static/fonts/Inter-Regular.ttf`) unless you update templates or code.
- When distributing the built binary (e.g., `go build -o ground-control`), place the `static/` folder alongside the executable. The binary does not embed these assets by default.
- Keep FontAwesome CSS and `webfonts/` in sync: the CSS references font files by relative paths.

---