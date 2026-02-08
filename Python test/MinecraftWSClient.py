import json
import time
import threading
from websocket import WebSocketApp


class MinecraftWSClient:
    def __init__(self, host, port, username, password):
        self.url = f"ws://{host}:{port}"
        self.username = username
        self.password = password

        self.ws = None
        self.authenticated = False
        self.players = {}

    # -------------------------------
    # WebSocket lifecycle
    # -------------------------------

    def connect(self):
        self.ws = WebSocketApp(
            self.url,
            on_open=self._on_open,
            on_message=self._on_message,
            on_error=self._on_error,
            on_close=self._on_close
        )

        thread = threading.Thread(target=self.ws.run_forever, daemon=True)
        thread.start()

        # Wait for auth result
        timeout = time.time() + 5
        while not self.authenticated and time.time() < timeout:
            time.sleep(0.1)

        if not self.authenticated:
            raise RuntimeError("Authentication failed or timed out")

    def close(self):
        if self.ws:
            self.ws.close()

    # -------------------------------
    # WebSocket callbacks
    # -------------------------------

    def _on_open(self, ws):
        auth = {
            "type": "auth",
            "username": self.username,
            "password": self.password
        }
        ws.send(json.dumps(auth))

    def _on_message(self, ws, message):
        # Ignore empty or whitespace messages
        if not message or not message.strip():
            return

        try:
            data = json.loads(message)
        except json.JSONDecodeError:
            # Server sent non-JSON (log/banner/etc)
            print("[WS] Non-JSON message ignored:", message)
            return

        msg_type = data.get("type")

        if msg_type == "auth_ok":
            print("[WS] Auth success")
            self.authenticated = True

        elif msg_type == "auth_fail":
            print("[WS] Auth failed")
            self.authenticated = False
            ws.close()

        elif msg_type == "players":
            self.players = data.get("players", {})

    def _on_error(self, ws, error):
        print("[WS ERROR]", error)

    def _on_close(self, ws, *_):
        print("[WS CLOSED]")

    # -------------------------------
    # API METHODS
    # -------------------------------

    def send_command(self, command: str):
        payload = {
            "type": "command",
            "command": command
        }
        self.ws.send(json.dumps(payload))

    def request_player_coords(self):
        payload = {
            "type": "get_players"
        }
        self.ws.send(json.dumps(payload))

        # Wait briefly for response
        time.sleep(0.2)
        return self.players

    # -------------------------------
    # HIGH-LEVEL FUNCTIONS YOU ASKED
    # -------------------------------

    def auth_and_send(self, commands: list[str]):
        """
        Auth (if not already), send commands, and update coords
        """
        if not self.authenticated:
            self.connect()

        for cmd in commands:
            self.send_command(cmd)

        return self.request_player_coords()

    def command_at_player_coords(self, player_name: str, command_template: str):
        """
        Gets player coords and executes command at that location

        command_template example:
        "summon tnt {x} {y} {z}"
        """
        self.request_player_coords()

        if player_name not in self.players:
            raise ValueError(f"Player '{player_name}' not online")

        p = self.players[player_name]

        cmd = command_template.format(
            x=p["x"],
            y=p["y"],
            z=p["z"],
            world=p["world"]
        )

        self.send_command(cmd)
        return cmd
