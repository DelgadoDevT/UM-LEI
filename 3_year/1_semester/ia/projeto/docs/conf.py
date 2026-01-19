import os
import sys
import sphinx_rtd_theme

# -- Path setup --------------------------------------------------------------
# Adiciona o diretório raiz ao sys.path
sys.path.insert(0, os.path.abspath('..'))

# -- Project information -----------------------------------------------------
project = 'TaxiGreen'
copyright = '2025, Grupo 6 - IA-2526'
author = 'DelgadoDevT, PaoComPlanta, SirLordNelson, M4chad0'
release = '1.0.0'

# -- General configuration ---------------------------------------------------
extensions = [
    'sphinx.ext.autodoc',      # Gera documentação a partir de docstrings
    'sphinx.ext.viewcode',     # Adiciona links para o código fonte
    'sphinx.ext.napoleon',     # Suporte para docstrings estilo Google/NumPy
    'sphinx.ext.githubpages'   # Compatibilidade GitHub Pages
]

templates_path = ['_templates']
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']
language = 'en'

# -- Options for HTML output -------------------------------------------------
html_theme = 'sphinx_rtd_theme'

html_static_path = ['_static']

html_css_files = [
    'custom.css',
]

# Configuração LIMPA do tema Read the Docs
html_theme_options = {
    'logo_only': False,
    'prev_next_buttons_location': 'bottom',
    'style_external_links': False,
    'vcs_pageview_mode': '',
    'style_nav_header_background': '#2980B9', # Azul "TaxiGreen"
    # Opções de navegação (Isto corrige a barra lateral)
    'collapse_navigation': False,
    'sticky_navigation': True,
    'navigation_depth': 4,
    'includehidden': True,
    'titles_only': False
}