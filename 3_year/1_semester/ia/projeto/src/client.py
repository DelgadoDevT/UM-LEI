import os
import sys
import socket
import pickle
from enum import Enum
from typing import Dict

current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)

from rich.prompt import Prompt, IntPrompt, Confirm
from rich.table import Table
from rich import box
from rich.columns import Columns
from rich.panel import Panel
from rich.align import Align
from rich.text import Text
from rich.layout import Layout
from rich.console import Group
from src.utils.Interface import UI, console

class AlgorithmType(Enum):
    """
    Enumeration of available pathfinding algorithms in the system.
    
    Attributes:
        DFS: Depth-First Search
        BFS: Breadth-First Search
        UNIFORM_COST: Uniform Cost Search
        GREEDY: Greedy Best-First Search
        WEIGHTED_A_STAR: Weighted A* Search
        A_STAR: Standard A* Search (recommended)
    """
    DFS = "dfs"
    BFS = "bfs"
    UNIFORM_COST = "uniform_cost"
    GREEDY = "greedy"
    WEIGHTED_A_STAR = "weighted_a_star"
    A_STAR = "a_star"

class TaxiGreenClient:
    """
    TaxiGreen client with graphical terminal interface.
    
    This class implements the user interface and communication with the server
    through TCP sockets. It uses the Rich library to create a visually rich
    terminal interface.
    
    Attributes:
        host (str): Server address (default: 'localhost')
        port (int): Server port (default: 8888)
        socket (socket.socket): Communication socket with the server
        theme_color (str): Visual theme color (default: 'bright_green')
    """

    def __init__(self, host='localhost', port=8888):
        """
        Initialize the TaxiGreen client.
        
        Args:
            host (str, optional): Server address. Defaults to 'localhost'.
            port (int, optional): Server port. Defaults to 8888.
        """
        self.host = host
        self.port = port
        self.socket = None
        self.theme_color = "bright_green"

    def print_logo(self):
        """Print the styled ASCII logo of TaxiGreen."""
        logo = """
  _______  _______  __   __  ___           _______  ______    _______  _______  __    _ 
 |       ||   _   ||  | |  ||   |         |       ||    _ |  |       ||       ||  |  | |
 |_     _||  | |  ||  \_/  ||   |  _____  |    ___||   | ||  |    ___||    ___||   |_| |
   |   |  |  |_|  ||       ||   | |     | |   | __ |   |_||_ |   |___ |   |___ |       |
   |   |  |   _   ||   _   ||   | |_____| |   ||  ||    __  ||    ___||    ___||  _    |
   |   |  |  | |  ||  / \  ||   |         |   |_| ||   |  | ||   |___ |   |___ | | |   |
   |___|  |__| |__||__| |__||___|         |_______||___|  |_||_______||_______||_|  |__|
        """
        console.print(Panel(Align.center(Text(logo, style="bold bright_green")), box=box.HEAVY, border_style="green"))

    def connect_to_server(self):
        """
        Establish TCP connection with the server.
        
        Returns:
            bool: True if connection was successfully established, False otherwise.
        """
        try:
            with console.status("[bold green]A estabelecer ligação segura ao mainframe...[/bold green]",
                                spinner="dots12"):
                self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.socket.connect((self.host, self.port))
            UI.print_success("Ligação estabelecida com o servidor TaxiGreen!")
            return True
        except Exception as e:
            console.print(
                Panel(f"[bold red]FALHA NA LIGAÇÃO[/bold red]\n\n{str(e)}", title="Erro de Rede", border_style="red"))
            return False

    def send_command(self, command: str, data: Dict = None):
        """
        Send a serialized command to the server.
        
        Args:
            command (str): Name of the command to execute.
            data (Dict, optional): Additional data for the command. Defaults to None.
        
        Returns:
            Dict: Server response in format {'success': bool, ...}
        """
        if not self.socket:
            return {'success': False, 'error': 'Não conectado ao servidor'}

        try:
            message = {'command': command, 'data': data or {}}
            self.socket.send(pickle.dumps(message))
            response = self.socket.recv(1048576)
            return pickle.loads(response)
        except Exception as e:
            return {'success': False, 'error': str(e)}

    def create_manual_request(self):
        """Create a manual trip request through the user interface."""
        console.clear()
        console.rule(f"[{self.theme_color}]NOVA VIAGEM MANUAL[/{self.theme_color}]")

        console.print(Panel("Insira os dados da viagem abaixo.\nO sistema calculará a rota ideal automaticamente.",
                            box=box.ROUNDED, border_style="cyan"))

        try:
            origin = IntPrompt.ask(f"[bold cyan]📍 Nodo de Origem[/bold cyan]")
            if origin == 99:
                UI.print_error("De momento não é possível aceder ao nodo requisitado.")
                return

            destination = IntPrompt.ask(f"[bold cyan]🏁 Nodo de Destino[/bold cyan]")
            if destination == 99:
                UI.print_error("De momento não é possível aceder ao nodo requisitado.")
                return

            num_passengers = IntPrompt.ask("[bold white]👥 Passageiros[/bold white]", default=1,
                                           choices=["1", "2", "3", "4"])
            environmental_pref = Confirm.ask("[bold green]🌱 Modo Eco? (Preferência Ambiental)[/bold green]")

            with console.status("[bold yellow]A processar pedido no servidor...[/bold yellow]", spinner="earth"):
                response = self.send_command('create_request', {
                    'origin': origin,
                    'destination': destination,
                    'num_passengers': num_passengers,
                    'environmental_pref': environmental_pref
                })

            if response['success']:
                UI.print_success(response['result'])
            else:
                UI.print_error(response['error'])

        except ValueError:
            UI.print_error("Insira valores numéricos válidos.")
        except Exception as e:
            UI.print_error(f"Erro ao criar pedido: {e}")

    def show_car_info(self):
        """Display information about the current state of the vehicle fleet."""
        with console.status("[bold cyan]A contactar satélites da frota...[/bold cyan]", spinner="bouncingBar"):
            response = self.send_command('get_car_info')

        if not response['success']:
            UI.print_error(response['error'])
            return

        cars_info = response['data']

        table = Table(title="Estado da Frota em Tempo Real", box=box.DOUBLE_EDGE, header_style="bold white on blue",
                      expand=True)
        table.add_column("ID", justify="center", style="bold white")
        table.add_column("Veículo", style="cyan")
        table.add_column("Motor", justify="center")
        table.add_column("Status", justify="center")
        table.add_column("Energia", justify="center")
        table.add_column("Posição", justify="center")

        for car in cars_info:
            reserve_indicator = " ⚠️" if car['on_reserve'] else ""

            if car['engine_type'] == 'eletrico':
                eng_type = "[bold blue]ELÉTRICO[/bold blue]"
                icon = "🔋"
            else:
                eng_type = "[bold red]COMBUSTÃO[/bold red]"
                icon = "⛽"

            try:
                bat_pct = float(car['battery'].replace('%', ''))
                if bat_pct > 60:
                    bat_style = "bold green"
                    bat_icon = "🟩"
                elif bat_pct > 25:
                    bat_style = "bold yellow"
                    bat_icon = "🟨"
                else:
                    bat_style = "bold red blink"
                    bat_icon = "🟥"
            except:
                bat_style = "white"
                bat_icon = ""

            battery_display = f"[{bat_style}]{bat_icon} {car['battery']}[/{bat_style}]\n[dim]{car['autonomy']}[/dim]"

            status_style = "bold green reverse" if car['status'].lower() == 'livre' else "bold yellow"
            status_text = f"[{status_style}] {car['status'].upper()} [/{status_style}]{reserve_indicator}"

            table.add_row(
                f"{car['id']}",
                f"{icon} {car['model']}",
                eng_type,
                status_text,
                battery_display,
                f"📍 Nodo {car['location']}"
            )

        console.print(table)

    def show_request_info(self):
        """Display information about all active requests in the system."""
        with console.status("[bold cyan]A carregar base de dados de pedidos...[/bold cyan]", spinner="material"):
            response = self.send_command('get_request_info')

        if not response['success']:
            UI.print_error(response['error'])
            return

        requests_info = response['data']

        if not requests_info:
            UI.print_info("A base de dados de pedidos está vazia.")
            return

        table = Table(title="Quadro de Pedidos Ativos", box=box.SIMPLE_HEAD, show_lines=True, expand=True)
        table.add_column("ID", justify="center", style="bold white", width=6)
        table.add_column("Status", justify="center", width=12)
        table.add_column("Itinerário", justify="left")
        table.add_column("Detalhes", justify="left")
        table.add_column("Atribuição", justify="left")

        for req in requests_info:
            status_map = {
                'concluido': ("✅ CONCLUÍDO", "green"),
                'pendente': ("⏳ PENDENTE", "yellow"),
                'atribuido': ("🚙 A CAMINHO", "blue"),
                'em_curso': ("🔄 EM VIAGEM", "cyan"),
                'cancelado': ("❌ CANCELADO", "red")
            }
            label, color = status_map.get(req['status'], ("📋 OUTRO", "white"))
            status_display = f"[bold {color}]{label}[/bold {color}]"

            route = Group(
                Text(f"De: Nodo {req['origin']}", style="bold cyan"),
                Text(f"Para: Nodo {req['destination']}", style="bold magenta")
            )

            eco = "🌿 Eco" if req['environmental'] else "💨 Std"
            details = f"👥 {req['passengers']} Pax\n{eco}\n💰 {req['price']}"

            if req['assigned_car'] != "N/A":
                car_info = f"[bold white]Carro {req['assigned_car']}[/bold white]"
                dist_info = f"Dist: {req['total_distance']}"
                time_info = f"Tempo: {req['total_time']}"
                logistics = Group(Text.from_markup(car_info), Text(dist_info, style="dim"),
                                  Text(time_info, style="dim"))
            else:
                logistics = Text("A aguardar frota...", style="italic dim")

            table.add_row(
                str(req['id']),
                status_display,
                route,
                details,
                logistics
            )

        console.print(table)

    def show_route_map(self):
        """
        Generate and display an interactive route map.
        
        The map is generated as an HTML file that can be opened in a browser.
        """
        with console.status("[bold cyan]A renderizar mapa topográfico...[/bold cyan]", spinner="arc"):
            response = self.send_command('generate_route_map')

        if not response['success']:
            UI.print_error(response['error'])
            return

        filename = response['filename']
        file_path = os.path.abspath(filename)
        file_url = f"file://{file_path}"

        link_panel = Panel(
            Align.center(
                f"[bold white]O mapa foi gerado com sucesso![/bold white]\n\n"
                f"🔗 [underline blue link={file_url}]{filename}[/underline blue link]\n\n"
                f"[italic dim](Clique no link acima ou copie o caminho)[/italic dim]"
            ),
            title="🗺️ Navegação",
            border_style="green",
            box=box.DOUBLE
        )
        console.print(link_panel)

        history_response = self.send_command('get_route_history')
        if history_response['success']:
            route_history = history_response['data']
            if route_history:
                table = Table(title="Histórico de Rotas", box=box.MINIMAL)
                table.add_column("Rota", style="cyan")
                table.add_column("Viagens Totais", justify="center", style="bold green")
                table.add_column("Distribuição da Frota")

                for route_hash, route_info in route_history.items():
                    origin = route_info['origin']
                    destination = route_info['destination']
                    total_trips = sum(route_info['cars'].values())

                    details = []
                    for car_plate, count in route_info['cars'].items():
                        details.append(f"[bold]{car_plate}[/bold]: {count}")

                    table.add_row(
                        f"{origin} ➜ {destination}",
                        str(total_trips),
                        " | ".join(details)
                    )
                console.print(table)
            else:
                UI.print_info("Ainda não existem rotas no histórico de navegação.")

    def change_algorithm(self):
        """Allow the user to change the pathfinding algorithm used."""
        console.clear()
        console.rule("[bold magenta]CONFIGURAÇÃO DE NAVEGAÇÃO[/bold magenta]")

        options = {
            "1": ("DFS", "Depth-First Search - Exploração profunda"),
            "2": ("BFS", "Breadth-First Search - Garante o caminho mais curto (neste grafo)"),
            "3": ("Uniform Cost", "Custo Uniforme - Otimiza pelo custo do trajeto"),
            "4": ("Greedy", "Greedy Best-First - Foca apenas no objetivo final"),
            "5": ("Weighted A*", "A* Ponderado - Rápido mas não garantidamente ótimo"),
            "6": ("A* (Standard)", "A* Search - O equilíbrio perfeito (Recomendado)")
        }

        grid = Table.grid(expand=True, padding=(0, 2))
        grid.add_column(justify="right", style="cyan")
        grid.add_column(style="white")
        grid.add_column(style="dim")

        for k, (name, desc) in options.items():
            grid.add_row(f"[bold]{k})[/bold]", name, desc)

        console.print(Panel(grid, title="Algoritmos Disponíveis", border_style="magenta"))

        choice = Prompt.ask("Selecione o novo protocolo", choices=list(options.keys()))

        algorithm_map = {
            '1': 'dfs', '2': 'bfs', '3': 'uniform_cost',
            '4': 'greedy', '5': 'weighted_a_star', '6': 'a_star'
        }

        with console.status("[bold yellow]A reprogramar sistema de navegação...[/bold yellow]"):
            response = self.send_command('set_algorithm', {
                'algorithm': algorithm_map[choice]
            })

        if response['success']:
            UI.print_success(response['message'])
        else:
            UI.print_error(response['error'])

    def change_environment(self):
        """Configure the environmental conditions of the simulation."""
        console.clear()
        console.rule("[bold blue]CONTROLO AMBIENTAL GLOBAL[/bold blue]")

        meteo_table = Table(show_header=False, box=box.SIMPLE)
        weather_opts = {'1': 'normal', '2': 'chuva', '3': 'nevoeiro', '4': 'neve', '5': 'gelo'}
        for k, v in weather_opts.items():
            meteo_table.add_row(f"[cyan]{k})[/cyan] {v.title()}")

        traffic_table = Table(show_header=False, box=box.SIMPLE)
        traffic_opts = {'1': 'normal', '2': 'lento', '3': 'muito lento'}
        for k, v in traffic_opts.items():
            traffic_table.add_row(f"[cyan]{k})[/cyan] {v.title()}")

        console.print(Columns([
            Panel(meteo_table, title="🌤️ Meteorologia", border_style="blue"),
            Panel(traffic_table, title="🚗 Trânsito", border_style="yellow")
        ]))

        w_choice = Prompt.ask("Nova Meteorologia", choices=list(weather_opts.keys()), default='1')
        t_choice = Prompt.ask("Novo Trânsito", choices=list(traffic_opts.keys()), default='1')

        new_weather = weather_opts[w_choice]
        new_traffic = traffic_opts[t_choice]

        console.print("\n[bold]📍 Seleção de Zona de Impacto:[/bold]")
        zone_map = {
            '1': 'center_north', '2': 'center_northeast', '3': 'center_east',
            '4': 'center_southeast', '5': 'center_south', '6': 'center_southwest',
            '7': 'center_west', '8': 'center_northwest', '9': 'periphery_north',
            '10': 'periphery_south', '11': 'periphery_east', '12': 'periphery_west'
        }

        grid_table = Table.grid(expand=True, padding=(0, 2))
        grid_table.add_column()
        grid_table.add_column()

        items = list(zone_map.items())
        half = (len(items) + 1) // 2
        for i in range(half):
            k1, v1 = items[i]
            col1 = f"[cyan]{k1})[/cyan] {v1}"
            col2 = ""
            if i + half < len(items):
                k2, v2 = items[i + half]
                col2 = f"[cyan]{k2})[/cyan] {v2}"
            grid_table.add_row(col1, col2)

        console.print(grid_table)
        z_choice = Prompt.ask("Zona", choices=list(zone_map.keys()), default='1')
        new_zone = zone_map[z_choice]

        with console.status("[bold yellow]A atualizar parâmetros da simulação...[/bold yellow]"):
            response = self.send_command('update_environment', {
                'conditions': {
                    'weather': new_weather,
                    'traffic': new_traffic,
                    'zone': new_zone
                }
            })

        if response['success']:
            UI.print_success(response['message'])
        else:
            UI.print_error(response['error'])

    def generate_reports(self):
        """Generate CSV reports with current system data."""
        with console.status("[bold green]A compilar dados e gerar CSV...[/bold green]", spinner="dots"):
            response = self.send_command('generate_reports')

        if response['success']:
            UI.print_success(f"Dados exportados com sucesso!\n{response['result']}")
        else:
            UI.print_error(response['error'])

    def show_system_stats(self):
        """Display a dashboard with system statistics."""
        response = self.send_command('get_system_stats')

        if not response['success']:
            UI.print_error(response['error'])
            return

        stats = response['data']

        p_req = Panel(
            Align.center(
                f"[bold white]Total:[/bold white] {stats['requests']['total']}\n"
                f"[bold green]Concluídos:[/bold green] {stats['requests']['concluidos']}\n"
                f"[bold yellow]Pendentes:[/bold yellow] {stats['requests']['pendentes']}"
            ),
            title="📊 Pedidos", border_style="blue"
        )

        p_env = Panel(
            Align.center(
                f"[bold cyan]Algo:[/bold cyan] {stats['algorithm'].upper()}\n"
                f"🌤️ {stats['environment']['meteorologia'].title()}\n"
                f"🚗 {stats['environment']['transito'].title()}"
            ),
            title="🌍 Configuração", border_style="green"
        )

        p_fleet = Panel(
            Align.center(
                f"\n[bold big]{stats['cars']['total']}[/bold big]\n[dim]Veículos Ativos[/dim]\n"
            ),
            title="🚗 Frota", border_style="magenta"
        )

        console.print(Layout(Group(
            Panel(Align.center("[bold]DASHBOARD DO SISTEMA[/bold]"), box=box.DOUBLE),
            Columns([p_req, p_env, p_fleet])
        )))

    def show_dashboard_menu(self):
        """Render the main system menu in dashboard format."""
        console.clear()
        self.print_logo()

        grid = Table.grid(expand=True, padding=(1, 1))
        grid.add_column(ratio=1)
        grid.add_column(ratio=1)

        actions_panel = Panel(
            Group(
                Text("1. Nova Viagem Manual", style="bold green"),
                Text("2. Estado da Frota", style="bold cyan"),
                Text("3. Estado dos Pedidos", style="bold cyan"),
                Text("4. Mapa de Rotas", style="bold cyan"),
                Text("-----------------------", style="dim"),
                Text("7. Gerar Relatórios", style="bold white"),
                Text("0. Sair", style="bold red")
            ),
            title="🎮 Centro de Controlo", border_style="green", box=box.ROUNDED
        )

        config_panel = Panel(
            Group(
                Text("5. Alterar Algoritmo", style="bold magenta"),
                Text("6. Condições Ambientais", style="bold cyan"),
                Text("8. Dashboard do Sistema", style="bold yellow"),
            ),
            title="⚙️ Sistema", border_style="white", box=box.ROUNDED
        )

        grid.add_row(actions_panel, config_panel)
        console.print(grid)

    def run(self):
        """
        Main client execution loop.
        
        Controls program flow, displaying the menu and processing user choices
        until the exit option is selected.
        """
        if not self.connect_to_server():
            return

        while True:
            self.show_dashboard_menu()

            choice = IntPrompt.ask("Selecione uma opção", choices=["0", "1", "2", "3", "4", "5", "6", "7", "8"])
            choice = str(choice)

            if choice == '0':
                console.print(Panel("[bold red]A encerrar ligação... Até à próxima![/bold red]", box=box.HEAVY))
                break
            elif choice == '1':
                self.create_manual_request()
            elif choice == '2':
                self.show_car_info()
            elif choice == '3':
                self.show_request_info()
            elif choice == '4':
                self.show_route_map()
            elif choice == '5':
                self.change_algorithm()
            elif choice == '6':
                self.change_environment()
            elif choice == '7':
                self.generate_reports()
            elif choice == '8':
                self.show_system_stats()

            if choice != '0':
                Prompt.ask("\n[dim]Pressione Enter para voltar ao menu...[/dim]")


if __name__ == "__main__":
    """
    Main entry point of the TaxiGreen client.
    
    Instantiates and runs the client when the script is executed directly.
    """
    client = TaxiGreenClient()
    client.run()