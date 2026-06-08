"""Sphinx configuration for Secure Chat documentation."""

from __future__ import annotations

import os
import sys

try:
    import sphinx_rtd_theme  # noqa: F401
except ModuleNotFoundError:
    sphinx_rtd_theme = None

# -- Path setup --------------------------------------------------------------
PROJECT_ROOT = os.path.abspath("..")
if PROJECT_ROOT not in sys.path:
    sys.path.insert(0, PROJECT_ROOT)

# -- Project information -----------------------------------------------------
project = "Secure Chat"
copyright = "2026, DelgadoDevT, PaoComPlanta, yHugoSoares"
author = "DelgadoDevT, PaoComPlanta, yHugoSoares"
release = "0.1.0"

# -- General configuration ---------------------------------------------------
extensions = [
    "sphinx.ext.autodoc",
    "sphinx.ext.napoleon",
    "sphinx.ext.viewcode",
    "sphinx.ext.githubpages",
]

autodoc_typehints = "description"
autodoc_member_order = "bysource"
autodoc_default_options = {
    "ignore-module-all": True,
}

templates_path = ["_templates"]
exclude_patterns: list[str] = [
    "_build",
    "Thumbs.db",
    ".DS_Store",
    "_api/modules.rst",
    "_api_backup",
]

html_title = "Secure Chat E2EE Documentation"
html_static_path = ["_static"]
html_css_files = ["custom.css"]

# -- Options for HTML output -------------------------------------------------
if sphinx_rtd_theme is not None:
    html_theme = "sphinx_rtd_theme"
    html_theme_options = {
        "logo_only": False,
        "prev_next_buttons_location": "bottom",
        "style_external_links": False,
        "vcs_pageview_mode": "",
        # Configuração LIMPA do tema Read the Docs proveniente do IA-2526
        "style_nav_header_background": "#2563eb", # Azul "Secure Chat"
        # Opções de navegação (Isto corrige a barra lateral)
        "collapse_navigation": False,
        "sticky_navigation": True,
        "navigation_depth": 4,
        "includehidden": True,
        "titles_only": False,
    }
else:
    html_theme = "alabaster"
    html_theme_options = {}
