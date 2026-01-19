#!/bin/bash

# Criar estrutura de pastas
echo "📂 A criar pastas..."
mkdir -p static/fonts
mkdir -p static/fontawesome/css
mkdir -p static/fontawesome/webfonts

# --- 1. FONTS (Google Fonts via GitHub Releases) ---

# Inter Font
echo "⬇️  A descarregar fonte Inter..."
curl -L -o inter.zip https://github.com/rsms/inter/releases/download/v3.19/Inter-3.19.zip
unzip -j inter.zip "Inter Hinted for Windows/Desktop/Inter-Regular.ttf" -d static/fonts/
unzip -j inter.zip "Inter Hinted for Windows/Desktop/Inter-Bold.ttf" -d static/fonts/
rm inter.zip

# JetBrains Mono
echo "⬇️  A descarregar fonte JetBrains Mono..."
curl -L -o jbmono.zip https://github.com/JetBrains/JetBrainsMono/releases/download/v2.304/JetBrainsMono-2.304.zip
unzip -j jbmono.zip "fonts/ttf/JetBrainsMono-Regular.ttf" -d static/fonts/
rm jbmono.zip

# --- 2. FONTAWESOME (Via CDN para simplificar) ---
# Descarregamos apenas os ficheiros essenciais para a versão 6.4.0

echo "⬇️  A descarregar FontAwesome 6.4.0..."
BASE_URL="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0"

# CSS
curl -s -o static/fontawesome/css/all.min.css "$BASE_URL/css/all.min.css"

# Webfonts (Solid e Regular são os mais usados)
curl -s -o static/fontawesome/webfonts/fa-solid-900.woff2 "$BASE_URL/webfonts/fa-solid-900.woff2"
curl -s -o static/fontawesome/webfonts/fa-solid-900.ttf "$BASE_URL/webfonts/fa-solid-900.ttf"
curl -s -o static/fontawesome/webfonts/fa-regular-400.woff2 "$BASE_URL/webfonts/fa-regular-400.woff2"
curl -s -o static/fontawesome/webfonts/fa-regular-400.ttf "$BASE_URL/webfonts/fa-regular-400.ttf"

echo "✅ Concluído! A pasta 'static' está pronta para uso offline."
