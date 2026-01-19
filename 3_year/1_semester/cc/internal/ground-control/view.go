package groundcontrol

import (
	"fmt"
	"html/template"
	"log"
	"net/http"

	"cc.2526/internal/common"
)

// =================================================================
// 🎨 VIEW ENGINE & TEMPLATES
// =================================================================

// RenderTemplate parses the internal HTML string and executes it with the provided data.
// It writes the resulting HTML directly to the HTTP ResponseWriter.
// If parsing fails, it returns a 500 Internal Server Error.
func RenderTemplate(w http.ResponseWriter, data PageData) {
	// 'template.New' creates a new template namespace.
	// 'Parse' analyses the HTML string and checks for syntax errors in the {{}} tags.
	tmpl, err := template.New("index").Parse(htmlTemplate)
	if err != nil {
		http.Error(w, "Template Error", 500)
		log.Printf("Template error: %v", err)
		return
	}
	// 'Execute' merges the data struct with the HTML template.
	tmpl.Execute(w, data)
}

// =================================================================
// 💄 VISUAL HELPERS (Presentation Logic)
// =================================================================
// These functions contain the "Business Logic for Display".
// They determine which visual cues (colors, icons, badges) represent specific states.

// getStatusString maps numeric status codes to human-readable strings with emoji icons.
// This decouples the internal status IDs (uint8) from their visual representation.
func getStatusString(status uint8) string {
	switch status {
	case common.IDLE:
		return "🟢 STANDBY"
	case common.MAPPING:
		return "🗺️ MAPPING"
	case common.ENVIRONMENTAL:
		return "🌡️ ENV. STUDY"
	case common.RECONNAISANCE:
		return "📸 RECON OP"
	case common.DIAGNOSTIC:
		return "🔧 DIAGNOSTIC"
	case common.ANALYSIS:
		return "🧪 ANALYSIS"
	case common.ERROR:
		return "🔴 ERROR STATE"
	default:
		return "❓ UNKNOWN"
	}
}

// getBatteryClass returns a Bootstrap/Tailwind-style utility class based on battery percentage.
//   - 🟢 > 70%: Green (Success)
//   - 🟠 30% - 70%: Orange (Warning)
//   - 🔴 < 30%: Red (Danger)
func getBatteryClass(battery uint8) string {
	if battery > 70 {
		return "text-success"
	} else if battery > 30 {
		return "text-warning"
	}
	return "text-danger"
}

// getHealthClass provides visual feedback for system health integrity.
// Stricter thresholds are used here compared to battery (80% and 60%).
func getHealthClass(health uint8) string {
	if health > 80 {
		return "text-success"
	} else if health > 60 {
		return "text-warning"
	}
	return "text-danger"
}

// getMissionStateClass returns specific CSS badge classes for mission statuses.
// These classes map to styles defined in the HTML <style> block (e.g., .badge-running).
func getMissionStateClass(state string) string {
	switch state {
	case "EM_PROGRESSO":
		return "badge-running" // Yellow/Orange pulse effect
	case "CONCLUIDA":
		return "badge-success" // Green
	case "FALHADA":
		return "badge-failed" // Red
	case "INTERROMPIDA":
		return "badge-failed" // Red
	default:
		return "badge-neutral" // Grey
	}
}

// formatUptime converts a raw count of seconds into a human-readable "HHh MMm SSs" string.
// It uses integer division and modulo operations to break down the time components.
func formatUptime(uptime uint64) string {
	hours := uptime / 3600
	minutes := (uptime % 3600) / 60
	seconds := uptime % 60
	return fmt.Sprintf("%02dh %02dm %02ds", hours, minutes, seconds)
}

// --- HTML Template ---
// Contains the full Single Page Application (SPA) structure.
// NOTE: Configured to use local assets from the ./static directory for offline capability.
// It includes embedded JavaScript for real-time updates (AJAX polling) and UI interactions.
const htmlTemplate = `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{.Title}}</title>
    <link rel="icon" href="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48dGV4dCB5PSIuOWVtIiBmb250LXNpemU9IjkwIj7wn5qAPC90ZXh0Pjwvc3ZnPg==">
    
    <link rel="stylesheet" href="/static/fontawesome/css/all.min.css">

    <style>
        /* LOCAL FONTS DEFINITION */
        /* Ensure .ttf/.woff2 files exist in /static/fonts/ */
        @font-face {
            font-family: 'Inter';
            src: url('/static/fonts/Inter-Regular.ttf') format('truetype');
            font-weight: 400;
            font-style: normal;
        }
        @font-face {
            font-family: 'Inter';
            src: url('/static/fonts/Inter-Bold.ttf') format('truetype');
            font-weight: 700;
            font-style: normal;
        }
        @font-face {
            font-family: 'JetBrains Mono';
            src: url('/static/fonts/JetBrainsMono-Regular.ttf') format('truetype');
            font-weight: 400;
            font-style: normal;
        }

        :root {
            --bg-app: #0b0c10;
            --bg-panel: #15161b;
            --bg-card: #1e1f26;
            --accent: #6366f1;
            --accent-bright: #818cf8;
            --border: 1px solid rgba(255, 255, 255, 0.08);
            --success: #10b981;
            --warning: #f59e0b;
            --danger: #ef4444;
            --text-muted: #9ca3af;
            --text-main: #f3f4f6;
            --radius: 12px;
            --shadow-header: 0 4px 30px rgba(0, 0, 0, 0.5);
        }

        body {
            margin: 0;
            font-family: 'Inter', sans-serif; /* Uses original font */
            background-color: var(--bg-app);
            color: var(--text-main);
            height: 100vh;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            background-image: radial-gradient(circle at 50% -20%, #1e1b4b 0%, #0b0c10 60%);
        }

        /* Header */
        .header {
            height: 68px; padding: 0 32px; display: flex; align-items: center; justify-content: space-between;
            background: rgba(15, 16, 20, 0.85); backdrop-filter: blur(16px); border-bottom: 1px solid rgba(255,255,255,0.05);
            box-shadow: var(--shadow-header); z-index: 100; position: relative; flex-shrink: 0;
        }
        .header::after {
            content: ''; position: absolute; bottom: 0; left: 0; width: 100%; height: 1px;
            background: linear-gradient(90deg, transparent 0%, var(--accent) 50%, transparent 100%);
            opacity: 0.5;
        }
        .brand { font-size: 1.4rem; font-weight: 700; letter-spacing: -0.5px; display: flex; align-items: center; gap: 14px; color: #fff; text-shadow: 0 0 20px rgba(99, 102, 241, 0.3); }
        .brand-icon { color: var(--accent-bright); background: rgba(99, 102, 241, 0.1); width: 38px; height: 38px; border-radius: 10px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(99, 102, 241, 0.2); transition: all 0.3s ease; }
        .brand:hover .brand-icon { transform: rotate(-15deg) scale(1.05); box-shadow: 0 0 15px rgba(99, 102, 241, 0.4); border-color: var(--accent); color: #fff; }
        .status-indicators { display: flex; gap: 16px; }
        .pill { background: rgba(20, 20, 25, 0.6); padding: 8px 16px; border-radius: 99px; font-size: 0.85rem; display: flex; align-items: center; gap: 10px; border: 1px solid rgba(255,255,255,0.08); font-weight: 500; color: #d1d5db; transition: all 0.2s; box-shadow: inset 0 1px 0 rgba(255,255,255,0.05); }
        .pill:hover { background: rgba(30, 30, 40, 0.8); border-color: rgba(255,255,255,0.2); color: #fff; transform: translateY(-1px); }
        .pill i { color: var(--accent-bright); font-size: 0.9rem; }
        .live-pill { border-color: rgba(16, 185, 129, 0.3); background: rgba(16, 185, 129, 0.05); color: #6ee7b7; }
        .live-dot { width: 8px; height: 8px; background: #10b981; border-radius: 50%; box-shadow: 0 0 8px #10b981; animation: pulse 2s infinite; }

        /* Layout */
        .dashboard { display: grid; grid-template-columns: 1fr 400px; gap: 24px; padding: 24px 32px; height: calc(100vh - 68px); overflow: hidden; }
        .column { display: flex; flex-direction: column; gap: 20px; overflow-y: auto; padding-right: 6px; min-height: 0; }

        /* Cards */
        .panel { background: var(--bg-panel); border: var(--border); border-radius: var(--radius); display: flex; flex-direction: column; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.2); flex-shrink: 0; }
        .panel-head { padding: 18px 24px; background: rgba(255,255,255,0.02); border-bottom: var(--border); font-size: 0.8rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 1px; display: flex; justify-content: space-between; align-items: center; }

        .rover-card { background: linear-gradient(180deg, #1e1f26 0%, #18181b 100%); border: var(--border); border-radius: var(--radius); overflow: hidden; margin-bottom: 20px; transition: transform 0.2s, border-color 0.2s; position: relative; flex-shrink: 0; }
        .rover-card:hover { border-color: var(--accent); transform: translateY(-2px); box-shadow: 0 10px 30px -10px rgba(0,0,0,0.5); }
        .rover-card::before { content: ''; position: absolute; top: 0; bottom: 0; left: 0; width: 4px; background: linear-gradient(to bottom, var(--accent), transparent); opacity: 0.8; }
        .rc-header { padding: 20px; background: rgba(99, 102, 241, 0.03); border-bottom: var(--border); display: flex; justify-content: space-between; align-items: center; }
        .rc-title { font-size: 1.1rem; font-weight: 700; color: #fff; }
        .rc-id { font-family: 'JetBrains Mono', monospace; font-size: 0.7rem; background: rgba(255,255,255,0.1); padding: 3px 8px; border-radius: 6px; color: #ccc; }

        .sensor-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1px; background: rgba(255,255,255,0.05); border-bottom: var(--border); }
        @media (max-width: 1400px) { .sensor-grid { grid-template-columns: repeat(2, 1fr); } .span-pos { grid-column: span 2 !important; } }
        @media (max-width: 600px) { .sensor-grid { grid-template-columns: 1fr; } .span-pos { grid-column: span 1 !important; } }
        .sensor-box { background: var(--bg-card); padding: 16px; display: flex; flex-direction: column; gap: 6px; transition: background 0.2s; min-width: 0; }
        .sensor-box:hover { background: #23232a; }
        .sb-label { font-size: 0.65rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px; display: flex; align-items: center; gap: 6px; white-space: nowrap; }
        .sb-label i { color: var(--accent-bright); opacity: 0.7; }
        .sb-value { font-family: 'JetBrains Mono', monospace; font-size: 0.95rem; font-weight: 500; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .sb-unit { font-size: 0.7rem; color: #666; margin-left: 2px; font-family: 'Inter', sans-serif; }
        .span-pos { grid-column: span 2; }

        /* Tables */
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
        th { text-align: left; padding: 14px 24px; background: rgba(0,0,0,0.2); color: var(--text-muted); font-weight: 600; font-size: 0.7rem; text-transform: uppercase; letter-spacing: 1px; border-bottom: var(--border); }
        td { padding: 14px 24px; border-bottom: 1px solid rgba(255,255,255,0.03); vertical-align: middle; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background: rgba(255,255,255,0.02); }

        /* Forms */
        .cmd-form { padding: 20px; display: flex; flex-direction: column; gap: 16px; }
        .input-group label { display: block; font-size: 0.75rem; color: var(--text-muted); margin-bottom: 6px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
        input, select { width: 100%; padding: 10px 14px; background: #0b0c10; border: 1px solid rgba(255,255,255,0.15); border-radius: 8px; color: #fff; font-family: 'Inter', sans-serif; font-size: 0.9rem; box-sizing: border-box; transition: all 0.2s; }
        input:focus, select:focus { outline: none; border-color: var(--accent); box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15); background: #121217; }
        .btn-tx { width: 100%; padding: 12px; background: linear-gradient(135deg, var(--accent), #4f46e5); border: none; border-radius: 8px; color: #fff; font-weight: 600; cursor: pointer; display: flex; justify-content: center; align-items: center; gap: 10px; transition: all 0.2s; letter-spacing: 0.5px; text-transform: uppercase; font-size: 0.85rem; box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3); }
        .btn-tx:hover { filter: brightness(1.1); transform: translateY(-1px); box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4); }
        .btn-tx:active { transform: translateY(0); }

        /* Terminal */
        .terminal { background: #08080a; padding: 16px; font-family: 'JetBrains Mono', monospace; font-size: 0.8rem; color: #10b981; height: 100%; overflow-y: auto; flex: 1; line-height: 1.6; }
        .log-line { display: flex; gap: 12px; margin-bottom: 4px; }
        .ts { color: #4b5563; user-select: none; }
        .sys { color: #6366f1; font-weight: bold; }
        .cmd { color: #f59e0b; font-weight: bold; }
        .warn { color: #ef4444; font-weight: bold; }

        /* Utils */
        .text-success { color: var(--success); text-shadow: 0 0 10px rgba(16, 185, 129, 0.3); }
        .text-warning { color: var(--warning); }
        .text-danger { color: var(--danger); }
        .badge { padding: 4px 10px; border-radius: 6px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
        .badge-running { background: rgba(245, 158, 11, 0.1); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.2); }
        .badge-success { background: rgba(16, 185, 129, 0.1); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.2); }
        .badge-failed { background: rgba(239, 68, 68, 0.1); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.2); }
        .badge-neutral { background: rgba(255, 255, 255, 0.05); color: #9ca3af; border: 1px solid rgba(255,255,255,0.1); }
        
        /* Responsiveness */
        @media (max-width: 1200px) {
            .dashboard { grid-template-columns: 1fr; height: auto; overflow: visible; padding: 16px; }
            .column { overflow: visible; min-height: 0; }
            .rover-card { margin-right: 0; }
            .terminal { min-height: 250px; }
            .panel { flex-shrink: 0; }
        }
        @media (max-height: 800px) { .terminal { min-height: 200px; } .column { padding-right: 8px; } }

        /* Scrollbar */
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #333; border-radius: 3px; }
        ::-webkit-scrollbar-thumb:hover { background: #444; }
        
        @keyframes pulse { 0% { opacity: 1; transform: scale(1); } 50% { opacity: 0.6; transform: scale(1.1); } 100% { opacity: 1; transform: scale(1); } }
    </style>
</head>
<body>

    <div class="header">
        <div class="brand">
            <div class="brand-icon">
                <i class="fas fa-satellite-dish"></i>
            </div>
            <div>GROUND CONTROL</div>
        </div>
        <div class="status-indicators">
            <div class="pill live-pill">
                <div class="live-dot"></div>
                <span id="uplink-status">UPLINK ACTIVE</span>
            </div>
            <div class="pill">
                <i class="fas fa-server"></i>
                <span>{{.Mothership}}</span>
            </div>
            <div class="pill">
                <i class="far fa-clock"></i>
                <span id="live-clock" style="font-family:'JetBrains Mono'">--:--:--</span>
            </div>
        </div>
    </div>

    <div class="dashboard">
        
        <div class="column">
            
            <div class="panel" style="margin-bottom: 0; background: transparent; box-shadow: none; border: none; padding: 0; flex-shrink: 0;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding: 0 10px;">
                    <div style="font-weight: 700; font-size: 0.9rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 1px;">
                        <i class="fas fa-rocket" style="color: var(--accent); margin-right: 8px;"></i> Active Fleet Status
                    </div>
                    <div class="pill" style="padding: 4px 12px; font-size: 0.75rem; background: rgba(99,102,241,0.1); border-color: rgba(99,102,241,0.3); color: #a5b4fc;">
                        <span id="active-rover-count" style="font-weight: 700; color: #fff;">{{.RoverCount}}</span> UNITS ONLINE
                    </div>
                </div>
            </div>

            <div id="rovers-container">
                {{range .Rovers}}
                <div class="rover-card" id="rover-card-{{.RoverId}}">
                    <div class="rc-header">
                        <div style="display:flex; align-items:center; gap:12px">
                            <span class="rc-title">{{.Name}}</span>
                            <span class="rc-id">ID: {{.RoverId}}</span>
                        </div>
                        <div style="display:flex; gap:10px; align-items:center">
                            <span class="badge badge-neutral" style="background:#111; color:#fff;" id="rover-status-{{.RoverId}}">{{.StatusString}}</span>
                            <i class="fas fa-circle text-success" style="font-size:8px; animation: pulse 2s infinite"></i>
                        </div>
                    </div>
                    
                    <div class="sensor-grid">
                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-bolt"></i> Battery</div>
                            <div class="sb-value {{.BatteryClass}}" id="rover-battery-{{.RoverId}}">{{.Battery}}<span class="sb-unit">%</span></div>
                        </div>
                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-heartbeat"></i> Health</div>
                            <div class="sb-value {{.HealthClass}}" id="rover-health-{{.RoverId}}">{{.SystemHealth}}<span class="sb-unit">%</span></div>
                        </div>
                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-stopwatch"></i> Uptime</div>
                            <div class="sb-value" style="font-size:0.85rem" id="rover-uptime-{{.RoverId}}">{{.UptimeFormatted}}</div>
                        </div>
                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-tachometer-alt"></i> Speed</div>
                            <div class="sb-value"><span id="rover-velocity-{{.RoverId}}">{{printf "%.2f" .Velocity}}</span><span class="sb-unit">m/s</span></div>
                        </div>

                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-thermometer-half"></i> Ext. Temp</div>
                            <div class="sb-value"><span id="rover-exttemp-{{.RoverId}}">{{printf "%.1f" .ExternalTemp}}</span><span class="sb-unit">K</span></div>
                        </div>
                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-microchip"></i> Int. Temp</div>
                            <div class="sb-value"><span id="rover-inttemp-{{.RoverId}}">{{printf "%.1f" .InternalTemp}}</span><span class="sb-unit">K</span></div>
                        </div>
                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-tint"></i> Humidity</div>
                            <div class="sb-value"><span id="rover-humidity-{{.RoverId}}">{{.Humidity}}</span><span class="sb-unit">%</span></div>
                        </div>
                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-compress-arrows-alt"></i> Pressure</div>
                            <div class="sb-value"><span id="rover-pressure-{{.RoverId}}">{{printf "%.1f" .Pressure}}</span><span class="sb-unit">hPa</span></div>
                        </div>
                        
                        <div class="sensor-box">
                            <div class="sb-label"><i class="fas fa-radiation"></i> Radiation</div>
                            <div class="sb-value"><span id="rover-radiation-{{.RoverId}}">{{printf "%.2f" .Radiation}}</span><span class="sb-unit">mSv</span></div>
                        </div>
                        <div class="sensor-box">
                            <div class="sb-label"><i class="far fa-compass"></i> Heading</div>
                            <div class="sb-value"><span id="rover-heading-{{.RoverId}}">{{.Direction}}</span><span class="sb-unit">°</span></div>
                        </div>
                        <div class="sensor-box span-pos">
                            <div class="sb-label"><i class="fas fa-map-marker-alt"></i> Global Position (X, Y, Z)</div>
                            <div class="sb-value" id="rover-pos-{{.RoverId}}">{{.PositionStr}}</div>
                        </div>
                    </div>
                </div>
                {{else}}
                <div id="no-telemetry-msg" style="padding:60px; text-align:center; color:var(--text-muted); border: 2px dashed var(--border); border-radius:var(--radius); background:rgba(255,255,255,0.01);">
                    <i class="fas fa-satellite-dish" style="font-size:2rem; margin-bottom:15px; opacity:0.3;"></i><br>
                    NO TELEMETRY SIGNAL ACQUIRED
                </div>
                {{end}}
            </div>

            <div class="panel" style="flex:1; min-height: 300px;">
                <div class="panel-head">
                    <div style="display:flex; align-items:center; gap:10px"><i class="fas fa-list-ul" style="color:var(--accent)"></i> Mission Registry</div>
                    <span class="badge badge-neutral"><span id="mission-count">{{.MissionCount}}</span> TOTAL</span>
                </div>
                <div class="table-wrap">
                    <table id="mission-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Unit</th>
                                <th>Operation</th>
                                <th>Status</th>
                                <th>Elapsed</th>
                                <th>Progress</th>
                            </tr>
                        </thead>
                        <tbody>
                            {{range .Missions}}
                            <tr id="mission-row-{{.MissionId}}">
                                <td style="font-family:'JetBrains Mono'">#{{.MissionId}}</td>
                                <td style="font-weight:600; color:#fff;">{{.RoverName}}</td>
                                <td>
                                    <div style="color:#e5e7eb; font-size:0.85rem;">{{.Tipo}}</div>
                                    <div style="font-size:0.75rem; color:var(--text-muted); margin-top:2px;">{{.Tarefa}}</div>
                                </td>
                                <td><span class="badge {{.EstadoClass}}">{{.Estado}}</span></td>
                                <td style="font-family:'JetBrains Mono'; font-size:0.85rem;">{{.TimeInfo}}</td>
                                <td>
                                    <div style="display:flex; justify-content:space-between; font-size:0.75rem; margin-bottom:4px;">
                                        <span>{{.Progresso}}</span>
                                    </div>
                                    <div style="height:6px; background:rgba(255,255,255,0.1); border-radius:3px; overflow:hidden;">
                                        <div style="width:{{.Progresso}}; height:100%; background:var(--accent); border-radius:3px;"></div>
                                    </div>
                                </td>
                            </tr>
                            {{else}}
                            <tr><td colspan="6" style="text-align:center; padding:40px; color:var(--text-muted)">Registry Empty</td></tr>
                            {{end}}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="column">
            
            <div class="panel">
                <div class="panel-head"><div style="display:flex; align-items:center; gap:10px"><i class="fas fa-terminal" style="color:var(--accent)"></i> Command Uplink</div></div>
                
                {{if .Message}}
                <div id="server-message" style="display:none;">{{.Message}}</div>
                {{end}}

                <form action="/mission/submit" method="POST" class="cmd-form">
                    <div class="input-group">
                        <label>Target Unit</label>
                        <select name="roverId" id="target-rover-select" required>
                            {{range .Rovers}}
                            <option value="{{.RoverId}}">Rover #{{.RoverId}} - {{.Name}}</option>
                            {{else}}
                            <option value="" disabled selected>OFFLINE</option>
                            {{end}}
                        </select>
                    </div>

                    <div class="input-group">
                        <label>Protocol</label>
                        <select name="type">
                            <option value="map_global">Global Mapping</option>
                            <option value="estudo_ambient">Atmospheric Study</option>
                            <option value="recon_foto">Visual Recon</option>
                            <option value="diagn_técnico">Diagnostics</option>
                            <option value="amostra_ambient">Sample Collection</option>
                        </select>
                    </div>

                    <div class="input-group">
                        <label>Operation Codename</label>
                        <input type="text" name="missionName" placeholder="e.g. ALPHA-1" required autocomplete="off">
                    </div>

                    <div class="input-group">
                        <label>Directives</label>
                        <input type="text" name="task" placeholder="Operational parameters..." required autocomplete="off">
                    </div>

                    <button type="submit" class="btn-tx">
                        TRANSMIT ORDER <i class="fas fa-paper-plane"></i>
                    </button>
                </form>
            </div>

            <div class="panel" style="flex:1; background:#08080a; border-color:#222; min-height: 250px;">
                <div class="panel-head" style="background:#111; border-color:#222; color:#666;">
                    <div style="display:flex; align-items:center; gap:10px"><i class="fas fa-code"></i> System Stream</div>
                    <i class="fas fa-circle text-success" style="font-size:8px"></i>
                </div>
                <div class="terminal" id="term">
                    <div class="log-line"><span class="ts">[BOOT]</span> <span class="sys">System initialized. All subsystems OK.</span></div>
                    <div class="log-line"><span class="ts">[AUTH]</span> <span>Operator identity verified. Session started.</span></div>
                    <div class="log-line"><span class="ts">[LINK]</span> <span>Connected to {{.Mothership}} via secure channel.</span></div>
                    <div class="log-line"><span class="ts">[SYNC]</span> <span>Clock synchronized with Mothership time.</span></div>
                </div>
            </div>

        </div>
    </div>

    <script>
        const terminal = document.getElementById('term'); 
        if(terminal) terminal.scrollTop = terminal.scrollHeight;

        const TIME_FACTOR = {{.TimeFactor}};
        // CLOCK INITIALIZATION WITH SERVER TIME
        // Value {{.ServerTime}} is injected by the Go template
        let clockTime = new Date({{.ServerTime}} * 1000);
        
        // --- LOGGER SYSTEM ---
        function addLog(type, msg) {
            const line = document.createElement('div');
            line.className = 'log-line';
            const ts = new Date().toLocaleTimeString('pt-PT');
            
            let typeHtml = "";
            if(type === "SYS") typeHtml = '<span class="sys">[SYS]</span>';
            else if(type === "CMD") typeHtml = '<span class="cmd">[CMD]</span>';
            else if(type === "WARN") typeHtml = '<span class="warn">[WARN]</span>';
            else if(type === "LINK") typeHtml = '<span style="color:#10b981; font-weight:bold;">[LINK]</span>';
            else typeHtml = '<span>[' + type + ']</span>';

            line.innerHTML = '<span class="ts">[' + ts + ']</span> ' + typeHtml + ' <span style="color:#d1d5db;">' + msg + '</span>';
            terminal.appendChild(line);
            terminal.scrollTop = terminal.scrollHeight;
        }

        // --- AJAX FORM SUBMISSION (No Refresh) ---
        document.querySelector('.cmd-form').addEventListener('submit', async function(e) {
            e.preventDefault();
            const form = this;
            const formData = new URLSearchParams(new FormData(form));

            addLog("CMD", "Transmitting mission package...");

            try {
                const res = await fetch(form.action, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'Accept': 'application/json'
                    },
                    body: formData
                });

                if (res.ok) {
                    const data = await res.json();
                    if(data.status === 'success') {
                        addLog("SYS", "Uplink confirmed: " + data.message);
                        form.reset();
                    } else {
                        addLog("WARN", "Transmission Error: " + data.message);
                    }
                } else {
                    addLog("WARN", "Uplink Failed: Server responded " + res.status);
                }
            } catch (err) {
                addLog("WARN", "Critical Uplink Failure: " + err.message);
            }
        });

        // Check for Server Message on Load (Legacy support)
        const serverMsg = document.getElementById('server-message');
        if (serverMsg) {
            const text = serverMsg.innerText.trim();
            if (text.includes("❌") || text.includes("⚠️")) {
                addLog("WARN", text);
            } else {
                addLog("CMD", text);
            }
        }

        // --- CLOCK ---
        function updateClock() {
            clockTime.setSeconds(clockTime.getSeconds() + TIME_FACTOR);
            const timeString = clockTime.toLocaleTimeString('pt-PT', { hour12: false });
            const clockEl = document.getElementById('live-clock');
            if(clockEl) clockEl.innerText = timeString;
        }
        setInterval(updateClock, 1000);

        // --- GLOBAL STATE ---
        let activeRoverIds = new Set();
        let roverNamesMap = new Map();
        let roverActiveMission = new Map(); // Maps RoverId to the MissionId currently running
        let previousMissionStates = new Map(); 
        let knownMissionIds = new Set();
        let isOnline = true; 
        let isFirstMissionLoad = true;

        // Load interrupted missions from storage (Persistence)
        let interruptedMissions = JSON.parse(localStorage.getItem('interrupted_missions') || '{}');

        function updateInterruptedStorage() {
             localStorage.setItem('interrupted_missions', JSON.stringify(interruptedMissions));
        }

        function formatUptime(seconds) {
            const h = Math.floor(seconds / 3600);
            const m = Math.floor((seconds % 3600) / 60);
            const s = seconds % 60;
            return String(h).padStart(2,'0') + 'h ' + String(m).padStart(2,'0') + 'm ' + String(s).padStart(2,'0') + 's';
        }

        function formatFullElapsed(startTime, estado) {
            if (!startTime || estado !== "EM_PROGRESSO") return "N/A";
            const diff = Math.floor((Date.now() - startTime) / 1000) * TIME_FACTOR;
            const h = Math.floor(diff / 3600);
            const m = Math.floor((diff % 3600) / 60);
            const s = diff % 60;
            if (h > 0) return "T+" + h.toString().padStart(2, '0') + "h " + m.toString().padStart(2, '0') + "m " + s.toString().padStart(2, '0') + "s";
            return "T+" + m.toString().padStart(2, '0') + "m " + s.toString().padStart(2, '0') + "s";
        }

        function getStatusStr(status) {
             const s = ["🟢 STANDBY", "🗺️ MAPPING", "🌡️ ENV. STUDY", "📸 RECON OP", "🔧 DIAGNOSTIC", "🧪 ANALYSIS", "🔴 ERROR STATE"];
             return s[status] || "❓ UNKNOWN";
        }

        function getStatusClass(status) {
            if(status === "EM_PROGRESSO") return "badge-running";
            if(status === "CONCLUIDA") return "badge-success";
            if(status === "FALHADA") return "badge-failed";
            if(status === "INTERROMPIDA") return "badge-failed";
            if(status === "ROVER OFFLINE") return "badge-failed";
            return "badge-neutral";
        }

        // --- DATA FETCHING LOOP ---
        async function fetchData() {
            try {
                // Fetch Telemetry
                const resTel = await fetch('{{.Mothership}}/api/telemetry/latest');
                if(!resTel.ok) throw new Error("Telemetry API Error");
                const rovers = await resTel.json();
                
                if (!isOnline) {
                    isOnline = true;
                    document.getElementById('uplink-status').innerText = "UPLINK ACTIVE";
                    document.querySelector('.live-dot').style.background = "#10b981";
                    addLog("LINK", "Telemetry uplink re-established.");
                }

                updateRovers(rovers);

                // Fetch Missions
                const resMiss = await fetch('{{.Mothership}}/api/missions');
                if(!resMiss.ok) throw new Error("Mission API Error");
                const missions = await resMiss.json();
                
                updateMissions(missions);

            } catch (e) {
                if (isOnline) {
                    console.error("Fetch error:", e);
                    isOnline = false;
                    document.getElementById('uplink-status').innerText = "CONNECTION LOST";
                    document.querySelector('.live-dot').style.background = "#ef4444";
                    activeRoverIds.clear(); 
                    addLog("WARN", "Connection to Mothership lost. Retrying...");
                }
            }
        }

        function generateRoverCard(r) {
            const id = r.RoverId;
            return '<div class="rover-card" id="rover-card-' + id + '">' +
                '<div class="rc-header">' +
                    '<div style="display:flex; align-items:center; gap:12px">' +
                        '<span class="rc-title">' + r.Name + '</span>' +
                        '<span class="rc-id">ID: ' + id + '</span>' +
                    '</div>' +
                    '<div style="display:flex; gap:10px; align-items:center">' +
                        '<span class="badge badge-neutral" style="background:#111; color:#fff;" id="rover-status-' + id + '">' + getStatusStr(r.Status) + '</span>' +
                        '<i class="fas fa-circle text-success" style="font-size:8px; animation: pulse 2s infinite"></i>' +
                    '</div>' +
                '</div>' +
                '<div class="sensor-grid">' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-bolt"></i> Battery</div>' +
                        '<div class="sb-value" id="rover-battery-' + id + '">' + r.Battery + '<span class="sb-unit">%</span></div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-heartbeat"></i> Health</div>' +
                        '<div class="sb-value" id="rover-health-' + id + '">' + r.SystemHealth + '<span class="sb-unit">%</span></div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-stopwatch"></i> Uptime</div>' +
                        '<div class="sb-value" style="font-size:0.85rem" id="rover-uptime-' + id + '">' + formatUptime(r.Uptime) + '</div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-tachometer-alt"></i> Speed</div>' +
                        '<div class="sb-value"><span id="rover-velocity-' + id + '">' + r.Velocity.toFixed(2) + '</span><span class="sb-unit">m/s</span></div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-thermometer-half"></i> Ext. Temp</div>' +
                        '<div class="sb-value"><span id="rover-exttemp-' + id + '">' + r.ExternalTemp.toFixed(1) + '</span><span class="sb-unit">K</span></div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-microchip"></i> Int. Temp</div>' +
                        '<div class="sb-value"><span id="rover-inttemp-' + id + '">' + r.InternalTemp.toFixed(1) + '</span><span class="sb-unit">K</span></div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-tint"></i> Humidity</div>' +
                        '<div class="sb-value"><span id="rover-humidity-' + id + '">' + r.Humidity + '</span><span class="sb-unit">%</span></div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-compress-arrows-alt"></i> Pressure</div>' +
                        '<div class="sb-value"><span id="rover-pressure-' + id + '">' + r.Pressure.toFixed(1) + '</span><span class="sb-unit">hPa</span></div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="fas fa-radiation"></i> Radiation</div>' +
                        '<div class="sb-value"><span id="rover-radiation-' + id + '">' + r.Radiation.toFixed(2) + '</span><span class="sb-unit">mSv</span></div>' +
                    '</div>' +
                    '<div class="sensor-box">' +
                        '<div class="sb-label"><i class="far fa-compass"></i> Heading</div>' +
                        '<div class="sb-value"><span id="rover-heading-' + id + '">' + r.Direction + '</span><span class="sb-unit">°</span></div>' +
                    '</div>' +
                    '<div class="sensor-box span-pos">' +
                        '<div class="sb-label"><i class="fas fa-map-marker-alt"></i> Global Position (X, Y, Z)</div>' +
                        '<div class="sb-value" id="rover-pos-' + id + '">[' + r.Position.X + ', ' + r.Position.Y + ', ' + r.Position.Z + ']</div>' +
                    '</div>' +
                '</div>' +
            '</div>';
        }

        function updateRovers(rovers) {
            rovers.sort((a, b) => a.RoverId - b.RoverId);
            
            const countEl = document.getElementById('active-rover-count');
            if(countEl) countEl.innerText = rovers.length;

            let newActiveIds = new Set();
            rovers.forEach(r => {
                newActiveIds.add(r.RoverId);
                roverNamesMap.set(r.RoverId, r.Name);
            });

            // Detect LOST rovers (Present in previous set, missing in new)
            // We iterate current DOM cards to see who was here before
            const existingCards = document.querySelectorAll('.rover-card');
            existingCards.forEach(card => {
                const idStr = card.id.replace('rover-card-', '');
                const id = parseInt(idStr, 10);
                
                // If rover disappeared
                if (!newActiveIds.has(id)) {
                    const rName = roverNamesMap.get(id) || "Unknown Unit";
                    addLog("WARN", "Signal Lost: Rover #" + id + " (" + rName + ") is offline.");
                    card.remove();
                }
            });

            activeRoverIds = newActiveIds;

            if (rovers.length > 0) {
                const noTelMsg = document.getElementById('no-telemetry-msg');
                if (noTelMsg) noTelMsg.remove();
            }

            const container = document.getElementById('rovers-container');
            const select = document.getElementById('target-rover-select');
            
            // Update Select Dropdown
            if (select.options.length - 1 !== rovers.length && rovers.length > 0) {
                 const currentVal = select.value;
                 select.innerHTML = '<option value="" disabled selected>Select Unit</option>';
                 rovers.forEach(r => {
                     const opt = document.createElement('option');
                     opt.value = r.RoverId;
                     opt.innerText = "Rover #" + r.RoverId + " - " + r.Name;
                     select.appendChild(opt);
                 });
                 if(currentVal) select.value = currentVal;
            }

            rovers.forEach(r => {
                const id = r.RoverId;
                if(!document.getElementById('rover-card-' + id)) {
                    const newCardHTML = generateRoverCard(r);
                    container.insertAdjacentHTML('beforeend', newCardHTML);
                    addLog("SYS", "New Unit Detected: Rover #" + id + " (" + r.Name + ")");
                }

                setText('rover-status-' + id, getStatusStr(r.Status));
                
                setText('rover-battery-' + id, r.Battery + "%");
                setClass('rover-battery-' + id, r.Battery > 70 ? "text-success" : (r.Battery > 30 ? "text-warning" : "text-danger"));
                
                setText('rover-health-' + id, r.SystemHealth + "%");
                setClass('rover-health-' + id, r.SystemHealth > 80 ? "text-success" : (r.SystemHealth > 60 ? "text-warning" : "text-danger"));
                
                setText('rover-uptime-' + id, formatUptime(r.Uptime));
                setText('rover-velocity-' + id, r.Velocity.toFixed(2));
                setText('rover-exttemp-' + id, r.ExternalTemp.toFixed(1));
                setText('rover-inttemp-' + id, r.InternalTemp.toFixed(1));
                setText('rover-humidity-' + id, r.Humidity);
                setText('rover-pressure-' + id, r.Pressure.toFixed(1));
                setText('rover-radiation-' + id, r.Radiation.toFixed(2));
                setText('rover-heading-' + id, r.Direction);
                setText('rover-pos-' + id, "[" + r.Position.X + ", " + r.Position.Y + ", " + r.Position.Z + "]");
            });
        }

        let missionStartTimes = new Map();

        function updateMissions(missions) {
            missions.sort((a, b) => a.MissionId - b.MissionId);
            document.getElementById('mission-count').innerText = missions.length;

            const tbody = document.querySelector('#mission-table tbody');
            // We rebuild table for simplicity
            tbody.innerHTML = '';

            if (missions.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:40px; color:var(--text-muted)">Registry Empty</td></tr>';
                return;
            }

            missions.forEach(m => {
                // DETECT NEW MISSION
                if (!knownMissionIds.has(m.MissionId)) {
                    knownMissionIds.add(m.MissionId);
                    // Only log if not the very first load (to avoid spamming history)
                    if (!isFirstMissionLoad) {
                        let rName = m.RoverName || ("Rover #" + m.RoverId);
                        addLog("CMD", "New Mission Received: #" + m.MissionId + " [" + m.Type + "] for " + rName);
                    }
                }

                // Monitor State Change for Logging
                const prevState = previousMissionStates.get(m.MissionId);
                if (prevState && prevState !== m.Estado) {
                    if (m.Estado === "CONCLUIDA") {
                        addLog("SYS", "Mission #" + m.MissionId + " (" + m.Type + ") completed successfully.");
                    } else if (m.Estado === "FALHADA") {
                        addLog("WARN", "Mission #" + m.MissionId + " failed.");
                    }
                }
                previousMissionStates.set(m.MissionId, m.Estado);

                // --- INTERRUPTED LOGIC REWORKED ---
                // Rule: If mission is IN_PROGRESS but Rover is OFFLINE -> Interrupted
                if (m.Estado === "EM_PROGRESSO") {
                    if (!activeRoverIds.has(m.RoverId)) {
                        // Rover is offline during active mission
                        if (!interruptedMissions[m.MissionId]) {
                             interruptedMissions[m.MissionId] = true;
                             updateInterruptedStorage();
                             addLog("WARN", "Connection lost with Unit #" + m.RoverId + " during active mission. Marked as INTERRUPTED.");
                        }
                    } else {
                        // Rover is online, check if it was interrupted before (reconnected?)
                         if (interruptedMissions[m.MissionId]) {
                             delete interruptedMissions[m.MissionId];
                             updateInterruptedStorage();
                             addLog("SYS", "Connection re-established with Unit #" + m.RoverId + ". Mission resuming.");
                         }
                    }
                }

                // Self-healing: If mission is confirmed finalized by backend, remove from interrupted list if present
                if ((m.Estado === "CONCLUIDA" || m.Estado === "FALHADA") && interruptedMissions[m.MissionId]) {
                     delete interruptedMissions[m.MissionId];
                     updateInterruptedStorage();
                }

                // Handle Status Override from Interrupted List
                let displayEstado = m.Estado;
                let displayClass = getStatusClass(m.Estado);
                
                if (interruptedMissions[m.MissionId]) {
                    displayEstado = "INTERROMPIDA";
                    displayClass = "badge-failed";
                }

                // Handle Rover Offline but mission not yet interrupted logic (visual only - grace period usually handled by backend)
                if (!activeRoverIds.has(m.RoverId) && m.Estado === "EM_PROGRESSO" && !interruptedMissions[m.MissionId]) {
                     // Fallback display if not yet caught by logic above
                    displayEstado = "ROVER OFFLINE"; 
                }

                const row = document.createElement('tr');
                const prog = m.Progresso.toFixed(1) + "%";
                
                let elapsedDisplay = "N/A";
                
                if (displayEstado === "EM_PROGRESSO") {
                    if (!missionStartTimes.has(m.MissionId)) {
                        missionStartTimes.set(m.MissionId, Date.now());
                    }
                    const startTime = missionStartTimes.get(m.MissionId);
                    elapsedDisplay = formatFullElapsed(startTime, "EM_PROGRESSO");
                } else if (displayEstado === "CONCLUIDA" || displayEstado === "FALHADA" || displayEstado === "INTERROMPIDA") {
                    elapsedDisplay = displayEstado === "INTERROMPIDA" ? "INTERRUPTED" : "COMPLETED";
                    missionStartTimes.delete(m.MissionId);
                }

                let rName = m.RoverName;
                if (!rName || rName === "") {
                    rName = roverNamesMap.get(m.RoverId) || ("Rover #" + m.RoverId);
                }
                
                row.innerHTML = '<td style="font-family:\'JetBrains Mono\'">#' + m.MissionId + '</td>' +
                    '<td style="font-weight:600; color:#fff;">' + rName + '</td>' +
                    '<td>' +
                        '<div style="color:#e5e7eb; font-size:0.85rem;">' + m.Type + '</div>' +
                        '<div style="font-size:0.75rem; color:var(--text-muted); margin-top:2px;">' + m.Tarefa + '</div>' +
                    '</td>' +
                    '<td><span class="badge ' + displayClass + '">' + displayEstado + '</span></td>' +
                    '<td style="font-family:\'JetBrains Mono\'; font-size:0.85rem;">' + elapsedDisplay + '</td>' +
                    '<td>' +
                        '<div style="display:flex; justify-content:space-between; font-size:0.75rem; margin-bottom:4px;">' +
                            '<span>' + prog + '</span>' +
                        '</div>' +
                        '<div style="height:6px; background:rgba(255,255,255,0.1); border-radius:3px; overflow:hidden;">' +
                            '<div style="width:' + prog + '; height:100%; background:var(--accent); border-radius:3px;"></div>' +
                        '</div>' +
                    '</td>';
                tbody.appendChild(row);
            });

            isFirstMissionLoad = false;
        }

        function setText(id, text) {
            const el = document.getElementById(id);
            if(el) el.innerText = text;
        }
        
        function setClass(id, cls) {
            const el = document.getElementById(id);
            if(el) {
                el.classList.remove("text-success", "text-warning", "text-danger", "sb-value");
                el.classList.add("sb-value", cls);
            }
        }

        setInterval(fetchData, 1000);
        fetchData(); // Initial load

    </script>
</body>
</html>
`
