from rich.console import Console
from rich.panel import Panel
from rich.table import Table
from rich.text import Text
from rich.prompt import Prompt, IntPrompt
from rich.progress import Progress, SpinnerColumn, TextColumn
from rich.layout import Layout
from rich.align import Align
from rich import box
import os
import time

# Inicializar consola global
console = Console()


class UI:
    """
    Static utility class to handle Terminal User Interface (TUI) using the 'rich' library.
    Provides methods for menus, headers, loading animations, and data tables.
    """
    @staticmethod
    def clear_screen():
        """
        Clears the terminal screen (cross-platform compatible).
        """
        os.system('cls' if os.name == 'nt' else 'clear')

    @staticmethod
    def print_header(title="SISTEMA DE GESTÃO DE TRÁFEGO IA-2526", subtitle="Cliente Conectado"):
        """
        Prints a stylized header centered on the screen.

        :param title: Main title text.
        :param subtitle: Subtitle text.
        """
        UI.clear_screen()
        grid = Table.grid(expand=True)
        grid.add_column(justify="center", ratio=1)
        grid.add_column(justify="right")

        title_text = Text(title, style="bold cyan")
        subtitle_text = Text(subtitle, style="italic magenta")

        panel = Panel(
            Align.center(
                Text.assemble(title_text, "\n", subtitle_text)
            ),
            box=box.ROUNDED,
            border_style="cyan",
            padding=(1, 2)
        )
        console.print(panel)

    @staticmethod
    def print_menu(options):
        """
        Displays an interactive menu and prompts the user for a choice.

        :param options: A dictionary where key is the option number and value is the description.
        :return: The integer key selected by the user.
        :rtype: int
        """
        table = Table(title="Menu de Operações", box=box.SIMPLE_HEAD, show_lines=True)
        table.add_column("Opção", justify="center", style="cyan", no_wrap=True)
        table.add_column("Descrição", style="white")

        for key, value in options.items():
            table.add_row(str(key), value)

        console.print(Align.center(table))

        choice = IntPrompt.ask(
            "[bold yellow]Selecione uma opção[/bold yellow]",
            choices=[str(k) for k in options.keys()]
        )
        return choice

    @staticmethod
    def print_success(message):
        """
        Prints a success message inside a green-bordered panel.

        :param message: The message string.
        """
        console.print(Panel(f"[bold green]SUCESSO:[/bold green] {message}", border_style="green"))

    @staticmethod
    def print_error(message):
        """
        Prints an error message inside a red-bordered panel.

        :param message: The error message string.
        """
        console.print(Panel(f"[bold red]ERRO:[/bold red] {message}", border_style="red"))

    @staticmethod
    def print_info(message):
        """
        Prints a standard info message in blue.

        :param message: The info message string.
        """
        console.print(f"[bold blue]INFO:[/bold blue] {message}")

    @staticmethod
    def loading_animation(task_description="A processar...", duration=2):
        """
        Displays a fake loading spinner for visual feedback.

        :param task_description: Text to display next to spinner.
        :param duration: How long the animation lasts in seconds.
        """
        with Progress(
                SpinnerColumn(),
                TextColumn("[progress.description]{task.description}"),
                transient=True
        ) as progress:
            progress.add_task(description=task_description, total=None)
            time.sleep(duration)

    @staticmethod
    def show_data_table(title, headers, rows):
        """
        Generates and prints a formatted table for lists of data (e.g., cars, nodes).

        :param title: Table title.
        :param headers: List of column headers.
        :param rows: List of rows (lists/tuples) containing data.
        """
        table = Table(title=title, box=box.ROUNDED, show_header=True, header_style="bold magenta")

        for header in headers:
            table.add_column(header, justify="center")

        for row in rows:
            # Converte tudo para string para evitar erros de renderização
            row_str = [str(item) for item in row]
            table.add_row(*row_str)

        console.print(table)